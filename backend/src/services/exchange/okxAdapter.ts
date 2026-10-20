import axios from 'axios';
import crypto from 'crypto';
import { ExchangeAdapter, ExchangeStatus, PortfolioSummary, Ticker, Balance } from './exchangeAdapter';

export class OkxAdapter implements ExchangeAdapter {
  getName(): string {
    return 'okx';
  }

  // Note: For a complete OKX implementation, you need apiKey, apiSecret, and apiPassphrase.
  // For this project structure, we will assume the passphrase is concatenated or provided separately.
  // Since we only have apiKey and apiSecret in DB currently, we might need to adjust or just mock OKX for now.
  // We'll provide a functional structure that validates if valid keys are provided.

  async validateKeys(apiKey: string, apiSecret: string): Promise<ExchangeStatus> {
    // Basic mock validation for OKX since it requires a passphrase which our DB schema doesn't have yet.
    return {
      exchangeName: 'OKX Spot',
      isConnected: apiKey.length > 5,
      apiKeyMasked: apiKey ? `${apiKey.substring(0, 4)}...` : 'INVALID',
      permissions: ['SPOT'],
      canTradeSpot: true,
      canWithdraw: false
    };
  }

  async getPortfolio(apiKey: string, apiSecret: string): Promise<PortfolioSummary> {
    // Placeholder OKX portfolio
    return {
      totalBalanceUsdt: 0,
      availableBalanceUsdt: 0,
      inOrdersUsdt: 0,
      balances: []
    };
  }

  async getTicker(symbol: string = 'BTC-USDT'): Promise<Ticker> {
    try {
      const response = await axios.get(`https://www.okx.com/api/v5/market/ticker?instId=${symbol}`, {
        timeout: 5000
      });

      const data = response.data.data[0];
      return {
        symbol: data.instId,
        price: parseFloat(data.last),
        high24h: parseFloat(data.high24h),
        low24h: parseFloat(data.low24h),
        volume24h: parseFloat(data.vol24h),
        priceChangePercent: ((parseFloat(data.last) - parseFloat(data.open24h)) / parseFloat(data.open24h)) * 100,
        timestamp: Date.now()
      };
    } catch (error: any) {
      throw new Error(`فشل جلب سعر زوج ${symbol} من OKX`);
    }
  }

  async getAvailableSymbols(): Promise<string[]> {
    try {
      const response = await axios.get(`https://www.okx.com/api/v5/public/instruments?instType=SPOT`, {
        timeout: 8000
      });
      const symbols = response.data.data.map((s: any) => s.instId);
      return symbols;
    } catch (error: any) {
      throw new Error(`فشل جلب أزواج التداول من OKX`);
    }
  }
}
