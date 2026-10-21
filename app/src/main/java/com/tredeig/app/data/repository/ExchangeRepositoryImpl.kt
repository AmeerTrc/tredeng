package com.tredeig.app.data.repository

import com.tredeig.app.core.common.Resource
import com.tredeig.app.core.security.EncryptedStorage
import com.tredeig.app.data.remote.api.ApiClient
import com.tredeig.app.data.remote.dto.*
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
                val errorMsg = try {
                    val rawJson = response.errorBody()?.string()
                    if (!rawJson.isNullOrBlank()) {
                        org.json.JSONObject(rawJson).optString("message", null)
                    } else null
                } catch (_: Exception) {
                    null
                }
                Resource.Error(errorMsg ?: response.body()?.message ?: "فشل حفظ مفاتيح API")
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

    suspend fun getUserSettings(): Resource<UserSettingsDto> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.instance.getUserSettings(getAuthHeader())
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data ?: UserSettingsDto())
            } else {
                Resource.Error(response.body()?.message ?: "فشل جلب إعدادات المستخدم")
            }
        } catch (e: Exception) {
            Resource.Error("خطأ في الشبكة: ${e.localizedMessage}")
        }
    }

    suspend fun saveUserSettings(settings: UserSettingsDto): Resource<UserSettingsDto> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.instance.saveUserSettings(getAuthHeader(), settings)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data ?: settings)
            } else {
                Resource.Error(response.body()?.message ?: "فشل حفظ الإعدادات")
            }
        } catch (e: Exception) {
            Resource.Error("خطأ في الشبكة: ${e.localizedMessage}")
        }
    }

    suspend fun getAvailableSymbols(exchangeName: String = "binance"): Resource<List<String>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.instance.getAvailableSymbols(getAuthHeader(), exchangeName)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data ?: emptyList())
            } else {
                Resource.Error(response.body()?.message ?: "فشل جلب رموز التداول")
            }
        } catch (e: Exception) {
            Resource.Error("خطأ في الشبكة: ${e.localizedMessage}")
        }
    }

    suspend fun getSelectedCoins(exchangeName: String = "binance"): Resource<List<SelectedCoinDto>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.instance.getSelectedCoins(getAuthHeader(), exchangeName)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data ?: emptyList())
            } else {
                Resource.Error(response.body()?.message ?: "فشل جلب العملات")
            }
        } catch (e: Exception) {
            Resource.Error("خطأ في الشبكة: ${e.localizedMessage}")
        }
    }

    suspend fun saveSelectedCoin(exchangeName: String = "binance", symbol: String, isActive: Boolean, isFavorite: Boolean = false): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.instance.saveSelectedCoin(getAuthHeader(), exchangeName, SaveSelectedCoinRequest(symbol, isActive, isFavorite))
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "فشل حفظ العملة")
            }
        } catch (e: Exception) {
            Resource.Error("خطأ في الشبكة: ${e.localizedMessage}")
        }
    }

    suspend fun getOpenPositions(): Resource<List<PaperTradeDto>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.instance.getOpenPositions(getAuthHeader())
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data ?: emptyList())
            } else {
                Resource.Error(response.body()?.message ?: "فشل جلب الصفقات المفتوحة")
            }
        } catch (e: Exception) {
            Resource.Error("خطأ في الشبكة: ${e.localizedMessage}")
        }
    }

    suspend fun closePosition(tradeId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.instance.closePosition(getAuthHeader(), ClosePositionRequest(tradeId))
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "فشل إغلاق الصفقة")
            }
        } catch (e: Exception) {
            Resource.Error("خطأ في الشبكة: ${e.localizedMessage}")
        }
    }

    suspend fun getTradeHistory(): Resource<List<PaperTradeDto>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.instance.getTradeHistory(getAuthHeader())
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data ?: emptyList())
            } else {
                Resource.Error(response.body()?.message ?: "فشل جلب سجل الصفقات")
            }
        } catch (e: Exception) {
            Resource.Error("خطأ في الشبكة: ${e.localizedMessage}")
        }
    }
}
