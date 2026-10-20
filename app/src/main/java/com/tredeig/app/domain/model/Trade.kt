package com.tredeig.app.domain.model

data class Trade(
    val id: String,
    val symbol: String,
    val side: String,
    val quantity: Double,
    val entryPrice: Double,
    val currentPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val pnlUsdt: Double,
    val pnlPercent: Double,
    val status: String,
    val timestamp: Long
)
