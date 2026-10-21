import { Request, Response } from 'express';
import { BinanceAdapter } from '../services/exchange/binanceAdapter';
import { ExnessAdapter } from '../services/exchange/exnessAdapter';
import { OkxAdapter } from '../services/exchange/okxAdapter';
import { BybitAdapter } from '../services/exchange/bybitAdapter';
import { GeminiService } from '../services/ai/geminiService';
import { DatabaseService } from '../services/storage/databaseService';
import { AuthenticatedRequest } from '../middleware/authMiddleware';

const db = DatabaseService.getInstance();
const geminiService = new GeminiService();

export class MarketController {
  static async getTicker(req: Request, res: Response): Promise<void> {
    try {
      const rawSymbol = Array.isArray(req.params.symbol) ? req.params.symbol[0] : req.params.symbol;
      const symbol = (rawSymbol || 'BTCUSDT').toUpperCase();

      const exchangeQuery = (req.query.exchange as string || 'bybit').toLowerCase();
      let adapter: any = new BybitAdapter();
      if (exchangeQuery === 'binance') adapter = new BinanceAdapter();
      if (exchangeQuery === 'okx') adapter = new OkxAdapter();
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

  static async getKlines(req: Request, res: Response): Promise<void> {
    try {
      const rawSymbol = Array.isArray(req.params.symbol) ? req.params.symbol[0] : req.params.symbol;
      const symbol = (rawSymbol || 'BTCUSDT').toUpperCase();
      const interval = (req.query.interval as string || '15m').toLowerCase();
      const exchangeQuery = (req.query.exchange as string || 'bybit').toLowerCase();

      let adapter: any = new BybitAdapter();
      if (exchangeQuery === 'binance') adapter = new BinanceAdapter();
      if (exchangeQuery === 'okx') adapter = new OkxAdapter();
      if (symbol.includes('XAU')) adapter = new ExnessAdapter();

      const klines = await adapter.getKlines(symbol, interval, 100);

      res.json({
        success: true,
        data: klines,
        message: `تم جلب ${klines.length} شمعة يابانية لزوج ${symbol} بالفترة ${interval}`
      });
    } catch (error: any) {
      res.status(500).json({ success: false, data: null, message: error.message });
    }
  }

  static async getAvailableSymbols(req: Request, res: Response): Promise<void> {
    try {
      const exchangeNameStr = (req.params.exchangeName as string) || 'bybit';
      const exchangeName = exchangeNameStr.toLowerCase();

      let adapter: any = new BybitAdapter();
      if (exchangeName === 'binance') adapter = new BinanceAdapter();
      if (exchangeName === 'okx') adapter = new OkxAdapter();
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

  static async testGeminiAnalysis(req: Request, res: Response): Promise<void> {
    try {
      const rawSymbol = (req.params.symbol as string) || 'BTCUSDT';
      const symbol = rawSymbol.toUpperCase();
      const binance = new BinanceAdapter();
      const ticker = await binance.getTicker(symbol);

      const analysis = await geminiService.analyzeMarket({
        symbol: ticker.symbol,
        price: ticker.price,
        high24h: ticker.high24h,
        low24h: ticker.low24h,
        volume24h: ticker.volume24h,
        priceChangePercent: ticker.priceChangePercent
      });

      res.json({
        success: true,
        data: analysis,
        message: 'تم إجراء التحليل الحقيقي بنجاح من Gemini API'
      });
    } catch (error: any) {
      res.status(500).json({
        success: false,
        data: null,
        message: error.message
      });
    }
  }

  static async getSelectedCoins(req: AuthenticatedRequest, res: Response): Promise<void> {
    try {
      const userId = req.userId!;
      const exchangeName = (req.params.exchangeName as string) || 'bybit';
      const coins = await db.getSelectedCoins(userId, exchangeName);
      res.json({ success: true, data: coins, message: 'Selected coins loaded' });
    } catch (error: any) {
      res.status(500).json({ success: false, data: null, message: error.message });
    }
  }

  static async saveSelectedCoin(req: AuthenticatedRequest, res: Response): Promise<void> {
    try {
      const userId = req.userId!;
      const exchangeName = (req.params.exchangeName as string) || 'bybit';
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
