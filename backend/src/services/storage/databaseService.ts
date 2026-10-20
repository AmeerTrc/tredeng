import sqlite3 from 'sqlite3';
import { open, Database } from 'sqlite';
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

export class DatabaseService {
  private static instance: DatabaseService;
  private db: Database | null = null;
  private isInitializing = false;

  private constructor() {}

  static getInstance(): DatabaseService {
    if (!DatabaseService.instance) {
      DatabaseService.instance = new DatabaseService();
    }
    return DatabaseService.instance;
  }

  async init(): Promise<void> {
    if (this.db || this.isInitializing) return;
    this.isInitializing = true;
    try {
      if (!fs.existsSync(DATA_DIR)) {
        fs.mkdirSync(DATA_DIR, { recursive: true });
      }

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
          apiKeyContent TEXT,
          apiSecretIv TEXT,
          apiSecretContent TEXT,
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
      `);
      console.log('SQLite database initialized successfully.');
    } catch (error) {
      console.error('Error initializing SQLite database:', error);
      throw error;
    } finally {
      this.isInitializing = false;
    }
  }

  // User Management
  async getUserByEmail(email: string): Promise<UserRecord | null> {
    const normalized = email.trim().toLowerCase();
    const row = await this.db?.get('SELECT * FROM users WHERE LOWER(email) = ?', normalized);
    return row as UserRecord || null;
  }

  async getUserById(id: string): Promise<UserRecord | null> {
    const row = await this.db?.get('SELECT * FROM users WHERE id = ?', id);
    return row as UserRecord || null;
  }

  async createUser(email: string, passwordHash: string): Promise<UserRecord> {
    const id = `usr_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
    const normalizedEmail = email.trim().toLowerCase();
    const createdAt = Date.now();
    await this.db?.run(
      'INSERT INTO users (id, email, passwordHash, createdAt) VALUES (?, ?, ?, ?)',
      [id, normalizedEmail, passwordHash, createdAt]
    );
    return { id, email: normalizedEmail, passwordHash, createdAt };
  }

  // Exchange Keys
  async saveExchangeKeys(userId: string, exchangeName: string, apiKeyEncrypted: EncryptedData, apiSecretEncrypted: EncryptedData): Promise<void> {
    await this.db?.run(
      `INSERT INTO exchange_keys (userId, exchangeName, apiKeyIv, apiKeyContent, apiSecretIv, apiSecretContent, updatedAt)
       VALUES (?, ?, ?, ?, ?, ?, ?)
       ON CONFLICT(userId, exchangeName) DO UPDATE SET
       apiKeyIv=excluded.apiKeyIv, apiKeyContent=excluded.apiKeyContent,
       apiSecretIv=excluded.apiSecretIv, apiSecretContent=excluded.apiSecretContent,
       updatedAt=excluded.updatedAt`,
      [userId, exchangeName, apiKeyEncrypted.iv, apiKeyEncrypted.content, apiSecretEncrypted.iv, apiSecretEncrypted.content, Date.now()]
    );
  }

  async getExchangeKeys(userId: string, exchangeName: string): Promise<UserExchangeKeys | null> {
    const row = await this.db?.get('SELECT * FROM exchange_keys WHERE userId = ? AND exchangeName = ?', [userId, exchangeName]);
    if (!row) return null;
    return {
      userId: row.userId,
      exchangeName: row.exchangeName,
      apiKeyEncrypted: { iv: row.apiKeyIv, content: row.apiKeyContent },
      apiSecretEncrypted: { iv: row.apiSecretIv, content: row.apiSecretContent },
      updatedAt: row.updatedAt
    };
  }

  async getAllExchangeKeys(userId: string): Promise<UserExchangeKeys[]> {
    const rows = await this.db?.all('SELECT * FROM exchange_keys WHERE userId = ?', [userId]);
    return (rows || []).map(row => ({
      userId: row.userId,
      exchangeName: row.exchangeName,
      apiKeyEncrypted: { iv: row.apiKeyIv, content: row.apiKeyContent },
      apiSecretEncrypted: { iv: row.apiSecretIv, content: row.apiSecretContent },
      updatedAt: row.updatedAt
    }));
  }

  async deleteExchangeKeys(userId: string, exchangeName: string): Promise<void> {
    await this.db?.run('DELETE FROM exchange_keys WHERE userId = ? AND exchangeName = ?', [userId, exchangeName]);
  }

  // Security / Kill Switch
  async setKillSwitch(userId: string, active: boolean): Promise<void> {
    await this.db?.run(
      `INSERT INTO security_settings (userId, isKillSwitchActive, updatedAt)
       VALUES (?, ?, ?)
       ON CONFLICT(userId) DO UPDATE SET isKillSwitchActive=excluded.isKillSwitchActive, updatedAt=excluded.updatedAt`,
      [userId, active ? 1 : 0, Date.now()]
    );
  }

  async isKillSwitchActive(userId: string): Promise<boolean> {
    const row = await this.db?.get('SELECT isKillSwitchActive FROM security_settings WHERE userId = ?', [userId]);
    return row ? row.isKillSwitchActive === 1 : false;
  }

  // Risk Settings
  async saveRiskSettings(userId: string, settings: Partial<UserRiskSettings>): Promise<UserRiskSettings> {
    const current = await this.getRiskSettings(userId);
    const updated = { ...current, ...settings, updatedAt: Date.now() };

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
    await this.db?.run(
      `INSERT INTO selected_coins (userId, exchangeName, symbol, isActive)
       VALUES (?, ?, ?, ?)
       ON CONFLICT(userId, exchangeName, symbol) DO UPDATE SET isActive=excluded.isActive`,
      [userId, exchangeName, symbol, isActive ? 1 : 0]
    );
  }

  async getSelectedCoins(userId: string, exchangeName: string): Promise<SelectedCoin[]> {
    const rows = await this.db?.all(
      'SELECT * FROM selected_coins WHERE userId = ? AND exchangeName = ? AND isActive = 1',
      [userId, exchangeName]
    );
    return (rows || []).map(row => ({
      userId: row.userId,
      exchangeName: row.exchangeName,
      symbol: row.symbol,
      isActive: row.isActive === 1
    }));
  }
}
