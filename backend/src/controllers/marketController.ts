import { Request, Response } from 'express';
import { BinanceAdapter } from '../services/exchange/binanceAdapter';
import { ExnessAdapter } from '../services/exchange/exnessAdapter';

export class MarketController {
  static async getTicker(req: Request, res: Response): Promise<void> {
    try {
      const symbol = (req.params.symbol || 'BTCUSDT').toUpperCase();
      const adapter = symbol.includes('XAU') ? new ExnessAdapter() : new BinanceAdapter();
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
}
