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

      const cleanKey = apiKey.trim();
      const cleanSecret = apiSecret.trim();

      if (cleanKey.length < 8 || cleanSecret.length < 8) {
        res.status(400).json({ success: false, data: null, message: 'طول مفاتيح API غير مكتمل' });
        return;
      }

      const selectedExchange = (exchangeName || 'binance').trim().toLowerCase();
      if (!['binance', 'bybit', 'okx'].includes(selectedExchange)) {
        res.status(400).json({ success: false, data: null, message: 'منصة غير مدعومة. المنصات المدعومة هي Binance و Bybit و OKX.' });
        return;
      }

      // 1. ALWAYS Encrypt & Save Keys IMMEDIATELY to SQLite Database first!
      const encryptedKey = CryptoService.encrypt(cleanKey);
      const encryptedSecret = CryptoService.encrypt(cleanSecret);
      await db.saveExchangeKeys(userId, selectedExchange, encryptedKey, encryptedSecret);

      // 2. Test live exchange connection asynchronously
      const adapter = getAdapter(selectedExchange);
      let status;
      try {
        status = await adapter.validateKeys(cleanKey, cleanSecret);
      } catch (e: any) {
        status = {
          exchangeName: `${selectedExchange.toUpperCase()} Spot`,
          isConnected: false,
          apiKeyMasked: `${cleanKey.substring(0, 4)}...${cleanKey.substring(cleanKey.length - 4)}`,
          permissions: [],
          canTradeSpot: true,
          canWithdraw: false,
          errorMessage: `تم حفظ المفتاح! ملاحظة: ${e.message}`
        };
      }

      // 3. Return HTTP 200 SUCCESS - Keys are saved, encrypted, and registered!
      res.json({
        success: true,
        data: status,
        message: status.isConnected
          ? 'تم حفظ وتشفير مفاتيح API والتحقق من اتصالها بنجاح!'
          : 'تم حفظ وتشفير المفاتيح بنجاح في قاعدة البيانات! (التداول التجريبي يعمل بمرونة).'
      });
    } catch (error: any) {
      res.status(500).json({ success: false, data: null, message: error.message });
    }
  }

  static async getBalance(req: AuthenticatedRequest, res: Response): Promise<void> {
    try {
      const userId = req.userId!;
      const requestedExchange = ((req.query.exchangeName as string) || '').trim().toLowerCase();

      let userKeyInfo = requestedExchange ? await db.getExchangeKeys(userId, requestedExchange) : null;
      if (!userKeyInfo) {
        const allKeys = await db.getAllExchangeKeys(userId);
        if (allKeys.length > 0) {
          userKeyInfo = allKeys[0];
        }
      }

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
      const requestedExchange = ((req.query.exchangeName as string) || '').trim().toLowerCase();

      let userKeyInfo = requestedExchange ? await db.getExchangeKeys(userId, requestedExchange) : null;
      if (!userKeyInfo) {
        const allKeys = await db.getAllExchangeKeys(userId);
        if (allKeys.length > 0) {
          userKeyInfo = allKeys[0];
        }
      }

      if (!userKeyInfo) {
        res.json({
          success: true,
          data: {
            exchangeName: requestedExchange || 'binance',
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
