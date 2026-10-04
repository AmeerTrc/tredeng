export interface TradeSignal {
  action: 'BUY' | 'SELL' | 'HOLD';
  symbol: string;
  confidence: number;
  entryPrice: number;
  stopLoss: number;
  takeProfit: number;
  positionSize: number;
  riskRewardRatio: number;
  reason: string;
  warnings?: string[];
}

export interface RiskCheckResult {
  isAllowed: boolean;
  rejectionReason?: string;
  sanitizedSignal?: TradeSignal;
}

export class ServerRiskEngine {
  private static MAX_TRADE_RISK_PCT = 0.0025; // 0.25%
  private static MAX_DAILY_LOSS_PCT = 0.0100; // 1.00%
  private static MIN_CONFIDENCE = 0.70;       // 70%
  private static MAX_DAILY_TRADES = 3;

  static validateTradeSignal(
    signal: TradeSignal,
    portfolioBalanceUsdt: number,
    dailyLossUsdt: number,
    dailyTradesCount: number,
    isKillSwitchActive: boolean,
    openPositionsCount: number
  ): RiskCheckResult {
    // 1. Check Kill Switch
    if (isKillSwitchActive) {
      return { isAllowed: false, rejectionReason: 'تم تفعيل زر الطوارئ (Kill Switch Active).' };
    }

    // 2. Phase 1 Hard Enforcement: NO TRADES ALLOWED IN PHASE 1
    return {
      isAllowed: false,
      rejectionReason: 'التداول معطل حالياً في المرحلة الأولى (Phase 1 Protection).'
    };

    /*
    // Phase 2 Logic Preview:
    if (signal.action === 'HOLD') {
      return { isAllowed: false, rejectionReason: 'إشارة الـ AI هي HOLD (انتظار).' };
    }

    if (signal.confidence < this.MIN_CONFIDENCE) {
      return { isAllowed: false, rejectionReason: `نسبة ثقة الـ AI (${signal.confidence}) أقل من الحد المسموح (0.70).` };
    }

    if (!signal.stopLoss || signal.stopLoss <= 0) {
      return { isAllowed: false, rejectionReason: 'تم رفض الصفقة لعدم وجود أمر Stop Loss صريح.' };
    }

    if (dailyTradesCount >= this.MAX_DAILY_TRADES) {
      return { isAllowed: false, rejectionReason: 'تم الوصول للحد الأقصى لعدد الصفقات اليومية (3 صفقات).' };
    }

    if (dailyLossUsdt >= portfolioBalanceUsdt * this.MAX_DAILY_LOSS_PCT) {
      return { isAllowed: false, rejectionReason: 'تم الوصول للحد الأقصى للخسارة اليومية (1%).' };
    }

    if (openPositionsCount >= 1) {
      return { isAllowed: false, rejectionReason: 'يوجد صفقة مفتوحة بالفعل لزوج BTCUSDT.' };
    }

    return { isAllowed: true, sanitizedSignal: signal };
    */
  }
}
