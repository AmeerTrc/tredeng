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

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)
