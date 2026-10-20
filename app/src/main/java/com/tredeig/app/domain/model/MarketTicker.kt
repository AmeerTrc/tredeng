package com.tredeig.app.domain.model

data class MarketTicker(
    val symbol: String,
    val price: Double,
    val high24h: Double,
    val low24h: Double,
    val volume24h: Double,
    val priceChangePercent: Double,
    val timestamp: Long
)
