import { Response } from 'express';
import { AuthenticatedRequest } from '../middleware/authMiddleware';

let isKillSwitchActive = false;

export class SecurityController {
  static async toggleKillSwitch(req: AuthenticatedRequest, res: Response): Promise<void> {
    try {
      const { active } = req.body;
      isKillSwitchActive = Boolean(active);

      res.json({
        success: true,
        data: isKillSwitchActive,
        message: isKillSwitchActive ? 'تم تفعيل زر الطوارئ بنجاح وإيقاف الصفقات' : 'تم إلغاء زر الطوارئ'
      });
    } catch (error: any) {
      res.status(500).json({ success: false, data: null, message: error.message });
    }
  }

  static isKillSwitchOn(): boolean {
    return isKillSwitchActive;
  }
}
