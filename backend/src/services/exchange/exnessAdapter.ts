import { ExchangeAdapter, ExchangeStatus, PortfolioSummary, Ticker } from './exchangeAdapter';

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
}
