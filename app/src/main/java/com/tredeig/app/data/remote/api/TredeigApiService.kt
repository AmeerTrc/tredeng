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

    @GET("api/security/risk")
    suspend fun getRiskSettings(
        @Header("Authorization") token: String
    ): Response<ApiResponse<RiskSettingsDto>>

    @POST("api/security/risk")
    suspend fun saveRiskSettings(
        @Header("Authorization") token: String,
        @Body request: RiskSettingsDto
    ): Response<ApiResponse<RiskSettingsDto>>

    @GET("api/market/coins/{exchangeName}")
    suspend fun getSelectedCoins(
        @Header("Authorization") token: String,
        @Path("exchangeName") exchangeName: String
    ): Response<ApiResponse<List<SelectedCoinDto>>>

    @POST("api/market/coins/{exchangeName}")
    suspend fun saveSelectedCoin(
        @Header("Authorization") token: String,
        @Path("exchangeName") exchangeName: String,
        @Body request: SaveSelectedCoinRequest
    ): Response<ApiResponse<Any>>
}
