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
  private static MAX_TRADE_RISK_PCT = 0.05; // 5% max position risk
  private static MAX_DAILY_LOSS_PCT = 0.05; // 5% max daily loss
  private static MIN_CONFIDENCE = 0.60;      // 60% minimum confidence
  private static MAX_DAILY_TRADES = 10;     // Allow up to 10 trades per day

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

    // 2. Filter out HOLD action
    if (signal.action === 'HOLD') {
      return { isAllowed: false, rejectionReason: 'إشارة الـ AI هي HOLD (انتظار ومراقبة السوق).' };
    }

    // 3. Minimum AI Confidence
    if (signal.confidence < this.MIN_CONFIDENCE) {
      return { isAllowed: false, rejectionReason: `نسبة ثقة الـ AI (${(signal.confidence * 100).toFixed(0)}%) أقل من الحد الأدنى المسموح (${(this.MIN_CONFIDENCE * 100).toFixed(0)}%).` };
    }

    // 4. Stop Loss Safety Requirement
    if (!signal.stopLoss || signal.stopLoss <= 0) {
      return { isAllowed: false, rejectionReason: 'تم رفض الصفقة لعدم وجود أمر Stop Loss صريح لحماية رأس المال.' };
    }

    // 5. Daily Trades Limit
    if (dailyTradesCount >= this.MAX_DAILY_TRADES) {
      return { isAllowed: false, rejectionReason: `تم الوصول للحد الأقصى لعدد الصفقات اليومية (${this.MAX_DAILY_TRADES} صفقات).` };
    }

    // 6. Max Daily Loss Protection
    if (portfolioBalanceUsdt > 0 && dailyLossUsdt >= portfolioBalanceUsdt * this.MAX_DAILY_LOSS_PCT) {
      return { isAllowed: false, rejectionReason: 'تم الوصول للحد الأقصى للخسارة اليومية المسموح بها.' };
    }

    return { isAllowed: true, sanitizedSignal: signal };
  }
}
