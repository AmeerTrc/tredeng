package com.tredeig.app.data.remote.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // Railway Deployed URL (Production) / Fallback to local
    private var baseUrl: String = "https://tredeng-production.up.railway.app/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private var retrofitInstance: TredeigApiService? = null

    fun updateBaseUrl(newUrl: String) {
        val formattedUrl = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        baseUrl = formattedUrl
        retrofitInstance = createApiService(formattedUrl)
    }

    private fun createApiService(url: String): TredeigApiService {
        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TredeigApiService::class.java)
    }

    val instance: TredeigApiService
        get() {
            if (retrofitInstance == null) {
                retrofitInstance = createApiService(baseUrl)
            }
            return retrofitInstance!!
        }
}
