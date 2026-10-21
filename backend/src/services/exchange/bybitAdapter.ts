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
    const recvWindow = 5000;
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
    try {
      const headers = this.getAuthHeaders(apiKey, apiSecret, '');
      const response = await axios.get(`${BYBIT_BASE_URL}/v5/user/get-api-key-information`, {
        headers,
        timeout: 8000
      });

      if (response.data.retCode !== 0) {
        const msg = response.data.retMsg || 'فشل التحقق من مفاتيح Bybit';
        console.error('Bybit validation error:', msg);
        return {
          exchangeName: 'Bybit Spot',
          isConnected: false,
          apiKeyMasked: `${apiKey.substring(0, 4)}...`,
          permissions: [],
          canTradeSpot: false,
          canWithdraw: false,
          errorMessage: `فشل المصادقة مع Bybit: ${msg}`
        };
      }

      const info = response.data.result;
      const isReadOnly = info.readOnly === 1;
      const permissionsMap = info.permissions || {};

      const spotPermissions = permissionsMap.Spot || [];
      const walletPermissions = permissionsMap.Wallet || [];

      const canTradeSpot = !isReadOnly && (spotPermissions.includes('SpotTrade') || spotPermissions.length > 0 || permissionsMap.Options?.length > 0);
      const canWithdraw = walletPermissions.includes('Withdraw');

      return {
        exchangeName: 'Bybit Spot',
        isConnected: true,
        apiKeyMasked: `${apiKey.substring(0, 4)}...${apiKey.substring(apiKey.length - 4)}`,
        permissions: Object.keys(permissionsMap),
        canTradeSpot: canTradeSpot,
        canWithdraw: canWithdraw
      };
    } catch (error: any) {
      const msg = error.response?.data?.retMsg || error.message || 'خطأ اتصال برلمجي';
      console.error('Bybit validation exception:', msg);
      return {
        exchangeName: 'Bybit Spot',
        isConnected: false,
        apiKeyMasked: 'INVALID',
        permissions: [],
        canTradeSpot: false,
        canWithdraw: false,
        errorMessage: `فشل الاتصال بـ Bybit: ${msg}`
      };
    }
  }

  async getPortfolio(apiKey: string, apiSecret: string): Promise<PortfolioSummary> {
    try {
      const queryString = 'accountType=UNIFIED';
      const headers = this.getAuthHeaders(apiKey, apiSecret, queryString);

      const response = await axios.get(`${BYBIT_BASE_URL}/v5/account/wallet-balance?${queryString}`, {
        headers,
        timeout: 8000
      });

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
