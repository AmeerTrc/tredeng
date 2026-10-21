import { GoogleGenerativeAI } from '@google/generative-ai';
import { ENV } from '../../config/env';
import { TradeSignal } from '../risk/serverRiskEngine';

export interface MarketDataSnapshot {
  symbol: string;
  price: number;
  high24h: number;
  low24h: number;
  volume24h: number;
  priceChangePercent: number;
}

export class GeminiService {
  async analyzeMarket(snapshot: MarketDataSnapshot): Promise<TradeSignal> {
    const apiKey = ENV.GEMINI_API_KEY || process.env.GEMINI_API_KEY;

    if (!apiKey || apiKey.trim().length === 0) {
      return this.analyzeWithTechnicalStrategy(snapshot, "خوارزمية الذكاء الاصطناعي الفني (وضع التحليل الذاتي المستقل)");
    }

    try {
      const genAI = new GoogleGenerativeAI(apiKey.trim());
      const model = genAI.getGenerativeModel({ model: "gemini-1.5-flash" });

      const prompt = `
      You are an expert algorithmic trading AI. Analyze the following 24h market snapshot for ${snapshot.symbol}.
      Current Price: ${snapshot.price}
      24h High: ${snapshot.high24h}
      24h Low: ${snapshot.low24h}
      24h Volume: ${snapshot.volume24h}
      24h Price Change: ${snapshot.priceChangePercent}%

      Return ONLY a raw JSON object with no markdown formatting or code blocks. The JSON must strictly match this structure:
      {
        "action": "BUY" | "SELL" | "HOLD",
        "confidence": number between 0.0 and 1.0,
        "reason": "short explanation in Arabic",
        "stopLoss": recommended stop loss price (number),
        "takeProfit": recommended take profit price (number),
        "riskRewardRatio": calculated risk/reward ratio (number)
      }

      Base your decision purely on technical indicators derived from the high, low, volume, and momentum.
      `;

      const result = await model.generateContent(prompt);
      const text = result.response.text().replace(/```json/g, '').replace(/```/g, '').trim();

      const parsed = JSON.parse(text);

      if (!parsed.action || !['BUY', 'SELL', 'HOLD'].includes(parsed.action)) {
        throw new Error("استجابة غير صالحة من نموذج Gemini (صيغة القرار غير معروفة).");
      }

      return {
        action: parsed.action,
        symbol: snapshot.symbol,
        confidence: typeof parsed.confidence === 'number' ? parsed.confidence : 0.75,
        entryPrice: snapshot.price,
        stopLoss: typeof parsed.stopLoss === 'number' ? parsed.stopLoss : snapshot.price * 0.98,
        takeProfit: typeof parsed.takeProfit === 'number' ? parsed.takeProfit : snapshot.price * 1.03,
        positionSize: 0,
        riskRewardRatio: typeof parsed.riskRewardRatio === 'number' ? parsed.riskRewardRatio : 2.0,
        reason: parsed.reason || 'إشارة مؤكدة من الذكاء الاصطناعي Gemini.'
      };

    } catch (error: any) {
      console.warn("Gemini AI API fallback to technical algorithm:", error.message || error);
      return this.analyzeWithTechnicalStrategy(snapshot, `خوارزمية الزخم الفني الذاتي (بديل Gemini: ${error.message || 'شبكة'})`);
    }
  }

  private analyzeWithTechnicalStrategy(snapshot: MarketDataSnapshot, sourceDesc: string): TradeSignal {
    const range = Math.max(0.0001, snapshot.high24h - snapshot.low24h);
    const rangePosition = (snapshot.price - snapshot.low24h) / range;
    const change = snapshot.priceChangePercent;

    let action: 'BUY' | 'SELL' | 'HOLD' = 'HOLD';
    let confidence = 0.65;
    let reason = `${sourceDesc}: مراقبة واستقرار السعر ضمن النطاق الطبيعي`;

    // Buy Condition: Price is near the support (lower 40% of 24h range) with recovery momentum
    if (rangePosition <= 0.40 && change > -6.0) {
      action = 'BUY';
      confidence = 0.82;
      reason = `${sourceDesc}: ارتداد سعري إيجابي من منطقة الدعم اليومي (${(rangePosition * 100).toFixed(0)}%) مع زخم شرائي صاعد`;
    } else if (rangePosition >= 0.85 || change > 8.0) {
      action = 'SELL';
      confidence = 0.78;
      reason = `${sourceDesc}: تشبع شرائي بالقرب من القمة اليومية (${(rangePosition * 100).toFixed(0)}%) يفضل جني الأرباح`;
    } else if (change > 1.5 && rangePosition < 0.70) {
      action = 'BUY';
      confidence = 0.75;
      reason = `${sourceDesc}: اتجاه صاعد معتدل (+${change.toFixed(1)}%) واختراق إيجابي`;
    }

    const stopLoss = snapshot.price * 0.98;
    const takeProfit = snapshot.price * 1.03;

    return {
      action,
      symbol: snapshot.symbol,
      confidence,
      entryPrice: snapshot.price,
      stopLoss,
      takeProfit,
      positionSize: 0,
      riskRewardRatio: 2.0,
      reason
    };
  }
}
