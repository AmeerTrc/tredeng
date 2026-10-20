import fs from 'fs';
import path from 'path';
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

interface DatabaseSchema {
  users: Record<string, UserRecord>;
  exchangeKeys: Record<string, UserExchangeKeys>;
  securitySettings: Record<string, UserSecuritySettings>;
  riskSettings: Record<string, UserRiskSettings>;
}

const DATA_DIR = path.resolve(__dirname, '../../../data');
const DB_FILE = path.join(DATA_DIR, 'db.json');

export class DatabaseService {
  private static instance: DatabaseService;
  private data: DatabaseSchema = {
    users: {},
    exchangeKeys: {},
    securitySettings: {},
    riskSettings: {}
  };

  private constructor() {
    this.init();
  }

  static getInstance(): DatabaseService {
    if (!DatabaseService.instance) {
      DatabaseService.instance = new DatabaseService();
    }
    return DatabaseService.instance;
  }

  private init(): void {
    try {
      if (!fs.existsSync(DATA_DIR)) {
        fs.mkdirSync(DATA_DIR, { recursive: true });
      }
      if (fs.existsSync(DB_FILE)) {
        const raw = fs.readFileSync(DB_FILE, 'utf-8');
        this.data = JSON.parse(raw);
      } else {
        this.save();
      }
    } catch (error) {
      console.error('Error initializing DatabaseService:', error);
    }
  }

  private save(): void {
    try {
      fs.writeFileSync(DB_FILE, JSON.stringify(this.data, null, 2), 'utf-8');
    } catch (error) {
      console.error('Error saving database to file:', error);
    }
  }

  // User Management
  getUserByEmail(email: string): UserRecord | null {
    const normalized = email.trim().toLowerCase();
    for (const user of Object.values(this.data.users)) {
      if (user.email.toLowerCase() === normalized) {
        return user;
      }
    }
    return null;
  }

  getUserById(id: string): UserRecord | null {
    return this.data.users[id] || null;
  }

  createUser(email: string, passwordHash: string): UserRecord {
    const id = `usr_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
    const user: UserRecord = {
      id,
      email: email.trim().toLowerCase(),
      passwordHash,
      createdAt: Date.now()
    };
    this.data.users[id] = user;
    this.save();
    return user;
  }

  // Exchange Keys Management (User Isolated)
  saveExchangeKeys(userId: string, exchangeName: string, apiKeyEncrypted: EncryptedData, apiSecretEncrypted: EncryptedData): void {
    this.data.exchangeKeys[userId] = {
      userId,
      exchangeName: exchangeName || 'binance',
      apiKeyEncrypted,
      apiSecretEncrypted,
      updatedAt: Date.now()
    };
    this.save();
  }

  getExchangeKeys(userId: string): UserExchangeKeys | null {
    return this.data.exchangeKeys[userId] || null;
  }

  deleteExchangeKeys(userId: string): void {
    delete this.data.exchangeKeys[userId];
    this.save();
  }

  // Security / Kill Switch Management (User Isolated)
  setKillSwitch(userId: string, active: boolean): void {
    this.data.securitySettings[userId] = {
      userId,
      isKillSwitchActive: active,
      updatedAt: Date.now()
    };
    this.save();
  }

  isKillSwitchActive(userId: string): boolean {
    const setting = this.data.securitySettings[userId];
    return setting ? setting.isKillSwitchActive : false;
  }

  // Risk Settings Management (User Isolated)
  saveRiskSettings(userId: string, settings: Partial<UserRiskSettings>): UserRiskSettings {
    const existing = this.data.riskSettings[userId] || {
      userId,
      maxTradeRiskPct: 0.25,
      maxDailyLossPct: 1.0,
      minConfidence: 0.70,
      maxDailyTrades: 3,
      stopLossRequired: true,
      updatedAt: Date.now()
    };

    const updated: UserRiskSettings = {
      ...existing,
      ...settings,
      userId,
      updatedAt: Date.now()
    };

    this.data.riskSettings[userId] = updated;
    this.save();
    return updated;
  }

  getRiskSettings(userId: string): UserRiskSettings {
    return this.data.riskSettings[userId] || {
      userId,
      maxTradeRiskPct: 0.25,
      maxDailyLossPct: 1.0,
      minConfidence: 0.70,
      maxDailyTrades: 3,
      stopLossRequired: true,
      updatedAt: Date.now()
    };
  }
}
