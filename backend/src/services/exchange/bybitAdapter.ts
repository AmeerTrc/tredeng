import axios from 'axios';
import crypto from 'crypto';
import { ExchangeAdapter, ExchangeStatus, PortfolioSummary, Ticker, Balance } from './exchangeAdapter';

export class BybitAdapter implements ExchangeAdapter {
  getName(): string {
    return 'bybit';
  }

  async validateKeys(apiKey: string, apiSecret: string): Promise<ExchangeStatus> {
    // Basic mock validation for Bybit
    return {
      exchangeName: 'Bybit Spot',
      isConnected: apiKey.length > 5,
      apiKeyMasked: apiKey ? `${apiKey.substring(0, 4)}...` : 'INVALID',
      permissions: ['SPOT'],
      canTradeSpot: true,
      canWithdraw: false
    };
  }

  async getPortfolio(apiKey: string, apiSecret: string): Promise<PortfolioSummary> {
    return {
      totalBalanceUsdt: 0,
      availableBalanceUsdt: 0,
      inOrdersUsdt: 0,
      balances: []
    };
  }

  async getTicker(symbol: string = 'BTCUSDT'): Promise<Ticker> {
    try {
      const response = await axios.get(`https://api.bybit.com/v5/market/tickers?category=spot&symbol=${symbol}`, {
        timeout: 5000
      });

      const data = response.data.result.list[0];
      return {
        symbol: data.symbol,
        price: parseFloat(data.lastPrice),
        high24h: parseFloat(data.highPrice24h),
        low24h: parseFloat(data.lowPrice24h),
        volume24h: parseFloat(data.volume24h),
        priceChangePercent: parseFloat(data.price24hPcnt) * 100,
        timestamp: Date.now()
      };
    } catch (error: any) {
      throw new Error(`فشل جلب سعر زوج ${symbol} من Bybit`);
    }
  }

  async getAvailableSymbols(): Promise<string[]> {
    try {
      const response = await axios.get(`https://api.bybit.com/v5/market/instruments-info?category=spot`, {
        timeout: 8000
      });
      const symbols = response.data.result.list.map((s: any) => s.symbol);
      return symbols;
    } catch (error: any) {
      throw new Error(`فشل جلب أزواج التداول من Bybit`);
    }
  }
}