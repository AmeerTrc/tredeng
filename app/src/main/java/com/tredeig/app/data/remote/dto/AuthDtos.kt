package com.tredeig.app.data.remote.dto

data class LoginRequest(
    val email: String,
    val passwordHash: String
)

data class RegisterRequest(
    val email: String,
    val passwordHash: String
)

data class AuthResponse(
    val token: String,
    val userId: String,
    val email: String,
    val message: String? = null
)
