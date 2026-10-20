import path from 'path';
import fs from 'fs';
import { EncryptedData } from '../cryptoService';

export interface UserRecord {
  id: string;
  email: string;
  passwordHash: string;
  createdAt: number;
}

export interface UserExchangeKeys {
  userId: string;
  exchangeName: string;
  apiKeyEncrypted: EncryptedData;
  apiSecretEncrypted: EncryptedData;
  updatedAt: number;
}

export interface UserSecuritySettings {
  userId: string;
  isKillSwitchActive: boolean;
  updatedAt: number;
}

export interface UserRiskSettings {
  userId: string;
  maxTradeRiskPct: number;
  maxDailyLossPct: number;
  minConfidence: number;
  maxDailyTrades: number;
  stopLossRequired: boolean;
  updatedAt: number;
}

export interface SelectedCoin {
  userId: string;
  exchangeName: string;
  symbol: string;
  isActive: boolean;
}

const DATA_DIR = path.resolve(__dirname, '../../../data');
const DB_FILE = path.join(DATA_DIR, 'database.sqlite');
const FALLBACK_FILE = path.join(DATA_DIR, 'db_fallback.json');

export class DatabaseService {
  private static instance: DatabaseService;
  private db: any = null;
  private isInitializing = false;
  private isFallback = false;
  private memoryDb: {
    users: Record<string, UserRecord>;
    exchangeKeys: Record<string, UserExchangeKeys>;
    securitySettings: Record<string, UserSecuritySettings>;
    riskSettings: Record<string, UserRiskSettings>;
    selectedCoins: Record<string, SelectedCoin>;
    paperTrades: Record<string, any>;
  } = {
    users: {},
    exchangeKeys: {},
    securitySettings: {},
    riskSettings: {},
    selectedCoins: {},
    paperTrades: {}
  };

  private constructor() {}

  static getInstance(): DatabaseService {
    if (!DatabaseService.instance) {
      DatabaseService.instance = new DatabaseService();
    }
    return DatabaseService.instance;
  }

  async init(): Promise<void> {
    if (this.db || this.isFallback || this.isInitializing) return;
    this.isInitializing = true;
    try {
      if (!fs.existsSync(DATA_DIR)) {
        fs.mkdirSync(DATA_DIR, { recursive: true });
      }

      // Try dynamically requiring sqlite3 and sqlite to prevent top-level crash if native bindings fail
      const sqlite3 = require('sqlite3');
      const { open } = require('sqlite');

      this.db = await open({
        filename: DB_FILE,
        driver: sqlite3.Database
      });

      await this.db.exec(`
        CREATE TABLE IF NOT EXISTS users (
          id TEXT PRIMARY KEY,
          email TEXT UNIQUE,
          passwordHash TEXT,
          createdAt INTEGER
        );

        CREATE TABLE IF NOT EXISTS exchange_keys (
          userId TEXT,
          exchangeName TEXT,
          apiKeyIv TEXT,
          apiKeyEncryptedText TEXT,
          apiKeyTag TEXT,
          apiSecretIv TEXT,
          apiSecretEncryptedText TEXT,
          apiSecretTag TEXT,
          updatedAt INTEGER,
          PRIMARY KEY (userId, exchangeName)
        );

        CREATE TABLE IF NOT EXISTS security_settings (
          userId TEXT PRIMARY KEY,
          isKillSwitchActive INTEGER,
          updatedAt INTEGER
        );

        CREATE TABLE IF NOT EXISTS risk_settings (
          userId TEXT PRIMARY KEY,
          maxTradeRiskPct REAL,
          maxDailyLossPct REAL,
          minConfidence REAL,
          maxDailyTrades INTEGER,
          stopLossRequired INTEGER,
          updatedAt INTEGER
        );

        CREATE TABLE IF NOT EXISTS selected_coins (
          userId TEXT,
          exchangeName TEXT,
          symbol TEXT,
          isActive INTEGER,
          PRIMARY KEY (userId, exchangeName, symbol)
        );

        CREATE TABLE IF NOT EXISTS paper_trades (
          id TEXT PRIMARY KEY,
          userId TEXT,
          exchangeName TEXT,
          symbol TEXT,
          action TEXT,
          price REAL,
          amount REAL,
          stopLoss REAL,
          takeProfit REAL,
          status TEXT,
          pnl REAL,
          createdAt INTEGER
        );
      `);
      console.log('SQLite database initialized successfully.');
    } catch (error: any) {
      console.warn('SQLite native driver unavailable or failed. Switching to JSON file fallback persistence:', error.message);
      this.isFallback = true;
      this.loadFallback();
    } finally {
      this.isInitializing = false;
    }
  }

  private loadFallback() {
    try {
      if (fs.existsSync(FALLBACK_FILE)) {
        const raw = fs.readFileSync(FALLBACK_FILE, 'utf8');
        this.memoryDb = JSON.parse(raw);
      }
    } catch (e) {
      console.error("Error loading JSON fallback database:", e);
    }
  }

  private saveFallback() {
    if (!this.isFallback) return;
    try {
      fs.writeFileSync(FALLBACK_FILE, JSON.stringify(this.memoryDb, null, 2), 'utf8');
    } catch (e) {
      console.error("Error saving JSON fallback database:", e);
    }
  }

  // User Management
  async getUserByEmail(email: string): Promise<UserRecord | null> {
    const normalized = email.trim().toLowerCase();
    if (this.isFallback) {
      return Object.values(this.memoryDb.users).find(u => u.email.toLowerCase() === normalized) || null;
    }
    const row = await this.db?.get('SELECT * FROM users WHERE LOWER(email) = ?', normalized);
    return row as UserRecord || null;
  }

  async getUserById(id: string): Promise<UserRecord | null> {
    if (this.isFallback) {
      return this.memoryDb.users[id] || null;
    }
    const row = await this.db?.get('SELECT * FROM users WHERE id = ?', id);
    return row as UserRecord || null;
  }

  async createUser(email: string, passwordHash: string): Promise<UserRecord> {
    const id = `usr_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
    const normalizedEmail = email.trim().toLowerCase();
    const createdAt = Date.now();
    const user = { id, email: normalizedEmail, passwordHash, createdAt };

    if (this.isFallback) {
      this.memoryDb.users[id] = user;
      this.saveFallback();
      return user;
    }

    await this.db?.run(
      'INSERT INTO users (id, email, passwordHash, createdAt) VALUES (?, ?, ?, ?)',
      [id, normalizedEmail, passwordHash, createdAt]
    );
    return user;
  }

  // Exchange Keys
  async saveExchangeKeys(userId: string, exchangeName: string, apiKeyEncrypted: EncryptedData, apiSecretEncrypted: EncryptedData): Promise<void> {
    const keyInfo: UserExchangeKeys = {
      userId,
      exchangeName,
      apiKeyEncrypted,
      apiSecretEncrypted,
      updatedAt: Date.now()
    };

    if (this.isFallback) {
      this.memoryDb.exchangeKeys[`${userId}_${exchangeName}`] = keyInfo;
      this.saveFallback();
      return;
    }

    await this.db?.run(
      `INSERT INTO exchange_keys (userId, exchangeName, apiKeyIv, apiKeyEncryptedText, apiKeyTag, apiSecretIv, apiSecretEncryptedText, apiSecretTag, updatedAt)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
       ON CONFLICT(userId, exchangeName) DO UPDATE SET
       apiKeyIv=excluded.apiKeyIv, apiKeyEncryptedText=excluded.apiKeyEncryptedText, apiKeyTag=excluded.apiKeyTag,
       apiSecretIv=excluded.apiSecretIv, apiSecretEncryptedText=excluded.apiSecretEncryptedText, apiSecretTag=excluded.apiSecretTag,
       updatedAt=excluded.updatedAt`,
      [userId, exchangeName, apiKeyEncrypted.iv, apiKeyEncrypted.encryptedText, apiKeyEncrypted.tag, apiSecretEncrypted.iv, apiSecretEncrypted.encryptedText, apiSecretEncrypted.tag, Date.now()]
    );
  }

  async getExchangeKeys(userId: string, exchangeName: string): Promise<UserExchangeKeys | null> {
    if (this.isFallback) {
      return this.memoryDb.exchangeKeys[`${userId}_${exchangeName}`] || null;
    }
    const row = await this.db?.get('SELECT * FROM exchange_keys WHERE userId = ? AND exchangeName = ?', [userId, exchangeName]);
    if (!row) return null;
    return {
      userId: row.userId,
      exchangeName: row.exchangeName,
      apiKeyEncrypted: { iv: row.apiKeyIv, encryptedText: row.apiKeyEncryptedText, tag: row.apiKeyTag },
      apiSecretEncrypted: { iv: row.apiSecretIv, encryptedText: row.apiSecretEncryptedText, tag: row.apiSecretTag },
      updatedAt: row.updatedAt
    };
  }

  async getAllExchangeKeys(userId: string): Promise<UserExchangeKeys[]> {
    if (this.isFallback) {
      return Object.values(this.memoryDb.exchangeKeys).filter(k => k.userId === userId);
    }
    const rows = await this.db?.all('SELECT * FROM exchange_keys WHERE userId = ?', [userId]);
    return (rows || []).map((row: any) => ({
      userId: row.userId,
      exchangeName: row.exchangeName,
      apiKeyEncrypted: { iv: row.apiKeyIv, encryptedText: row.apiKeyEncryptedText, tag: row.apiKeyTag },
      apiSecretEncrypted: { iv: row.apiSecretIv, encryptedText: row.apiSecretEncryptedText, tag: row.apiSecretTag },
      updatedAt: row.updatedAt
    }));
  }

  async deleteExchangeKeys(userId: string, exchangeName: string): Promise<void> {
    if (this.isFallback) {
      delete this.memoryDb.exchangeKeys[`${userId}_${exchangeName}`];
      this.saveFallback();
      return;
    }
    await this.db?.run('DELETE FROM exchange_keys WHERE userId = ? AND exchangeName = ?', [userId, exchangeName]);
  }

  // Security / Kill Switch
  async setKillSwitch(userId: string, active: boolean): Promise<void> {
    if (this.isFallback) {
      this.memoryDb.securitySettings[userId] = { userId, isKillSwitchActive: active, updatedAt: Date.now() };
      this.saveFallback();
      return;
    }
    await this.db?.run(
      `INSERT INTO security_settings (userId, isKillSwitchActive, updatedAt)
       VALUES (?, ?, ?)
       ON CONFLICT(userId) DO UPDATE SET isKillSwitchActive=excluded.isKillSwitchActive, updatedAt=excluded.updatedAt`,
      [userId, active ? 1 : 0, Date.now()]
    );
  }

  async isKillSwitchActive(userId: string): Promise<boolean> {
    if (this.isFallback) {
      return this.memoryDb.securitySettings[userId]?.isKillSwitchActive || false;
    }
    const row = await this.db?.get('SELECT isKillSwitchActive FROM security_settings WHERE userId = ?', [userId]);
    return row ? row.isKillSwitchActive === 1 : false;
  }

  // Risk Settings
  async saveRiskSettings(userId: string, settings: Partial<UserRiskSettings>): Promise<UserRiskSettings> {
    const current = await this.getRiskSettings(userId);
    const updated = { ...current, ...settings, updatedAt: Date.now() };

    if (this.isFallback) {
      this.memoryDb.riskSettings[userId] = updated;
      this.saveFallback();
      return updated;
    }

    await this.db?.run(
      `INSERT INTO risk_settings (userId, maxTradeRiskPct, maxDailyLossPct, minConfidence, maxDailyTrades, stopLossRequired, updatedAt)
       VALUES (?, ?, ?, ?, ?, ?, ?)
       ON CONFLICT(userId) DO UPDATE SET
       maxTradeRiskPct=excluded.maxTradeRiskPct, maxDailyLossPct=excluded.maxDailyLossPct,
       minConfidence=excluded.minConfidence, maxDailyTrades=excluded.maxDailyTrades,
       stopLossRequired=excluded.stopLossRequired, updatedAt=excluded.updatedAt`,
      [userId, updated.maxTradeRiskPct, updated.maxDailyLossPct, updated.minConfidence, updated.maxDailyTrades, updated.stopLossRequired ? 1 : 0, updated.updatedAt]
    );
    return updated;
  }

  async getRiskSettings(userId: string): Promise<UserRiskSettings> {
    if (this.isFallback) {
      return this.memoryDb.riskSettings[userId] || {
        userId,
        maxTradeRiskPct: 0.25,
        maxDailyLossPct: 1.0,
        minConfidence: 0.70,
        maxDailyTrades: 3,
        stopLossRequired: true,
        updatedAt: Date.now()
      };
    }
    const row = await this.db?.get('SELECT * FROM risk_settings WHERE userId = ?', [userId]);
    if (row) {
      return {
        userId: row.userId,
        maxTradeRiskPct: row.maxTradeRiskPct,
        maxDailyLossPct: row.maxDailyLossPct,
        minConfidence: row.minConfidence,
        maxDailyTrades: row.maxDailyTrades,
        stopLossRequired: row.stopLossRequired === 1,
        updatedAt: row.updatedAt
      };
    }
    return {
      userId,
      maxTradeRiskPct: 0.25,
      maxDailyLossPct: 1.0,
      minConfidence: 0.70,
      maxDailyTrades: 3,
      stopLossRequired: true,
      updatedAt: Date.now()
    };
  }

  // Selected Coins
  async saveSelectedCoin(userId: string, exchangeName: string, symbol: string, isActive: boolean): Promise<void> {
    if (this.isFallback) {
      this.memoryDb.selectedCoins[`${userId}_${exchangeName}_${symbol}`] = { userId, exchangeName, symbol, isActive };
      this.saveFallback();
      return;
    }
    await this.db?.run(
      `INSERT INTO selected_coins (userId, exchangeName, symbol, isActive)
       VALUES (?, ?, ?, ?)
       ON CONFLICT(userId, exchangeName, symbol) DO UPDATE SET isActive=excluded.isActive`,
      [userId, exchangeName, symbol, isActive ? 1 : 0]
    );
  }

  async getSelectedCoins(userId: string, exchangeName: string): Promise<SelectedCoin[]> {
    if (this.isFallback) {
      return Object.values(this.memoryDb.selectedCoins).filter(c => c.userId === userId && c.exchangeName === exchangeName && c.isActive);
    }
    const rows = await this.db?.all(
      'SELECT * FROM selected_coins WHERE userId = ? AND exchangeName = ? AND isActive = 1',
      [userId, exchangeName]
    );
    return (rows || []).map((row: any) => ({
      userId: row.userId,
      exchangeName: row.exchangeName,
      symbol: row.symbol,
      isActive: row.isActive === 1
    }));
  }

  // Paper Trading
  async recordPaperTrade(
    userId: string,
    exchangeName: string,
    symbol: string,
    action: string,
    price: number,
    amount: number,
    stopLoss: number,
    takeProfit: number
  ): Promise<string> {
    const id = `pt_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
    const trade = { id, userId, exchangeName, symbol, action, price, amount, stopLoss, takeProfit, status: 'OPEN', pnl: 0.0, createdAt: Date.now() };

    if (this.isFallback) {
      this.memoryDb.paperTrades[id] = trade;
      this.saveFallback();
      return id;
    }

    await this.db?.run(
      `INSERT INTO paper_trades (id, userId, exchangeName, symbol, action, price, amount, stopLoss, takeProfit, status, pnl, createdAt)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      [id, userId, exchangeName, symbol, action, price, amount, stopLoss, takeProfit, 'OPEN', 0.0, Date.now()]
    );
    return id;
  }

  async getOpenPaperTrades(userId: string): Promise<any[]> {
    if (this.isFallback) {
      return Object.values(this.memoryDb.paperTrades).filter(t => t.userId === userId && t.status === 'OPEN');
    }
    const rows = await this.db?.all('SELECT * FROM paper_trades WHERE userId = ? AND status = ?', [userId, 'OPEN']);
    return rows || [];
  }
}
