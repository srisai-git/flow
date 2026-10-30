package com.simats.focusflow.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client for network communication with local PHP flow_backend.
 *
 * Network Notes:
 * - `http://10.0.2.2/flow_backend/` is the standard loopback IP for the Android Emulator to access localhost/XAMPP.
 * - When testing on a physical Android device over Wi-Fi, change the IP to your PC's LAN IP
 *   (e.g., `http://192.168.1.100/flow_backend/`) or run: `adb reverse tcp:80 tcp:80`.
 * - Cleartext HTTP is supported via `android:usesCleartextTraffic="true"` in AndroidManifest.xml.
 */
object RetrofitClient {

    private const val DEFAULT_BASE_URL = "http://172.23.176.89/flow_backend/"

    @Volatile
    private var customBaseUrl: String? = null

    val baseUrl: String
        get() = customBaseUrl ?: DEFAULT_BASE_URL

    /**
     * Allows configuring a custom server address at runtime (e.g., physical device LAN IP).
     */
    fun setBaseUrl(url: String) {
        val formattedUrl = if (url.endsWith("/")) url else "$url/"
        synchronized(this) {
            customBaseUrl = formattedUrl
            retrofitInstance = null
            apiServiceInstance = null
            flowApiServiceInstance = null
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
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

    /**
     * Singleton instance of the AuthApiService.
     */
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

    /**
     * Singleton instance of the FlowApiService (Productivity, Tasks, Timer, Habits, Profile).
     */
    val flowApiService: FlowApiService
        get() = flowApiServiceInstance ?: synchronized(this) {
            flowApiServiceInstance ?: getRetrofit().create(FlowApiService::class.java).also {
                flowApiServiceInstance = it
            }
        }
}

