import { Router } from 'express';
import { SecurityController } from '../controllers/securityController';
import { authMiddleware } from '../middleware/authMiddleware';

const router = Router();

router.post('/kill-switch', authMiddleware as any, SecurityController.toggleKillSwitch as any);
router.get('/kill-switch', authMiddleware as any, SecurityController.getKillSwitchStatus as any);

export default router;
