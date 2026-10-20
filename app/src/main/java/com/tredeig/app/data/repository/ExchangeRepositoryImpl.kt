package com.tredeig.app.data.repository

import com.tredeig.app.core.common.Resource
import com.tredeig.app.core.security.EncryptedStorage
import com.tredeig.app.data.remote.api.ApiClient
import com.tredeig.app.data.remote.dto.KillSwitchRequest
import com.tredeig.app.data.remote.dto.SaveExchangeKeysRequest
import com.tredeig.app.domain.model.AccountBalance
import com.tredeig.app.domain.model.ExchangeKeyInfo
import com.tredeig.app.domain.model.PortfolioSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExchangeRepositoryImpl(private val encryptedStorage: EncryptedStorage) {

    private fun getAuthHeader(): String {
        val token = encryptedStorage.getAuthToken() ?: ""
        return "Bearer $token"
    }

    suspend fun saveKeys(exchangeName: String, apiKey: String, apiSecret: String): Resource<ExchangeKeyInfo> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.instance.saveExchangeKeys(
                getAuthHeader(),
                SaveExchangeKeysRequest(exchangeName, apiKey, apiSecret)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                Resource.Success(
                    ExchangeKeyInfo(
                        exchangeName = data.exchangeName,
                        isConnected = data.isConnected,
                        apiKeyMasked = data.apiKeyMasked,
                        isSpotEnabled = data.canTradeSpot,
                        isWithdrawalDisabled = !data.canWithdraw
                    )
                )
            } else {
                Resource.Error(response.body()?.message ?: "فشل حفظ مفاتيح API")
            }
        } catch (e: Exception) {
            Resource.Error("خطأ في الشبكة: ${e.localizedMessage}")
        }
    }

    suspend fun getPortfolioSummary(): Resource<PortfolioSummary> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.instance.getAccountBalance(getAuthHeader())
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                val balances = data.balances.map {
                    AccountBalance(it.asset, it.free, it.locked, it.total)
                }
                Resource.Success(
                    PortfolioSummary(
                        totalBalanceUsdt = data.totalBalanceUsdt,
                        availableBalanceUsdt = data.availableBalanceUsdt,
                        inOrdersUsdt = data.inOrdersUsdt,
                        balances = balances
                    )
                )
            } else {
                Resource.Error(response.body()?.message ?: "فشل جلب الرصيد")
            }
        } catch (e: Exception) {
            Resource.Error("خطأ في الاتصال بالخادم: ${e.localizedMessage}")
        }
    }

    suspend fun toggleKillSwitch(active: Boolean): Resource<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.instance.toggleKillSwitch(getAuthHeader(), KillSwitchRequest(active))
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data ?: active)
            } else {
                Resource.Error(response.body()?.message ?: "فشل تغيير حالة زر الطوارئ")
            }
        } catch (e: Exception) {
            Resource.Error("خطأ في الشبكة: ${e.localizedMessage}")
        }
    }
}
