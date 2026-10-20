import { Request, Response } from 'express';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { ENV } from '../config/env';
import { DatabaseService } from '../services/storage/databaseService';

const db = DatabaseService.getInstance();

export class AuthController {
  static async register(req: Request, res: Response): Promise<void> {
    try {
      const email = req.body.email?.trim().toLowerCase();
      const password = req.body.password || req.body.passwordHash;

      if (!email || !password) {
        res.status(400).json({ success: false, data: null, message: 'البريد الإلكتروني وكلمة المرور مطلوبان' });
        return;
      }

      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        res.status(400).json({ success: false, data: null, message: 'صيغة البريد الإلكتروني غير صحيحة' });
        return;
      }

      if (password.length < 6) {
        res.status(400).json({ success: false, data: null, message: 'كلمة المرور يجب أن لا تقل عن 6 أحرف' });
        return;
      }

      const existingUser = db.getUserByEmail(email);
      if (existingUser) {
        res.status(400).json({ success: false, data: null, message: 'البريد الإلكتروني مستخدم بالفعل' });
        return;
      }

      const hashed = await bcrypt.hash(password, 10);
      const user = db.createUser(email, hashed);

      const token = jwt.sign({ userId: user.id, email: user.email }, ENV.JWT_SECRET, { expiresIn: '7d' });

      res.status(201).json({
        success: true,
        data: {
          token,
          userId: user.id,
          email: user.email,
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
      const email = req.body.email?.trim().toLowerCase();
      const password = req.body.password || req.body.passwordHash;

      if (!email || !password) {
        res.status(400).json({ success: false, data: null, message: 'البريد الإلكتروني وكلمة المرور مطلوبان' });
        return;
      }

      const user = db.getUserByEmail(email);
      if (!user) {
        res.status(401).json({ success: false, data: null, message: 'البريد الإلكتروني أو كلمة المرور غير صحيحة' });
        return;
      }

      const isPasswordValid = await bcrypt.compare(password, user.passwordHash);
      if (!isPasswordValid) {
        res.status(401).json({ success: false, data: null, message: 'البريد الإلكتروني أو كلمة المرور غير صحيحة' });
        return;
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
