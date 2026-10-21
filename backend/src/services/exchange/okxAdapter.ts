import axios from 'axios';
import crypto from 'crypto';
import { ExchangeAdapter, ExchangeStatus, PortfolioSummary, Ticker, Balance, Kline } from './exchangeAdapter';

export class OkxAdapter implements ExchangeAdapter {
  getName(): string {
    return 'okx';
  }

  async validateKeys(apiKey: string, apiSecret: string): Promise<ExchangeStatus> {
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

  async getKlines(symbol: string = 'BTC-USDT', interval: string = '15m', limit: number = 100): Promise<Kline[]> {
    const barMap: Record<string, string> = {
      '1m': '1m', '5m': '5m', '15m': '15m', '30m': '30m',
      '1h': '1H', '4h': '4H', '1d': '1D', '1w': '1W'
    };
    const okxBar = barMap[interval] || '15m';

    try {
      const response = await axios.get(`https://www.okx.com/api/v5/market/candles?instId=${symbol}&bar=${okxBar}&limit=${limit}`, { timeout: 8000 });
      const rawList = response.data?.data || [];

      const klines: Kline[] = rawList.reverse().map((item: any) => ({
        openTime: parseInt(item[0], 10),
        open: parseFloat(item[1]),
        high: parseFloat(item[2]),
        low: parseFloat(item[3]),
        close: parseFloat(item[4]),
        volume: parseFloat(item[5])
      }));

      return klines;
    } catch (error: any) {
      throw new Error(`فشل جلب الشموع اليابانية من OKX: ${error.message}`);
    }
  }
}
