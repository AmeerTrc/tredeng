import { Response } from 'express';
import { AuthenticatedRequest } from '../middleware/authMiddleware';
import { CryptoService, EncryptedData } from '../services/cryptoService';
import { BinanceAdapter } from '../services/exchange/binanceAdapter';
import { ExnessAdapter } from '../services/exchange/exnessAdapter';

// In-memory store for encrypted keys per user
const userKeysStore = new Map<string, { exchangeName: string; apiKeyEncrypted: EncryptedData; apiSecretEncrypted: EncryptedData }>();

export class ExchangeController {
  static async saveKeys(req: AuthenticatedRequest, res: Response): Promise<void> {
    try {
      const userId = req.userId!;
      const { exchangeName, apiKey, apiSecret } = req.body;

      if (!apiKey || !apiSecret) {
        res.status(400).json({ success: false, data: null, message: 'API Key و API Secret مطلوبان' });
        return;
      }

      // Encrypt secrets using AES-256-GCM
      const encryptedKey = CryptoService.encrypt(apiKey);
      const encryptedSecret = CryptoService.encrypt(apiSecret);

      userKeysStore.set(userId, {
        exchangeName: exchangeName || 'binance',
        apiKeyEncrypted: encryptedKey,
        apiSecretEncrypted: encryptedSecret
      });

      const adapter = exchangeName?.toLowerCase().includes('exness') ? new ExnessAdapter() : new BinanceAdapter();
      const status = await adapter.validateKeys(apiKey, apiSecret);

      res.json({
        success: true,
        data: status,
        message: 'تم حفظ مفاتيح API بنجاح وتشفيرها بـ AES-256-GCM'
      });
    } catch (error: any) {
      res.status(500).json({ success: false, data: null, message: error.message });
    }
  }

  static async getBalance(req: AuthenticatedRequest, res: Response): Promise<void> {
    try {
      const userId = req.userId!;
      const userKeyInfo = userKeysStore.get(userId);

      let apiKey = 'MOCK_API_KEY';
      let apiSecret = 'MOCK_API_SECRET';
      let exchangeName = 'binance';

      if (userKeyInfo) {
        apiKey = CryptoService.decrypt(userKeyInfo.apiKeyEncrypted);
        apiSecret = CryptoService.decrypt(userKeyInfo.apiSecretEncrypted);
        exchangeName = userKeyInfo.exchangeName;
      }

      const adapter = exchangeName.toLowerCase().includes('exness') ? new ExnessAdapter() : new BinanceAdapter();
      const portfolio = await adapter.getPortfolio(apiKey, apiSecret);

      res.json({
        success: true,
        data: portfolio,
        message: 'تم جلب الرصيد بنجاح'
      });
    } catch (error: any) {
      res.status(500).json({ success: false, data: null, message: error.message });
    }
  }

  static async getStatus(req: AuthenticatedRequest, res: Response): Promise<void> {
    try {
      const userId = req.userId!;
      const userKeyInfo = userKeysStore.get(userId);

      if (!userKeyInfo) {
        res.json({
          success: true,
          data: {
            exchangeName: 'Binance Spot',
            isConnected: false,
            apiKeyMasked: 'لم يتم الربط بعد',
            permissions: [],
            canTradeSpot: false,
            canWithdraw: false
          },
          message: 'لم يتم تكوين المفاتيح بعد'
        });
        return;
      }

      const apiKey = CryptoService.decrypt(userKeyInfo.apiKeyEncrypted);
      const apiSecret = CryptoService.decrypt(userKeyInfo.apiSecretEncrypted);

      const adapter = userKeyInfo.exchangeName.toLowerCase().includes('exness') ? new ExnessAdapter() : new BinanceAdapter();
      const status = await adapter.validateKeys(apiKey, apiSecret);

      res.json({ success: true, data: status, message: 'تم جلب حالة الاتصال' });
    } catch (error: any) {
      res.status(500).json({ success: false, data: null, message: error.message });
    }
  }
}
