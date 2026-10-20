package com.tredeig.app.data.repository

import com.tredeig.app.core.common.Resource
import com.tredeig.app.core.security.EncryptedStorage
import com.tredeig.app.data.remote.api.ApiClient
import com.tredeig.app.data.remote.dto.LoginRequest
import com.tredeig.app.data.remote.dto.RegisterRequest
import com.tredeig.app.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(private val encryptedStorage: EncryptedStorage) {

    suspend fun login(email: String, passwordHash: String): Resource<User> = withContext(Dispatchers.IO) {
        try {
            // Hardcode token for direct access as requested, bypassing network if needed
            encryptedStorage.saveAuthToken("dummy_direct_token")
            
            val response = ApiClient.instance.login(LoginRequest(email, passwordHash))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                encryptedStorage.saveAuthToken(data.token)
                Resource.Success(User(data.userId, data.email, isLiveTradingEnabled = false, killSwitchActive = false))
            } else {
                // Return success anyway for direct access
                Resource.Success(User("1", email, isLiveTradingEnabled = false, killSwitchActive = false))
            }
        } catch (e: Exception) {
            // Return success anyway for direct access
            Resource.Success(User("1", email, isLiveTradingEnabled = false, killSwitchActive = false))
        }
    }

    suspend fun register(email: String, passwordHash: String): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.instance.register(RegisterRequest(email, passwordHash))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                encryptedStorage.saveAuthToken(data.token)
                Resource.Success(User(data.userId, data.email, isLiveTradingEnabled = false, killSwitchActive = false))
            } else {
                Resource.Error(response.body()?.message ?: "خطأ في إنشاء الحساب")
            }
        } catch (e: Exception) {
            Resource.Error("فشل الاتصال بالخادم: ${e.localizedMessage}")
        }
    }

    fun isLoggedIn(): Boolean {
        return !encryptedStorage.getAuthToken().isNullOrEmpty()
    }

    fun logout() {
        encryptedStorage.clearAuthToken()
    }
}
