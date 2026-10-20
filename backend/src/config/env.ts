import dotenv from 'dotenv';
dotenv.config();

const JWT_SECRET = process.env.JWT_SECRET;
const AES_SECRET_KEY = process.env.AES_SECRET_KEY;

if (!JWT_SECRET || JWT_SECRET.trim().length < 16) {
  console.error('FATAL ERROR: JWT_SECRET environment variable is missing or insecure! Server startup aborted.');
  process.exit(1);
}

if (!AES_SECRET_KEY || Buffer.from(AES_SECRET_KEY, 'utf-8').length < 32) {
  console.error('FATAL ERROR: AES_SECRET_KEY must be configured in environment variables and be at least 32 bytes (256-bit)! Server startup aborted.');
  process.exit(1);
}

export const ENV = {
  PORT: process.env.PORT ? parseInt(process.env.PORT, 10) : 5000,
  JWT_SECRET: JWT_SECRET,
  AES_SECRET_KEY: AES_SECRET_KEY.substring(0, 32),
  BINANCE_BASE_URL: process.env.BINANCE_BASE_URL || 'https://api.binance.com',
  BINANCE_TESTNET_URL: process.env.BINANCE_TESTNET_URL || 'https://testnet.binance.vision'
};
