import { Router } from 'express';
import { MarketController } from '../controllers/marketController';

const router = Router();

router.get('/ticker/:symbol?', MarketController.getTicker);

export default router;
