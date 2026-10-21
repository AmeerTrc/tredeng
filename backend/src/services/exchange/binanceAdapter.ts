import axios from 'axios';
import crypto from 'crypto';
import { ExchangeAdapter, ExchangeStatus, PortfolioSummary, Ticker, Balance, Kline } from './exchangeAdapter';
import { ENV } from '../../config/env';

const BINANCE_AUTH_ENDPOINTS = [
  ENV.BINANCE_BASE_URL || 'https://api.binance.com',
  'https://api1.binance.com',
  'https://api2.binance.com',
  'https://api3.binance.com'
];

const BINANCE_PUBLIC_ENDPOINTS = [
  'https://data-api.binance.vision',
  ...BINANCE_AUTH_ENDPOINTS
];

export class BinanceAdapter implements ExchangeAdapter {
  getName(): string {
    return 'binance';
  }

  private generateSignature(queryString: string, apiSecret: string): string {
    return crypto
      .createHmac('sha256', apiSecret)
      .update(queryString)
      .digest('hex');
  }

  private async fetchAuthWithFallback(endpointPath: string, config: any = {}): Promise<any> {
    let lastError = null;
    for (const baseUrl of BINANCE_AUTH_ENDPOINTS) {
      try {
        const url = `${baseUrl}${endpointPath}`;
        const response = await axios.get(url, { ...config, timeout: 8000 });
        return response;
      } catch (err: any) {
        lastError = err;
        if (err.response && (err.response.status === 400 || err.response.status === 401 || err.response.status === 403)) {
          throw err;
        }
      }
    }
    throw lastError;
  }

  private async fetchWithFallback(endpointPath: string, config: any = {}): Promise<any> {
    let lastError = null;
    for (const baseUrl of BINANCE_PUBLIC_ENDPOINTS) {
      try {
        const url = `${baseUrl}${endpointPath}`;
        const response = await axios.get(url, { ...config, timeout: 5000 });
        return response;
      } catch (err: any) {
        lastError = err;
      }
    }
    throw lastError;
  }

  async validateKeys(apiKey: string, apiSecret: string): Promise<ExchangeStatus> {
    try {
      const timestamp = Date.now();
      const queryString = `timestamp=${timestamp}&recvWindow=60000`;
      const signature = this.generateSignature(queryString, apiSecret);

      const response = await this.fetchAuthWithFallback(`/api/v3/account?${queryString}&signature=${signature}`, {
        headers: { 'X-MBX-APIKEY': apiKey }
      });

      const canTrade = response.data.canTrade ?? true;
      const canWithdraw = response.data.canWithdraw ?? false;

      return {
        exchangeName: 'Binance Spot',
        isConnected: true,
        apiKeyMasked: `${apiKey.substring(0, 4)}...${apiKey.substring(apiKey.length - 4)}`,
        permissions: response.data.permissions || ['SPOT'],
        canTradeSpot: canTrade,
        canWithdraw: canWithdraw
      };
    } catch (error: any) {
      const errData = error.response?.data;
      const errorMsg = errData ? (errData.msg || JSON.stringify(errData)) : error.message;
      console.error('Binance API validation error:', errorMsg);
      return {
        exchangeName: 'Binance Spot',
        isConnected: false,
        apiKeyMasked: 'INVALID',
        permissions: [],
        canTradeSpot: false,
        canWithdraw: false,
        errorMessage: `Binance: ${errorMsg}`
      };
    }
  }

  async getPortfolio(apiKey: string, apiSecret: string): Promise<PortfolioSummary> {
    try {
      const timestamp = Date.now();
      const queryString = `timestamp=${timestamp}`;
      const signature = this.generateSignature(queryString, apiSecret);

      const response = await this.fetchWithFallback(`/api/v3/account?${queryString}&signature=${signature}`, {
        headers: { 'X-MBX-APIKEY': apiKey }
      });

      const rawBalances = response.data.balances || [];
      const ticker = await this.getTicker('BTCUSDT');

      let totalBalanceUsdt = 0;
      let availableBalanceUsdt = 0;
      let inOrdersUsdt = 0;

      const balances: Balance[] = rawBalances
        .map((b: any) => {
          const free = parseFloat(b.free);
          const locked = parseFloat(b.locked);
          const total = free + locked;
          return { asset: b.asset, free, locked, total };
        })
        .filter((b: Balance) => b.total > 0.0001);

      for (const b of balances) {
        if (b.asset === 'USDT') {
          totalBalanceUsdt += b.total;
          availableBalanceUsdt += b.free;
          inOrdersUsdt += b.locked;
        } else if (b.asset === 'BTC') {
          totalBalanceUsdt += b.total * ticker.price;
          availableBalanceUsdt += b.free * ticker.price;
          inOrdersUsdt += b.locked * ticker.price;
        }
      }

      return {
        totalBalanceUsdt,
        availableBalanceUsdt,
        inOrdersUsdt,
        balances
      };
    } catch (error: any) {
      const errDetails = error.response?.data?.msg || error.message;
      throw new Error(`فشل جلب رصيد الحساب من Binance: ${errDetails}`);
    }
  }

  async getTicker(symbol: string = 'BTCUSDT'): Promise<Ticker> {
    try {
      const response = await this.fetchWithFallback(`/api/v3/ticker/24hr?symbol=${symbol}`);

      return {
        symbol: response.data.symbol,
        price: parseFloat(response.data.lastPrice),
        high24h: parseFloat(response.data.highPrice),
        low24h: parseFloat(response.data.lowPrice),
        volume24h: parseFloat(response.data.volume),
        priceChangePercent: parseFloat(response.data.priceChangePercent),
        timestamp: Date.now()
      };
    } catch (error: any) {
      const errDetails = error.response?.data?.msg || error.message;
      throw new Error(`فشل جلب سعر زوج ${symbol} اللحظي من Binance: ${errDetails}`);
    }
  }

  async getAvailableSymbols(): Promise<string[]> {
    try {
      const response = await this.fetchWithFallback(`/api/v3/exchangeInfo`);
      const symbols = response.data.symbols
        .filter((s: any) => s.status === 'TRADING' && s.isSpotTradingAllowed)
        .map((s: any) => s.symbol);
      return symbols;
    } catch (error: any) {
      throw new Error(`فشل جلب أزواج التداول من Binance: ${error.message}`);
    }
  }

  async getKlines(symbol: string = 'BTCUSDT', interval: string = '15m', limit: number = 100): Promise<Kline[]> {
    try {
      const response = await this.fetchWithFallback(`/api/v3/klines?symbol=${symbol}&interval=${interval}&limit=${limit}`);
      const rawList = response.data || [];

      const klines: Kline[] = rawList.map((item: any) => ({
        openTime: item[0],
        open: parseFloat(item[1]),
        high: parseFloat(item[2]),
        low: parseFloat(item[3]),
        close: parseFloat(item[4]),
        volume: parseFloat(item[5])
      }));

      return klines;
    } catch (error: any) {
      throw new Error(`فشل جلب الشموع اليابانية لزوج ${symbol} من Binance: ${error.message}`);
    }
  }
}
