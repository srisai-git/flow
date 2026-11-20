package com.simats.focusflow.data.api

import com.simats.focusflow.data.repository.ProductivityRepository
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit



object RetrofitClient {

    private const val DEFAULT_BASE_URL = "https://focusflow-groot-ai.onrender.com/"
    const val GROOT_AI_BASE_URL = "https://focusflow-groot-ai.onrender.com/"

    @Volatile
    private var customBaseUrl: String? = null

    val baseUrl: String
        get() = (customBaseUrl ?: DEFAULT_BASE_URL).trim()

    fun setBaseUrl(url: String) {
        val formattedUrl = if (url.endsWith("/")) url else "$url/"
        synchronized(this) {
            customBaseUrl = formattedUrl
            retrofitInstance = null
            apiServiceInstance = null
            flowApiServiceInstance = null
        }
    }

    val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
            .retryOnConnectionFailure(true)
        ProductivityRepository.appContext?.cacheDir?.let { cacheDir ->
            try {
                builder.cache(Cache(File(cacheDir, "http_cache"), 10L * 1024 * 1024))
            } catch (_: Exception) {
            }
        }
        builder.build()
    }

    @Volatile
    private var retrofitInstance: Retrofit? = null

    private fun getRetrofit(): Retrofit {
        return retrofitInstance ?: synchronized(this) {
            retrofitInstance ?: Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .also { retrofitInstance = it }
        }
    }

    @Volatile
    private var apiServiceInstance: AuthApiService? = null

    

    val instance: AuthApiService
        get() = apiServiceInstance ?: synchronized(this) {
            apiServiceInstance ?: getRetrofit().create(AuthApiService::class.java).also {
                apiServiceInstance = it
            }
        }

    val authApiService: AuthApiService
        get() = instance

    @Volatile
    private var flowApiServiceInstance: FlowApiService? = null

    

    val flowApiService: FlowApiService
        get() = flowApiServiceInstance ?: synchronized(this) {
            flowApiServiceInstance ?: getRetrofit().create(FlowApiService::class.java).also {
                flowApiServiceInstance = it
            }
        }
}

