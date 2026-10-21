import { Response } from 'express';
import { AuthenticatedRequest } from '../middleware/authMiddleware';
import { CryptoService } from '../services/cryptoService';
import { BinanceAdapter } from '../services/exchange/binanceAdapter';
import { OkxAdapter } from '../services/exchange/okxAdapter';
import { BybitAdapter } from '../services/exchange/bybitAdapter';
import { DatabaseService } from '../services/storage/databaseService';
import { PaperTradingEngine } from '../services/trading/paperTradingEngine';

const db = DatabaseService.getInstance();

function getAdapter(exchangeName: string) {
  switch (exchangeName.toLowerCase()) {
    case 'okx': return new OkxAdapter();
    case 'bybit': return new BybitAdapter();
    default: return new BinanceAdapter();
  }
}

export class ExchangeController {
  static async saveKeys(req: AuthenticatedRequest, res: Response): Promise<void> {
    try {
      const userId = req.userId!;
      const { exchangeName, apiKey, apiSecret } = req.body;

      if (!apiKey || !apiSecret) {
        res.status(400).json({ success: false, data: null, message: 'API Key و API Secret مطلوبان' });
        return;
      }

      if (apiKey.trim().length < 16 || apiSecret.trim().length < 16) {
        res.status(400).json({ success: false, data: null, message: 'طول مفاتيح API غير صالح' });
        return;
      }

      const selectedExchange = (exchangeName || 'binance').toLowerCase();
      const adapter = getAdapter(selectedExchange);

      const status = await adapter.validateKeys(apiKey.trim(), apiSecret.trim());
      if (!status.isConnected) {
        res.status(400).json({
          success: false,
          data: status,
          message: status.errorMessage || 'فشل التحقق من مفاتيح API لدى المنصة. تأكد من صحة المفاتيح وصلاحية Spot Trading.'
        });
        return;
      }

      if (status.canWithdraw) {
        res.status(400).json({
          success: false,
          data: null,
          message: 'تحذير أمان: تم رفض المفتاح لوجود صلاحية السحب (Withdrawals). يرجى إنشاء مفتاح مخصص للتداول فقط دون سحب.'
        });
        return;
      }

      const encryptedKey = CryptoService.encrypt(apiKey.trim());
      const encryptedSecret = CryptoService.encrypt(apiSecret.trim());

      await db.saveExchangeKeys(userId, selectedExchange, encryptedKey, encryptedSecret);

      res.json({
        success: true,
        data: status,
        message: 'تم حفظ وتشفير مفاتيح API بنجاح بعد التحقق منها'
      });
    } catch (error: any) {
      res.status(500).json({ success: false, data: null, message: error.message });
    }
  }

  static async getBalance(req: AuthenticatedRequest, res: Response): Promise<void> {
    try {
      const userId = req.userId!;
      const userKeyInfo = await db.getExchangeKeys(userId, 'binance');

      if (!userKeyInfo) {
        res.status(400).json({
          success: false,
          data: null,
          message: 'لم يتم ربط مفاتيح المنصة بعد. يرجى إدخال مفاتيح API أولاً.'
        });
        return;
      }

      const apiKey = CryptoService.decrypt(userKeyInfo.apiKeyEncrypted);
      const apiSecret = CryptoService.decrypt(userKeyInfo.apiSecretEncrypted);

      const adapter = getAdapter(userKeyInfo.exchangeName);
      const portfolio = await adapter.getPortfolio(apiKey, apiSecret);

      res.json({
        success: true,
        data: portfolio,
        message: 'تم جلب الرصيد الحقيقي بنجاح'
      });
    } catch (error: any) {
      res.status(500).json({ success: false, data: null, message: error.message });
    }
  }

  static async getStatus(req: AuthenticatedRequest, res: Response): Promise<void> {
    try {
      const userId = req.userId!;
      const userKeyInfo = await db.getExchangeKeys(userId, 'binance');

      if (!userKeyInfo) {
        res.json({
          success: true,
          data: {
            exchangeName: 'Binance Spot',
            isConnected: false,
            apiKeyMasked: 'لم يتم الربط بعد',
            permissions: [],
            canTradeSpot: false,
            canWithdraw: false
          },
          message: 'لم يتم ربط المفاتيح بعد'
        });
        return;
      }

      const apiKey = CryptoService.decrypt(userKeyInfo.apiKeyEncrypted);
      const apiSecret = CryptoService.decrypt(userKeyInfo.apiSecretEncrypted);

      const adapter = getAdapter(userKeyInfo.exchangeName);
      const status = await adapter.validateKeys(apiKey, apiSecret);

      res.json({ success: true, data: status, message: 'تم جلب حالة الاتصال الحقيقية' });
    } catch (error: any) {
      res.status(500).json({ success: false, data: null, message: error.message });
    }
  }

  static async getOpenPositions(req: AuthenticatedRequest, res: Response): Promise<void> {
    try {
      const userId = req.userId!;
      // Trigger a position monitoring check
      await PaperTradingEngine.monitorOpenPositions(userId);

      const openTrades = await db.getOpenPaperTrades(userId);
      const enrichedTrades = [];

      for (const trade of openTrades) {
        try {
          const adapter = getAdapter(trade.exchangeName || 'binance');
          const ticker = await adapter.getTicker(trade.symbol);
          const currentPrice = ticker.price;
          const pnl = ((currentPrice - trade.price) / trade.price) * trade.amount;
          const pnlPct = ((currentPrice - trade.price) / trade.price) * 100;

          enrichedTrades.push({
            ...trade,
            currentPrice,
            pnl,
            pnlPct
          });
        } catch (e) {
          enrichedTrades.push(trade);
        }
      }

      res.json({
        success: true,
        data: enrichedTrades,
        message: 'تم جلب الصفقات المفتوحة مع الأسعار الحية بنجاح'
      });
    } catch (error: any) {
      res.status(500).json({ success: false, data: null, message: error.message });
    }
  }

  static async closePosition(req: AuthenticatedRequest, res: Response): Promise<void> {
    try {
      const userId = req.userId!;
      const { tradeId } = req.body;

      if (!tradeId) {
        res.status(400).json({ success: false, data: null, message: 'معرف الصفقة (tradeId) مطلوب' });
        return;
      }

      const closed = await PaperTradingEngine.closePositionManually(userId, tradeId);
      if (closed) {
        res.json({ success: true, data: null, message: 'تم إغلاق الصفقة يدوياً بنجاح' });
      } else {
        res.status(400).json({ success: false, data: null, message: 'الصفقة غير موجودة أو مغلقة بالفعل' });
      }
    } catch (error: any) {
      res.status(500).json({ success: false, data: null, message: error.message });
    }
  }

  static async getTradeHistory(req: AuthenticatedRequest, res: Response): Promise<void> {
    try {
      const userId = req.userId!;
      const closedTrades = await db.getClosedPaperTrades(userId);
      res.json({
        success: true,
        data: closedTrades,
        message: 'تم جلب سجل الصفقات المغلقة بنجاح'
      });
    } catch (error: any) {
      res.status(500).json({ success: false, data: null, message: error.message });
    }
  }
}
