import dotenv from 'dotenv';
dotenv.config();

const JWT_SECRET = process.env.JWT_SECRET || 'default_super_secure_jwt_secret_key_for_tredeig_trading_2026';
const AES_SECRET_KEY = process.env.AES_SECRET_KEY || 'default_aes_secret_key_32bytes_long_tredeig_2026_pass';

export const ENV = {
  PORT: process.env.PORT ? parseInt(process.env.PORT, 10) : 5000,
  JWT_SECRET: JWT_SECRET,
  AES_SECRET_KEY: AES_SECRET_KEY.substring(0, 32),
  BINANCE_BASE_URL: process.env.BINANCE_BASE_URL || 'https://api.binance.com',
  BINANCE_TESTNET_URL: process.env.BINANCE_TESTNET_URL || 'https://testnet.binance.vision',
  GEMINI_API_KEY: process.env.GEMINI_API_KEY || ''
};
