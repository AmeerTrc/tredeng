import { ExchangeAdapter, ExchangeStatus, PortfolioSummary, Ticker, Kline } from './exchangeAdapter';

export class ExnessAdapter implements ExchangeAdapter {
  getName(): string {
    return 'exness';
  }

  async validateKeys(apiKey: string, apiSecret: string): Promise<ExchangeStatus> {
    return {
      exchangeName: 'Exness MT5 (Gold / XAUUSD)',
      isConnected: true,
      apiKeyMasked: `${apiKey.substring(0, 4)}...`,
      permissions: ['SPOT', 'FOREX_METALS'],
      canTradeSpot: true,
      canWithdraw: false
    };
  }

  async getPortfolio(apiKey: string, apiSecret: string): Promise<PortfolioSummary> {
    return {
      totalBalanceUsdt: 3000.00,
      availableBalanceUsdt: 2500.00,
      inOrdersUsdt: 500.00,
      balances: [
        { asset: 'USD', free: 2500.00, locked: 500.00, total: 3000.00 }
      ]
    };
  }

  async getTicker(symbol: string = 'XAUUSD'): Promise<Ticker> {
    return {
      symbol: 'XAUUSD',
      price: 2648.50,
      high24h: 2660.00,
      low24h: 2635.00,
      volume24h: 450000,
      priceChangePercent: 0.85,
      timestamp: Date.now()
    };
  }

  async getAvailableSymbols(): Promise<string[]> {
    return ['XAUUSD', 'XAGUSD', 'EURUSD', 'GBPUSD'];
  }

  async getKlines(symbol: string = 'XAUUSD', interval: string = '15m', limit: number = 100): Promise<Kline[]> {
    const basePrice = 2648.50;
    const now = Date.now();
    const intervalMs = 15 * 60 * 1000;
    const klines: Kline[] = [];

    for (let i = limit; i >= 0; i--) {
      const openTime = now - (i * intervalMs);
      const varFactor = Math.sin(i / 3) * 5;
      klines.push({
        openTime,
        open: basePrice + varFactor,
        high: basePrice + varFactor + 3,
        low: basePrice + varFactor - 3,
        close: basePrice + varFactor + 1,
        volume: 5000 + (i * 10)
      });
    }
    return klines;
  }
}
