import { GeminiService } from '../ai/geminiService';
import { BinanceAdapter } from '../exchange/binanceAdapter';
import { OkxAdapter } from '../exchange/okxAdapter';
import { BybitAdapter } from '../exchange/bybitAdapter';
import { ServerRiskEngine, TradeSignal } from '../risk/serverRiskEngine';
import { DatabaseService } from '../storage/databaseService';

const db = DatabaseService.getInstance();
const gemini = new GeminiService();

export class PaperTradingEngine {
  static async runCycleForUser(userId: string, exchangeName: string = 'binance'): Promise<void> {
    const isKillSwitch = await db.isKillSwitchActive(userId);
    if (isKillSwitch) {
      console.log(`[Paper Trading] Kill switch active for user ${userId}. Skipping trade execution.`);
      return;
    }

    const userSettings = await db.getUserSettings(userId);
    const selectedCoins = await db.getSelectedCoins(userId, userSettings.selectedExchange || exchangeName);

    if (selectedCoins.length === 0) {
      console.log(`[Paper Trading] No coins selected for user ${userId} on ${userSettings.selectedExchange}.`);
      return;
    }

    let adapter: any = new BinanceAdapter();
    if (userSettings.selectedExchange === 'okx') adapter = new OkxAdapter();
    if (userSettings.selectedExchange === 'bybit') adapter = new BybitAdapter();

    const openTrades = await db.getOpenPaperTrades(userId);
    if (openTrades.length >= userSettings.maxOpenPositions) {
      console.log(`[Paper Trading] Maximum open positions (${userSettings.maxOpenPositions}) reached for user ${userId}.`);
      return;
    }

    for (const coin of selectedCoins) {
      try {
        const ticker = await adapter.getTicker(coin.symbol);
        let signal: TradeSignal;

        if (userSettings.tradingMode === 'MANUAL') {
          // Manual Target Price Evaluation
          const entryPrice = userSettings.entryPrice;
          const isTargetReached = entryPrice ? ticker.price <= entryPrice : true;
          const action = isTargetReached ? 'BUY' : 'HOLD';

          const stopLoss = ticker.price * (1 - (userSettings.stopLossPricePct / 100));
          const takeProfit = userSettings.exitPrice || (ticker.price * (1 + (userSettings.takeProfitPricePct / 100)));

          signal = {
            action: action,
            symbol: coin.symbol,
            confidence: 1.0,
            entryPrice: ticker.price,
            stopLoss: stopLoss,
            takeProfit: takeProfit,
            positionSize: userSettings.tradeAmountUsdt,
            riskRewardRatio: 2.0,
            reason: isTargetReached
              ? `تم الوصول لسعر الشراء المني المترقب ($${entryPrice || ticker.price})`
              : `السعر الحقيقي ($${ticker.price}) أعلى من سعر الشراء المترقب ($${entryPrice})`
          };
        } else {
          // Automated AI Analysis via Gemini
          signal = await gemini.analyzeMarket({
            symbol: ticker.symbol,
            price: ticker.price,
            high24h: ticker.high24h,
            low24h: ticker.low24h,
            volume24h: ticker.volume24h,
            priceChangePercent: ticker.priceChangePercent
          });

          // Override SL/TP with user's explicit risk rules if specified
          signal.stopLoss = ticker.price * (1 - (userSettings.stopLossPricePct / 100));
          signal.takeProfit = ticker.price * (1 + (userSettings.takeProfitPricePct / 100));
          signal.positionSize = userSettings.tradeAmountUsdt;
        }

        console.log(`[Trade Signal - ${userSettings.tradingMode}] ${coin.symbol}: ${signal.action} - ${signal.reason}`);

        // Validate via Server Risk Engine
        const riskCheck = ServerRiskEngine.validateTradeSignal(
          signal,
          userSettings.totalCapitalUsdt,
          0.0,
          0,
          isKillSwitch,
          openTrades.length
        );

        if (riskCheck.isAllowed && signal.action !== 'HOLD') {
          const tradeId = await db.recordPaperTrade(
            userId,
            userSettings.selectedExchange,
            coin.symbol,
            signal.action,
            ticker.price,
            userSettings.tradeAmountUsdt,
            signal.stopLoss,
            signal.takeProfit
          );
          console.log(`[Paper Trade Executed] ID: ${tradeId} | ${signal.action} ${coin.symbol} @ $${ticker.price} | Amount: $${userSettings.tradeAmountUsdt}`);
        } else {
          console.log(`[Paper Trade Filtered] Reason: ${riskCheck.rejectionReason || 'HOLD or Invalid signal'}`);
        }

      } catch (err: any) {
        console.error(`[Paper Trading Error] Symbol ${coin.symbol}:`, err.message);
      }
    }
  }
}
