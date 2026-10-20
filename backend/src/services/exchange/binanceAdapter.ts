import axios from 'axios';
import crypto from 'crypto';
import { ExchangeAdapter, ExchangeStatus, PortfolioSummary, Ticker, Balance } from './exchangeAdapter';
import { ENV } from '../../config/env';

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

  async validateKeys(apiKey: string, apiSecret: string): Promise<ExchangeStatus> {
    try {
      const timestamp = Date.now();
      const queryString = `timestamp=${timestamp}`;
      const signature = this.generateSignature(queryString, apiSecret);

      const response = await axios.get(`${ENV.BINANCE_BASE_URL}/api/v3/account?${queryString}&signature=${signature}`, {
        headers: {
          'X-MBX-APIKEY': apiKey
        },
        timeout: 8000
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
      console.error('Binance API validation error:', error.response?.data || error.message);
      return {
        exchangeName: 'Binance Spot',
        isConnected: false,
        apiKeyMasked: 'INVALID',
        permissions: [],
        canTradeSpot: false,
        canWithdraw: false
      };
    }
  }

  async getPortfolio(apiKey: string, apiSecret: string): Promise<PortfolioSummary> {
    try {
      const timestamp = Date.now();
      const queryString = `timestamp=${timestamp}`;
      const signature = this.generateSignature(queryString, apiSecret);

      const response = await axios.get(`${ENV.BINANCE_BASE_URL}/api/v3/account?${queryString}&signature=${signature}`, {
        headers: {
          'X-MBX-APIKEY': apiKey
        },
        timeout: 8000
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
      const response = await axios.get(`${ENV.BINANCE_BASE_URL}/api/v3/ticker/24hr?symbol=${symbol}`, {
        timeout: 5000
      });

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
      const response = await axios.get(`${ENV.BINANCE_BASE_URL}/api/v3/exchangeInfo`, {
        timeout: 8000
      });
      // Return only SPOT trading pairs that are trading
      const symbols = response.data.symbols
        .filter((s: any) => s.status === 'TRADING' && s.isSpotTradingAllowed)
        .map((s: any) => s.symbol);
      return symbols;
    } catch (error: any) {
      throw new Error(`فشل جلب أزواج التداول من Binance: ${error.message}`);
    }
  }
}
