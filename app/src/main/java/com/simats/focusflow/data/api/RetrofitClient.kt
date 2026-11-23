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

    private const val DEFAULT_BASE_URL = "https://reaction-union-sublevel.ngrok-free.dev/flow_backend/"
    const val LOCAL_BASE_URL = "http://10.0.2.2/flow_backend/"
    const val LAN_BASE_URL = "http://172.23.176.89/flow_backend/"
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
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("ngrok-skip-browser-warning", "true")
                    .build()
                chain.proceed(request)
            }
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

    @Volatile
    private var grootRetrofitInstance: Retrofit? = null

    private fun getGrootRetrofit(): Retrofit {
        return grootRetrofitInstance ?: synchronized(this) {
            grootRetrofitInstance ?: Retrofit.Builder()
                .baseUrl(GROOT_AI_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .also { grootRetrofitInstance = it }
        }
    }

    @Volatile
    private var grootApiServiceInstance: GrootApiService? = null

    val grootApiService: GrootApiService
        get() = grootApiServiceInstance ?: synchronized(this) {
            grootApiServiceInstance ?: getGrootRetrofit().create(GrootApiService::class.java).also {
                grootApiServiceInstance = it
            }
        }
}

