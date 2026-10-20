package com.tredeig.app.data.remote.api

import com.tredeig.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface TredeigApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    @POST("api/exchange/keys")
    suspend fun saveExchangeKeys(
        @Header("Authorization") token: String,
        @Body request: SaveExchangeKeysRequest
    ): Response<ApiResponse<ExchangeStatusResponse>>

    @GET("api/exchange/status")
    suspend fun getExchangeStatus(
        @Header("Authorization") token: String
    ): Response<ApiResponse<ExchangeStatusResponse>>

    @GET("api/exchange/balance")
    suspend fun getAccountBalance(
        @Header("Authorization") token: String
    ): Response<ApiResponse<PortfolioResponse>>

    @GET("api/market/ticker/{symbol}")
    suspend fun getMarketTicker(
        @Path("symbol") symbol: String = "BTCUSDT"
    ): Response<ApiResponse<TickerResponse>>

    @POST("api/security/kill-switch")
    suspend fun toggleKillSwitch(
        @Header("Authorization") token: String,
        @Body request: KillSwitchRequest
    ): Response<ApiResponse<Boolean>>
}
