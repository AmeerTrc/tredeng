import express from 'express';
import cors from 'cors';
import { ENV } from './config/env';
import authRoutes from './routes/authRoutes';
import exchangeRoutes from './routes/exchangeRoutes';
import marketRoutes from './routes/marketRoutes';
import securityRoutes from './routes/securityRoutes';

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

app.listen(ENV.PORT, () => {
  console.log(`=======================================================`);
  console.log(` Tredeig Backend running on http://localhost:${ENV.PORT}`);
  console.log(` Phase 1 active: Spot Balance & Market Data ONLY.`);
  console.log(` Live trading orders disabled until Phase 2 trigger.`);
  console.log(`=======================================================`);
});
