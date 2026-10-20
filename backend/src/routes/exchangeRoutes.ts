import { Router } from 'express';
import { ExchangeController } from '../controllers/exchangeController';
import { authMiddleware } from '../middleware/authMiddleware';

const router = Router();

router.post('/keys', authMiddleware as any, ExchangeController.saveKeys as any);
router.get('/balance', authMiddleware as any, ExchangeController.getBalance as any);
router.get('/status', authMiddleware as any, ExchangeController.getStatus as any);
router.get('/paper-trades', authMiddleware as any, ExchangeController.getPaperTrades as any);

export default router;
