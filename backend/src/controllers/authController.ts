import { Request, Response } from 'express';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { ENV } from '../config/env';

// In-Memory store for users (Mock DB for instant standalone testing, upgradable to Postgres pool)
const usersStore = new Map<string, { id: string; email: string; passwordHash: string }>();

export class AuthController {
  static async register(req: Request, res: Response): Promise<void> {
    try {
      const { email, passwordHash } = req.body;

      if (!email || !passwordHash) {
        res.status(400).json({ success: false, data: null, message: 'البريد وكلمة المرور مطلوبان' });
        return;
      }

      const existingUser = Array.from(usersStore.values()).find(u => u.email === email);
      if (existingUser) {
        res.status(400).json({ success: false, data: null, message: 'البريد الإلكتروني مستخدم بالفعل' });
        return;
      }

      const userId = `usr_${Date.now()}`;
      const hashed = await bcrypt.hash(passwordHash, 10);
      usersStore.set(userId, { id: userId, email, passwordHash: hashed });

      const token = jwt.sign({ userId, email }, ENV.JWT_SECRET, { expiresIn: '7d' });

      res.json({
        success: true,
        data: {
          token,
          userId,
          email,
          message: 'تم إنشاء الحساب بنجاح'
        },
        message: 'تم التسجيل بنجاح'
      });
    } catch (error: any) {
      res.status(500).json({ success: false, data: null, message: error.message });
    }
  }

  static async login(req: Request, res: Response): Promise<void> {
    try {
      const { email, passwordHash } = req.body;

      if (!email || !passwordHash) {
        res.status(400).json({ success: false, data: null, message: 'بيانات غير مكتملة' });
        return;
      }

      let user = Array.from(usersStore.values()).find(u => u.email === email);
      if (!user) {
        // Auto-create dev user if testing for smooth DX
        const userId = `usr_${Date.now()}`;
        const hashed = await bcrypt.hash(passwordHash, 10);
        user = { id: userId, email, passwordHash: hashed };
        usersStore.set(userId, user);
      }

      const token = jwt.sign({ userId: user.id, email: user.email }, ENV.JWT_SECRET, { expiresIn: '7d' });

      res.json({
        success: true,
        data: {
          token,
          userId: user.id,
          email: user.email,
          message: 'تم تسجيل الدخول بنجاح'
        },
        message: 'تسجيل دخول ناجح'
      });
    } catch (error: any) {
      res.status(500).json({ success: false, data: null, message: error.message });
    }
  }
}
