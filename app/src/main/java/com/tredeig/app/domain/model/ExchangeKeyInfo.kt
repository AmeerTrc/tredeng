package com.tredeig.app.domain.model

data class ExchangeKeyInfo(
    val exchangeName: String,
    val isConnected: Boolean,
    val apiKeyMasked: String,
    val isSpotEnabled: Boolean,
    val isWithdrawalDisabled: Boolean
)
