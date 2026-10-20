package com.tredeig.app.domain.model

data class User(
    val id: String,
    val email: String,
    val isLiveTradingEnabled: Boolean,
    val killSwitchActive: Boolean
)
