import express from 'express';
import cors from 'cors';
import { ENV } from './config/env';
import authRoutes from './routes/authRoutes';
import exchangeRoutes from './routes/exchangeRoutes';
import marketRoutes from './routes/marketRoutes';
import securityRoutes from './routes/securityRoutes';
import { DatabaseService } from './services/storage/databaseService';

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
    tradingEnabled: false
  });
});

// Start HTTP Server immediately so Railway proxy health checks succeed
const server = app.listen(ENV.PORT, '0.0.0.0', () => {
  console.log(`=======================================================`);
  console.log(` Tredeig Backend running on port ${ENV.PORT}`);
  console.log(` Phase 2 active: Database & Gemini AI ready.`);
  console.log(`=======================================================`);
});

// Initialize Database asynchronously
DatabaseService.getInstance().init().then(() => {
  console.log("Database initialized successfully.");
}).catch(err => {
  console.error("Warning: Database initialization error:", err.message);
});
