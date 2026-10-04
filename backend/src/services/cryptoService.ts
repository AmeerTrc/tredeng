import crypto from 'crypto';
import { ENV } from '../config/env';

const ALGORITHM = 'aes-256-gcm';

export interface EncryptedData {
  encryptedText: string;
  iv: string;
  tag: string;
}

export class CryptoService {
  static encrypt(plainText: string): EncryptedData {
    const iv = crypto.randomBytes(12);
    const key = Buffer.from(ENV.AES_SECRET_KEY, 'utf-8');
    const cipher = crypto.createCipheriv(ALGORITHM, key, iv);

    let encrypted = cipher.update(plainText, 'utf8', 'hex');
    encrypted += cipher.final('hex');

    const tag = cipher.getAuthTag().toString('hex');

    return {
      encryptedText: encrypted,
      iv: iv.toString('hex'),
      tag: tag
    };
  }

  static decrypt(encryptedData: EncryptedData): string {
    const key = Buffer.from(ENV.AES_SECRET_KEY, 'utf-8');
    const iv = Buffer.from(encryptedData.iv, 'hex');
    const tag = Buffer.from(encryptedData.tag, 'hex');

    const decipher = crypto.createDecipheriv(ALGORITHM, key, iv);
    decipher.setAuthTag(tag);

    let decrypted = decipher.update(encryptedData.encryptedText, 'hex', 'utf8');
    decrypted += decipher.final('utf8');

    return decrypted;
  }

  static maskApiKey(apiKey: string): string {
    if (!apiKey || apiKey.length <= 8) return '****';
    return `${apiKey.substring(0, 4)}...${apiKey.substring(apiKey.length - 4)}`;
  }
}
