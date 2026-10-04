export interface Balance {
  asset: string;
  free: number;
  locked: number;
  total: number;
}

export interface PortfolioSummary {
  totalBalanceUsdt: number;
  availableBalanceUsdt: number;
  inOrdersUsdt: number;
  balances: Balance[];
}

export interface Ticker {
  symbol: string;
  price: number;
  high24h: number;
  low24h: number;
  volume24h: number;
  priceChangePercent: number;
  timestamp: number;
}

export interface ExchangeStatus {
  exchangeName: string;
  isConnected: boolean;
  apiKeyMasked: string;
  permissions: string[];
  canTradeSpot: boolean;
  canWithdraw: boolean;
}

export interface ExchangeAdapter {
  getName(): string;
  validateKeys(apiKey: string, apiSecret: string): Promise<ExchangeStatus>;
  getPortfolio(apiKey: string, apiSecret: string): Promise<PortfolioSummary>;
  getTicker(symbol: string): Promise<Ticker>;
}
