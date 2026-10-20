import { Response } from 'express';
import { AuthenticatedRequest } from '../middleware/authMiddleware';
import { DatabaseService } from '../services/storage/databaseService';

const db = DatabaseService.getInstance();

export class SecurityController {
  static async toggleKillSwitch(req: AuthenticatedRequest, res: Response): Promise<void> {
    try {
      const userId = req.userId!;
      const { active } = req.body;
      const isKillSwitchActive = Boolean(active);

      await db.setKillSwitch(userId, isKillSwitchActive);

      res.json({
        success: true,
        data: isKillSwitchActive,
        message: isKillSwitchActive
          ? 'تم تفعيل زر الطوارئ بنجاح وإيقاف الصفقات فورياً'
          : 'تم إلغاء زر الطوارئ والعودة للوضع الطبيعي'
      });
    } catch (error: any) {
      res.status(500).json({ success: false, data: null, message: error.message });
    }
  }

  static async getKillSwitchStatus(req: AuthenticatedRequest, res: Response): Promise<void> {
    try {
      const userId = req.userId!;
      const isKillSwitchActive = await db.isKillSwitchActive(userId);

      res.json({
        success: true,
        data: isKillSwitchActive,
        message: 'تم جلب حالة زر الطوارئ'
      });
    } catch (error: any) {
      res.status(500).json({ success: false, data: null, message: error.message });
    }
  }

  static async isKillSwitchOn(userId: string): Promise<boolean> {
    return await db.isKillSwitchActive(userId);
  }
}
