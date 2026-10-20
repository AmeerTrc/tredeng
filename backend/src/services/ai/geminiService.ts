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
  // In a real app, this would also contain historical OHLCV data for RSI/MACD calculations
  // For now, we will pass the 24h ticker data to simulate market context.
}

export class GeminiService {
  private genAI: GoogleGenerativeAI | null = null;

  constructor() {
    if (ENV.GEMINI_API_KEY) {
      this.genAI = new GoogleGenerativeAI(ENV.GEMINI_API_KEY);
    }
  }

  async analyzeMarket(snapshot: MarketDataSnapshot): Promise<TradeSignal> {
    if (!this.genAI) {
      console.warn("GEMINI_API_KEY is not set. Using fallback mock analysis.");
      return this.getMockAnalysis(snapshot);
    }

    try {
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

      return {
        action: parsed.action || 'HOLD',
        symbol: snapshot.symbol,
        confidence: parsed.confidence || 0.5,
        entryPrice: snapshot.price,
        stopLoss: parsed.stopLoss || snapshot.price * 0.98,
        takeProfit: parsed.takeProfit || snapshot.price * 1.02,
        positionSize: 0, // Calculated later by Risk Engine
        riskRewardRatio: parsed.riskRewardRatio || 2.0,
        reason: parsed.reason || 'No reason provided by AI.'
      };

    } catch (error) {
      console.error("Gemini AI Analysis Error:", error);
      return this.getMockAnalysis(snapshot);
    }
  }

  private getMockAnalysis(snapshot: MarketDataSnapshot): TradeSignal {
    const isUptrend = snapshot.priceChangePercent > 0;
    return {
      action: isUptrend ? 'BUY' : 'HOLD',
      symbol: snapshot.symbol,
      confidence: 0.75,
      entryPrice: snapshot.price,
      stopLoss: snapshot.price * 0.98,
      takeProfit: snapshot.price * 1.02,
      positionSize: 0,
      riskRewardRatio: 2.0,
      reason: 'تحليل افتراضي: السوق في مسار صاعد (التكامل مع Gemini بانتظار مفتاح API)'
    };
  }
}
