import { GeminiService } from '../ai/geminiService';
import { BinanceAdapter } from '../exchange/binanceAdapter';
import { OkxAdapter } from '../exchange/okxAdapter';
import { BybitAdapter } from '../exchange/bybitAdapter';
import { ServerRiskEngine } from '../risk/serverRiskEngine';
import { DatabaseService } from '../storage/databaseService';

const db = DatabaseService.getInstance();
const gemini = new GeminiService();

export class PaperTradingEngine {
  static async evaluateUserPortfolios(): Promise<void> {
    console.log("[Paper Trading Engine] Running market evaluation cycle...");
    // For every user (in a real app, you'd iterate over all active users)
    // We will get all users from DB or run for a demo cycle.
  }

  static async runCycleForUser(userId: string, exchangeName: string = 'binance'): Promise<void> {
    const isKillSwitch = await db.isKillSwitchActive(userId);
    if (isKillSwitch) {
      console.log(`[Paper Trading] Kill switch active for user ${userId}. Skipping trade execution.`);
      return;
    }

    const selectedCoins = await db.getSelectedCoins(userId, exchangeName);
    if (selectedCoins.length === 0) {
      console.log(`[Paper Trading] No coins selected for user ${userId} on ${exchangeName}.`);
      return;
    }

    let adapter: any = new BinanceAdapter();
    if (exchangeName === 'okx') adapter = new OkxAdapter();
    if (exchangeName === 'bybit') adapter = new BybitAdapter();

    for (const coin of selectedCoins) {
      try {
        const ticker = await adapter.getTicker(coin.symbol);

        // 1. Analyze with Gemini AI
        const signal = await gemini.analyzeMarket({
          symbol: ticker.symbol,
          price: ticker.price,
          high24h: ticker.high24h,
          low24h: ticker.low24h,
          volume24h: ticker.volume24h,
          priceChangePercent: ticker.priceChangePercent
        });

        console.log(`[AI Signal] ${coin.symbol}: ${signal.action} (Confidence: ${signal.confidence}) - ${signal.reason}`);

        // 2. Validate with Risk Engine
        const openTrades = await db.getOpenPaperTrades(userId);
        const riskCheck = ServerRiskEngine.validateTradeSignal(
          signal,
          1000.0, // Mock paper portfolio balance 1000 USDT
          0.0,
          0,
          isKillSwitch,
          openTrades.length
        );

        if (riskCheck.isAllowed && signal.action !== 'HOLD') {
          // Record Paper Trade
          const tradeId = await db.recordPaperTrade(
            userId,
            exchangeName,
            coin.symbol,
            signal.action,
            ticker.price,
            10.0, // Default $10 allocation for paper trading
            signal.stopLoss,
            signal.takeProfit
          );
          console.log(`[Paper Trade Executed] ID: ${tradeId} | ${signal.action} ${coin.symbol} @ $${ticker.price}`);
        } else {
          console.log(`[Paper Trade Rejected] Reason: ${riskCheck.rejectionReason || 'HOLD or Invalid signal'}`);
        }

      } catch (err: any) {
        console.error(`[Paper Trading Error] Symbol ${coin.symbol}:`, err.message);
      }
    }
  }
}
