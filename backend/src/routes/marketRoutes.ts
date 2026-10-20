import { Router } from 'express';
import { MarketController } from '../controllers/marketController';
import { authMiddleware } from '../middleware/authMiddleware';

const router = Router();

router.get('/ticker/:symbol?', MarketController.getTicker);

// Coin selection API
router.get('/coins/:exchangeName', authMiddleware as any, MarketController.getSelectedCoins as any);
router.post('/coins/:exchangeName', authMiddleware as any, MarketController.saveSelectedCoin as any);

export default router;
