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
  private genAI: GoogleGenerativeAI | null = null;

  constructor() {
    if (ENV.GEMINI_API_KEY) {
      this.genAI = new GoogleGenerativeAI(ENV.GEMINI_API_KEY);
    }
  }

  async analyzeMarket(snapshot: MarketDataSnapshot): Promise<TradeSignal> {
    if (!this.genAI || !ENV.GEMINI_API_KEY) {
      throw new Error("خطأ: مفتاح GEMINI_API_KEY غير مكوّن في الخادم. تعذر إجراء تحليل الذكاء الاصطناعي.");
    }

    try {
      // Using gemini-1.5-flash (fast and supported on standard Gemini API keys)
      const model = this.genAI.getGenerativeModel({ model: "gemini-1.5-flash" });

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

      Base your decision purely on technical indicators derived from the high, low, volume, and momentum. Be very conservative.
      `;

      const result = await model.generateContent(prompt);
      const text = result.response.text().replace(/\`\`\`json/g, '').replace(/\`\`\`/g, '').trim();

      const parsed = JSON.parse(text);

      if (!parsed.action || !['BUY', 'SELL', 'HOLD'].includes(parsed.action)) {
        throw new Error("استجابة غير صالحة من نموذج Gemini (صيغة القرار غير معروفة).");
      }

      return {
        action: parsed.action,
        symbol: snapshot.symbol,
        confidence: typeof parsed.confidence === 'number' ? parsed.confidence : 0.5,
        entryPrice: snapshot.price,
        stopLoss: typeof parsed.stopLoss === 'number' ? parsed.stopLoss : snapshot.price * 0.98,
        takeProfit: typeof parsed.takeProfit === 'number' ? parsed.takeProfit : snapshot.price * 1.02,
        positionSize: 0,
        riskRewardRatio: typeof parsed.riskRewardRatio === 'number' ? parsed.riskRewardRatio : 2.0,
        reason: parsed.reason || 'لم يتم تقديم سبب من الذكاء الاصطناعي.'
      };

    } catch (error: any) {
      console.error("Gemini AI Analysis Error:", error);
      throw new Error(`فشل تحليل الذكاء الاصطناعي (Gemini Error): ${error.message}`);
    }
  }
}
