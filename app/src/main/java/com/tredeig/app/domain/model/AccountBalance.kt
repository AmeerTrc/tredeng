package com.tredeig.app.domain.model

data class AccountBalance(
    val asset: String,
    val free: Double,
    val locked: Double,
    val total: Double
)

data class PortfolioSummary(
    val totalBalanceUsdt: Double,
    val availableBalanceUsdt: Double,
    val inOrdersUsdt: Double,
    val balances: List<AccountBalance>
)
