package com.tredeig.app.data.remote.dto

data class SaveExchangeKeysRequest(
    val exchangeName: String,
    val apiKey: String,
    val apiSecret: String
)

data class ExchangeStatusResponse(
    val exchangeName: String,
    val isConnected: Boolean,
    val apiKeyMasked: String,
    val permissions: List<String>,
    val canTradeSpot: Boolean,
    val canWithdraw: Boolean
)

data class BalanceResponse(
    val asset: String,
    val free: Double,
    val locked: Double,
    val total: Double
)

data class PortfolioResponse(
    val totalBalanceUsdt: Double,
    val availableBalanceUsdt: Double,
    val inOrdersUsdt: Double,
    val balances: List<BalanceResponse>
)

data class KillSwitchRequest(
    val active: Boolean,
    val closePositionsHandledManually: Boolean = true
)

data class UserSettingsDto(
    val totalCapitalUsdt: Double = 1000.0,
    val tradeAmountUsdt: Double = 100.0,
    val entryPrice: Double? = null,
    val exitPrice: Double? = null,
    val stopLossPricePct: Double = 2.0,
    val takeProfitPricePct: Double = 4.0,
    val maxDailyLossPct: Double = 1.0,
    val maxDailyTrades: Int = 5,
    val maxOpenPositions: Int = 3,
    val tradingMode: String = "AUTOMATED_AI", // "AUTOMATED_AI" or "MANUAL"
    val selectedExchange: String = "binance", // "binance", "okx", "bybit"
    val isPaperTrading: Boolean = true,
    val stopLossRequired: Boolean = true
)

data class SelectedCoinDto(
    val symbol: String,
    val isActive: Boolean,
    val isFavorite: Boolean = false,
    val price: Double? = null,
    val priceChangePercent: Double? = null
)

data class SaveSelectedCoinRequest(
    val symbol: String,
    val isActive: Boolean,
    val isFavorite: Boolean = false
)

data class PaperTradeDto(
    val id: String,
    val exchangeName: String,
    val symbol: String,
    val action: String,
    val price: Double,
    val amount: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val status: String,
    val pnl: Double,
    val createdAt: Long
)

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)
