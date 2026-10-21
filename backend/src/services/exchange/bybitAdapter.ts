import axios from 'axios';
import crypto from 'crypto';
import { ExchangeAdapter, ExchangeStatus, PortfolioSummary, Ticker, Balance } from './exchangeAdapter';

const BYBIT_BASE_URL = 'https://api.bybit.com';

export class BybitAdapter implements ExchangeAdapter {
  getName(): string {
    return 'bybit';
  }

  private generateSignature(timestamp: number, apiKey: string, recvWindow: number, queryString: string, apiSecret: string): string {
    const originString = `${timestamp}${apiKey}${recvWindow}${queryString}`;
    return crypto
      .createHmac('sha256', apiSecret)
      .update(originString)
      .digest('hex');
  }

  private getAuthHeaders(apiKey: string, apiSecret: string, queryString: string = '') {
    const timestamp = Date.now();
    const recvWindow = 10000;
    const signature = this.generateSignature(timestamp, apiKey, recvWindow, queryString, apiSecret);

    return {
      'X-BAPI-API-KEY': apiKey,
      'X-BAPI-TIMESTAMP': timestamp.toString(),
      'X-BAPI-RECV-WINDOW': recvWindow.toString(),
      'X-BAPI-SIGN': signature,
      'Content-Type': 'application/json'
    };
  }

  async validateKeys(apiKey: string, apiSecret: string): Promise<ExchangeStatus> {
    const cleanKey = apiKey.trim();
    const cleanSecret = apiSecret.trim();

    try {
      // Primary Check: Try user API key info endpoint
      const headers = this.getAuthHeaders(cleanKey, cleanSecret, '');
      const response = await axios.get(`${BYBIT_BASE_URL}/v5/user/get-api-key-information`, {
        headers,
        timeout: 8000
      });

      if (response.data && response.data.retCode === 0) {
        const info = response.data.result || {};
        const isReadOnly = info.readOnly === 1;
        const permissionsMap = info.permissions || {};
        const walletPermissions = permissionsMap.Wallet || [];
        const canWithdraw = walletPermissions.includes('Withdraw');

        return {
          exchangeName: 'Bybit Spot',
          isConnected: true,
          apiKeyMasked: `${cleanKey.substring(0, 4)}...${cleanKey.substring(cleanKey.length - 4)}`,
          permissions: Object.keys(permissionsMap),
          canTradeSpot: !isReadOnly,
          canWithdraw: canWithdraw
        };
      }

      // Secondary Check 1: Try UNIFIED wallet balance
      const unifiedQuery = 'accountType=UNIFIED';
      const unifiedHeaders = this.getAuthHeaders(cleanKey, cleanSecret, unifiedQuery);
      const unifiedResponse = await axios.get(`${BYBIT_BASE_URL}/v5/account/wallet-balance?${unifiedQuery}`, {
        headers: unifiedHeaders,
        timeout: 8000
      });

      if (unifiedResponse.data && unifiedResponse.data.retCode === 0) {
        return {
          exchangeName: 'Bybit Spot',
          isConnected: true,
          apiKeyMasked: `${cleanKey.substring(0, 4)}...${cleanKey.substring(cleanKey.length - 4)}`,
          permissions: ['SPOT', 'UNIFIED'],
          canTradeSpot: true,
          canWithdraw: false
        };
      }

      // Secondary Check 2: Try Classic SPOT wallet balance
      const spotQuery = 'accountType=SPOT';
      const spotHeaders = this.getAuthHeaders(cleanKey, cleanSecret, spotQuery);
      const spotResponse = await axios.get(`${BYBIT_BASE_URL}/v5/account/wallet-balance?${spotQuery}`, {
        headers: spotHeaders,
        timeout: 8000
      });

      if (spotResponse.data && spotResponse.data.retCode === 0) {
        return {
          exchangeName: 'Bybit Spot',
          isConnected: true,
          apiKeyMasked: `${cleanKey.substring(0, 4)}...${cleanKey.substring(cleanKey.length - 4)}`,
          permissions: ['SPOT'],
          canTradeSpot: true,
          canWithdraw: false
        };
      }

      const errMsg = response.data?.retMsg || unifiedResponse.data?.retMsg || spotResponse.data?.retMsg || 'فشل المصادقة مع Bybit';
      return {
        exchangeName: 'Bybit Spot',
        isConnected: false,
        apiKeyMasked: 'INVALID',
        permissions: [],
        canTradeSpot: false,
        canWithdraw: false,
        errorMessage: `Bybit: ${errMsg}`
      };

    } catch (error: any) {
      const msg = error.response?.data?.retMsg || error.message || 'خطأ اتصال بالشبكة';
      console.error('Bybit validation exception:', msg);
      return {
        exchangeName: 'Bybit Spot',
        isConnected: false,
        apiKeyMasked: 'INVALID',
        permissions: [],
        canTradeSpot: false,
        canWithdraw: false,
        errorMessage: `Bybit: ${msg}`
      };
    }
  }

  async getPortfolio(apiKey: string, apiSecret: string): Promise<PortfolioSummary> {
    const cleanKey = apiKey.trim();
    const cleanSecret = apiSecret.trim();

    try {
      // Try UNIFIED account balance first
      let queryString = 'accountType=UNIFIED';
      let headers = this.getAuthHeaders(cleanKey, cleanSecret, queryString);

      let response = await axios.get(`${BYBIT_BASE_URL}/v5/account/wallet-balance?${queryString}`, {
        headers,
        timeout: 8000
      });

      // Fallback to SPOT if UNIFIED fails
      if (response.data.retCode !== 0) {
        queryString = 'accountType=SPOT';
        headers = this.getAuthHeaders(cleanKey, cleanSecret, queryString);
        response = await axios.get(`${BYBIT_BASE_URL}/v5/account/wallet-balance?${queryString}`, {
          headers,
          timeout: 8000
        });
      }

      if (response.data.retCode !== 0) {
        throw new Error(`Bybit API Error: ${response.data.retMsg}`);
      }

      const accountList = response.data.result?.list || [];
      let totalBalanceUsdt = 0;
      let availableBalanceUsdt = 0;
      const balances: Balance[] = [];

      if (accountList.length > 0) {
        const account = accountList[0];
        totalBalanceUsdt = parseFloat(account.totalEquity || '0');
        availableBalanceUsdt = parseFloat(account.totalAvailableBalance || account.totalEquity || '0');

        const coinList = account.coin || [];
        for (const c of coinList) {
          const free = parseFloat(c.walletBalance || '0');
          const locked = parseFloat(c.locked || '0');
          const total = parseFloat(c.equity || c.walletBalance || '0');
          if (total > 0.0001) {
            balances.push({
              asset: c.coin,
              free,
              locked,
              total
            });
          }
        }
      }

      return {
        totalBalanceUsdt,
        availableBalanceUsdt,
        inOrdersUsdt: Math.max(0, totalBalanceUsdt - availableBalanceUsdt),
        balances
      };
    } catch (error: any) {
      const errDetails = error.response?.data?.retMsg || error.message;
      throw new Error(`فشل جلب رصيد الحساب من Bybit: ${errDetails}`);
    }
  }

  async getTicker(symbol: string = 'BTCUSDT'): Promise<Ticker> {
    try {
      const response = await axios.get(`${BYBIT_BASE_URL}/v5/market/tickers?category=spot&symbol=${symbol}`, {
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
      const response = await axios.get(`${BYBIT_BASE_URL}/v5/market/instruments-info?category=spot`, {
        timeout: 8000
      });
      const symbols = response.data.result.list.map((s: any) => s.symbol);
      return symbols;
    } catch (error: any) {
      throw new Error(`فشل جلب أزواج التداول من Bybit`);
    }
  }
}
