import express from 'express';
import cors from 'cors';
import { ENV } from './config/env';
import authRoutes from './routes/authRoutes';
import exchangeRoutes from './routes/exchangeRoutes';
import marketRoutes from './routes/marketRoutes';
import securityRoutes from './routes/securityRoutes';
import { DatabaseService } from './services/storage/databaseService';
import { PaperTradingEngine } from './services/trading/paperTradingEngine';

const app = express();

app.use(cors());
app.use(express.json());

// Routes
app.use('/api/auth', authRoutes);
app.use('/api/exchange', exchangeRoutes);
app.use('/api/market', marketRoutes);
app.use('/api/security', securityRoutes);

// Root health check
app.get('/', (req, res) => {
  res.json({
    status: 'ONLINE',
    system: 'Tredeig Secure Backend Infrastructure',
    phase: 'PHASE_2_LIVE_GEMINI_AND_EXCHANGES',
    tradingEnabled: true
  });
});

// Start HTTP Server immediately so Railway proxy health checks succeed
const server = app.listen(ENV.PORT, '0.0.0.0', () => {
  console.log(`=======================================================`);
  console.log(` Tredeig Backend running on port ${ENV.PORT}`);
  console.log(` Phase 2 active: Database & Gemini AI ready.`);
  console.log(` Automated AI Trading Engine: ACTIVE (Interval: 30s)`);
  console.log(`=======================================================`);
});

// Initialize Database asynchronously
DatabaseService.getInstance().init().then(() => {
  console.log("Database initialized successfully.");

  // Launch Automated Background AI Trading Cycle (every 30 seconds)
  setInterval(async () => {
    try {
      const userIds = await DatabaseService.getInstance().getAllUserIds();
      for (const userId of userIds) {
        await PaperTradingEngine.runCycleForUser(userId);
      }
    } catch (err: any) {
      console.error("[Background Engine] Error during cycle:", err.message);
    }
  }, 30000);

}).catch(err => {
  console.error("Warning: Database initialization error:", err.message);
});
