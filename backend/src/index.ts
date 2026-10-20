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
    phase: 'PHASE_1_BALANCE_AND_MARKET_MONITORING',
    tradingEnabled: false
  });
});

// Init DB and Start Server
DatabaseService.getInstance().init().then(() => {
  app.listen(ENV.PORT, '0.0.0.0', () => {
    console.log(`=======================================================`);
    console.log(` Tredeig Backend running on port ${ENV.PORT}`);
    console.log(` Phase 2 active: Database initialized. Independent Exchanges + Live Settings.`);
    console.log(`=======================================================`);
  });
}).catch(err => {
  console.error("Failed to initialize database", err);
  process.exit(1);
});
