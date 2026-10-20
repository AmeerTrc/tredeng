import { GeminiService } from '../ai/geminiService';
import { BinanceAdapter } from '../exchange/binanceAdapter';
import { OkxAdapter } from '../exchange/okxAdapter';
import { BybitAdapter } from '../exchange/bybitAdapter';
import { ServerRiskEngine, TradeSignal } from '../risk/serverRiskEngine';
import { DatabaseService } from '../storage/databaseService';

const db = DatabaseService.getInstance();
const gemini = new GeminiService();

function getAdapter(exchangeName: string) {
  switch (exchangeName.toLowerCase()) {
    case 'okx': return new OkxAdapter();
    case 'bybit': return new BybitAdapter();
    default: return new BinanceAdapter();
  }
}

export class PaperTradingEngine {
  static async runCycleForUser(userId: string, exchangeName: string = 'binance'): Promise<void> {
    const isKillSwitch = await db.isKillSwitchActive(userId);
    if (isKillSwitch) {
      console.log(`[Paper Trading] Kill switch active for user ${userId}. Skipping trade execution.`);
      return;
    }

    const userSettings = await db.getUserSettings(userId);
    const targetExchange = userSettings.selectedExchange || exchangeName;

    // 1. Monitor existing open positions for SL/TP triggers
    await this.monitorOpenPositions(userId);

    const selectedCoins = await db.getSelectedCoins(userId, targetExchange);
    if (selectedCoins.length === 0) {
      console.log(`[Paper Trading] No coins selected for user ${userId} on ${targetExchange}.`);
      return;
    }

    const openTrades = await db.getOpenPaperTrades(userId);
    if (openTrades.length >= userSettings.maxOpenPositions) {
      console.log(`[Paper Trading] Maximum open positions (${userSettings.maxOpenPositions}) reached for user ${userId}.`);
      return;
    }

    const adapter = getAdapter(targetExchange);

    for (const coin of selectedCoins) {
      try {
        // Prevent opening duplicate active positions for the same coin
        const alreadyOpen = openTrades.some(t => t.symbol === coin.symbol);
        if (alreadyOpen) continue;

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
              ? `تم الوصول لسعر الشراء المترقب ($${entryPrice || ticker.price})`
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
            targetExchange,
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

  static async monitorOpenPositions(userId: string): Promise<void> {
    const openTrades = await db.getOpenPaperTrades(userId);
    for (const trade of openTrades) {
      try {
        const adapter = getAdapter(trade.exchangeName || 'binance');
        const ticker = await adapter.getTicker(trade.symbol);
        const livePrice = ticker.price;

        const pnl = ((livePrice - trade.price) / trade.price) * trade.amount;
        const fee = trade.amount * 0.001; // 0.1% spot fee estimate

        let shouldClose = false;
        let reason = '';

        if (livePrice <= trade.stopLoss) {
          shouldClose = true;
          reason = `Stop Loss Triggered @ $${livePrice}`;
        } else if (livePrice >= trade.takeProfit) {
          shouldClose = true;
          reason = `Take Profit Triggered @ $${livePrice}`;
        }

        if (shouldClose) {
          await db.closePaperTrade(trade.id, livePrice, pnl - fee, fee);
          console.log(`[Paper Trade Closed] ID: ${trade.id} | Symbol: ${trade.symbol} | ${reason} | Realized PnL: $${(pnl - fee).toFixed(2)}`);
        }
      } catch (err: any) {
        console.error(`[Monitor Position Error] Trade ID ${trade.id}:`, err.message);
      }
    }
  }

  static async closePositionManually(userId: string, tradeId: string): Promise<boolean> {
    const openTrades = await db.getOpenPaperTrades(userId);
    const trade = openTrades.find(t => t.id === tradeId);
    if (!trade) return false;

    const adapter = getAdapter(trade.exchangeName || 'binance');
    const ticker = await adapter.getTicker(trade.symbol);
    const livePrice = ticker.price;
    const pnl = ((livePrice - trade.price) / trade.price) * trade.amount;
    const fee = trade.amount * 0.001;

    await db.closePaperTrade(trade.id, livePrice, pnl - fee, fee);
    return true;
  }
}
