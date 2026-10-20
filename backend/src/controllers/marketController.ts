import { Request, Response } from 'express';
import { BinanceAdapter } from '../services/exchange/binanceAdapter';
import { ExnessAdapter } from '../services/exchange/exnessAdapter';
import { OkxAdapter } from '../services/exchange/okxAdapter';
import { BybitAdapter } from '../services/exchange/bybitAdapter';
import { DatabaseService } from '../services/storage/databaseService';
import { AuthenticatedRequest } from '../middleware/authMiddleware';

const db = DatabaseService.getInstance();

export class MarketController {
  static async getTicker(req: Request, res: Response): Promise<void> {
    try {
      const rawSymbol = Array.isArray(req.params.symbol) ? req.params.symbol[0] : req.params.symbol;
      const symbol = (rawSymbol || 'BTCUSDT').toUpperCase();

      const exchangeQuery = (req.query.exchange as string || 'binance').toLowerCase();
      let adapter: any = new BinanceAdapter();
      if (exchangeQuery === 'okx') adapter = new OkxAdapter();
      if (exchangeQuery === 'bybit') adapter = new BybitAdapter();
      if (symbol.includes('XAU')) adapter = new ExnessAdapter();

      const ticker = await adapter.getTicker(symbol);

      res.json({
        success: true,
        data: ticker,
        message: 'تم جلب سعر السوق اللحظي'
      });
    } catch (error: any) {
      res.status(500).json({ success: false, data: null, message: error.message });
    }
  }

  static async getAvailableSymbols(req: Request, res: Response): Promise<void> {
    try {
      const exchangeNameStr = (req.params.exchangeName as string) || 'binance';
      const exchangeName = exchangeNameStr.toLowerCase();

      let adapter: any = new BinanceAdapter();
      if (exchangeName === 'okx') adapter = new OkxAdapter();
      if (exchangeName === 'bybit') adapter = new BybitAdapter();
      if (exchangeName === 'exness') adapter = new ExnessAdapter();

      const symbols = await adapter.getAvailableSymbols();

      res.json({
        success: true,
        data: symbols,
        message: 'تم جلب أزواج التداول المتاحة بنجاح'
      });
    } catch (error: any) {
      res.status(500).json({ success: false, data: null, message: error.message });
    }
  }

  static async getSelectedCoins(req: AuthenticatedRequest, res: Response): Promise<void> {
    try {
      const userId = req.userId!;
      const exchangeName = (req.params.exchangeName as string) || 'binance';
      const coins = await db.getSelectedCoins(userId, exchangeName);
      res.json({ success: true, data: coins, message: 'Selected coins loaded' });
    } catch (error: any) {
      res.status(500).json({ success: false, data: null, message: error.message });
    }
  }

  static async saveSelectedCoin(req: AuthenticatedRequest, res: Response): Promise<void> {
    try {
      const userId = req.userId!;
      const exchangeName = (req.params.exchangeName as string) || 'binance';
      const { symbol, isActive } = req.body;

      if (!symbol) {
        res.status(400).json({ success: false, data: null, message: 'Symbol is required' });
        return;
      }

      await db.saveSelectedCoin(userId, exchangeName, symbol, isActive);
      res.json({ success: true, data: null, message: 'Coin selection saved' });
    } catch (error: any) {
      res.status(500).json({ success: false, data: null, message: error.message });
    }
  }
}
