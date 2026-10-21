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

    @DELETE("api/exchange/keys/{exchangeName}")
    suspend fun deleteExchangeKeys(
        @Header("Authorization") token: String,
        @Path("exchangeName") exchangeName: String
    ): Response<ApiResponse<Any>>

    @GET("api/exchange/all-status")
    suspend fun getAllExchangeStatus(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<ExchangeStatusResponse>>>

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
        @Path("symbol") symbol: String = "BTCUSDT",
        @Query("exchange") exchange: String = "bybit"
    ): Response<ApiResponse<TickerResponse>>

    @GET("api/market/klines/{symbol}")
    suspend fun getKlines(
        @Path("symbol") symbol: String = "BTCUSDT",
        @Query("interval") interval: String = "15m",
        @Query("exchange") exchange: String = "bybit"
    ): Response<ApiResponse<List<KlineDto>>>

    @GET("api/market/symbols/{exchangeName}")
    suspend fun getAvailableSymbols(
        @Header("Authorization") token: String,
        @Path("exchangeName") exchangeName: String
    ): Response<ApiResponse<List<String>>>

    @POST("api/security/kill-switch")
    suspend fun toggleKillSwitch(
        @Header("Authorization") token: String,
        @Body request: KillSwitchRequest
    ): Response<ApiResponse<Boolean>>

    @GET("api/security/settings")
    suspend fun getUserSettings(
        @Header("Authorization") token: String
    ): Response<ApiResponse<UserSettingsDto>>

    @POST("api/security/settings")
    suspend fun saveUserSettings(
        @Header("Authorization") token: String,
        @Body request: UserSettingsDto
    ): Response<ApiResponse<UserSettingsDto>>

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

    @GET("api/exchange/positions")
    suspend fun getOpenPositions(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<PaperTradeDto>>>

    @POST("api/exchange/positions/open")
    suspend fun openPosition(
        @Header("Authorization") token: String,
        @Body request: OpenPositionRequest
    ): Response<ApiResponse<Any>>

    @POST("api/exchange/positions/close")
    suspend fun closePosition(
        @Header("Authorization") token: String,
        @Body request: ClosePositionRequest
    ): Response<ApiResponse<Any>>

    @GET("api/exchange/history")
    suspend fun getTradeHistory(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<PaperTradeDto>>>
}
