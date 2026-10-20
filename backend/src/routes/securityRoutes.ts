import { Router } from 'express';
import { SecurityController } from '../controllers/securityController';
import { authMiddleware } from '../middleware/authMiddleware';

const router = Router();

router.post('/kill-switch', authMiddleware as any, SecurityController.toggleKillSwitch as any);
router.get('/kill-switch', authMiddleware as any, SecurityController.getKillSwitchStatus as any);

router.get('/risk', authMiddleware as any, SecurityController.getRiskSettings as any);
router.post('/risk', authMiddleware as any, SecurityController.saveRiskSettings as any);

router.get('/settings', authMiddleware as any, SecurityController.getUserSettings as any);
router.post('/settings', authMiddleware as any, SecurityController.saveUserSettings as any);

export default router;
