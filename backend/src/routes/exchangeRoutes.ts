import { Router } from 'express';
import { ExchangeController } from '../controllers/exchangeController';
import { authMiddleware } from '../middleware/authMiddleware';

const router = Router();

router.post('/keys', authMiddleware as any, ExchangeController.saveKeys as any);
router.delete('/keys/:exchangeName', authMiddleware as any, ExchangeController.deleteKeys as any);
router.get('/all-status', authMiddleware as any, ExchangeController.getAllKeysStatus as any);
router.get('/balance', authMiddleware as any, ExchangeController.getBalance as any);
router.get('/status', authMiddleware as any, ExchangeController.getStatus as any);

// Positions & History
router.get('/positions', authMiddleware as any, ExchangeController.getOpenPositions as any);
router.post('/positions/close', authMiddleware as any, ExchangeController.closePosition as any);
router.get('/history', authMiddleware as any, ExchangeController.getTradeHistory as any);

export default router;
