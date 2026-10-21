package com.tredeig.app.data.repository

import com.tredeig.app.core.common.Resource
import com.tredeig.app.data.remote.api.ApiClient
import com.tredeig.app.data.remote.dto.KlineDto
import com.tredeig.app.domain.model.MarketTicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MarketRepositoryImpl {

    suspend fun getTicker(symbol: String = "BTCUSDT", exchange: String = "bybit"): Resource<MarketTicker> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.instance.getMarketTicker(symbol, exchange)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                Resource.Success(
                    MarketTicker(
                        symbol = data.symbol,
                        price = data.price,
                        high24h = data.high24h,
                        low24h = data.low24h,
                        volume24h = data.volume24h,
                        priceChangePercent = data.priceChangePercent,
                        timestamp = data.timestamp
                    )
                )
            } else {
                Resource.Error(response.body()?.message ?: "فشل جلب سعر السوق")
            }
        } catch (e: Exception) {
            Resource.Error("خطأ في الاتصال: ${e.localizedMessage}")
        }
    }

    suspend fun getKlines(symbol: String = "BTCUSDT", interval: String = "15m", exchange: String = "bybit"): Resource<List<KlineDto>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.instance.getKlines(symbol, interval, exchange)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data ?: emptyList())
            } else {
                Resource.Error(response.body()?.message ?: "فشل جلب بيانات الشموع اليابانية")
            }
        } catch (e: Exception) {
            Resource.Error("خطأ في الشبكة: ${e.localizedMessage}")
        }
    }
}
