import dotenv from 'dotenv';
dotenv.config();

export const ENV = {
  PORT: process.env.PORT || 5000,
  JWT_SECRET: process.env.JWT_SECRET || 'TREDEIG_SECURE_JWT_SECRET_KEY_987654321',
  AES_SECRET_KEY: process.env.AES_SECRET_KEY || '12345678901234567890123456789012', // 32 chars for AES-256
  BINANCE_BASE_URL: process.env.BINANCE_BASE_URL || 'https://api.binance.com'
};
