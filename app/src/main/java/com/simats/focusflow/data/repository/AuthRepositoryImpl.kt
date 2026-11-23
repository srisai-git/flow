package com.simats.focusflow.data.repository

import com.google.gson.Gson
import com.simats.focusflow.data.api.AuthApiService
import com.simats.focusflow.data.api.RetrofitClient
import com.simats.focusflow.data.models.AuthResponse
import com.simats.focusflow.data.models.LoginRequest
import com.simats.focusflow.data.models.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import retrofit2.Response
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class AuthRepositoryImpl(
    private val apiService: AuthApiService = RetrofitClient.instance,
    private val gson: Gson = Gson()
) : AuthRepository {

    override suspend fun login(request: LoginRequest): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(request)
            val res = handleResponse(response)
            if (res.isSuccess) return@withContext res
            val fallback = tryFallbackAuth(request)
            if (fallback != null) return@withContext fallback
            res
        } catch (e: IOException) {
            val fallback = tryFallbackAuth(request)
            if (fallback != null) return@withContext fallback
            Result.failure(
                Exception("Cannot connect to server. Please ensure XAMPP is running and your device can reach the host at ${RetrofitClient.baseUrl}.")
            )
        } catch (e: Exception) {
            Result.failure(
                Exception(e.localizedMessage ?: "An unexpected error occurred during login.")
            )
        }
    }

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(request)
            val res = handleResponse(response)
            if (res.isSuccess) return@withContext res
            val fallback = tryFallbackRegister(request)
            if (fallback != null) return@withContext fallback
            res
        } catch (e: IOException) {
            val fallback = tryFallbackRegister(request)
            if (fallback != null) return@withContext fallback
            Result.failure(
                Exception("Cannot connect to server. Please ensure XAMPP is running and your device can reach the host at ${RetrofitClient.baseUrl}.")
            )
        } catch (e: Exception) {
            Result.failure(
                Exception(e.localizedMessage ?: "An unexpected error occurred during signup.")
            )
        }
    }

    private fun tryFallbackAuth(request: LoginRequest): Result<AuthResponse>? {
        val fallbackUrls = listOf(
            "http://10.0.2.2/flow_backend/login.php",
            "http://172.23.176.89/flow_backend/login.php",
            "http://127.0.0.1/flow_backend/login.php"
        )
        val client = RetrofitClient.okHttpClient
        val jsonBody = gson.toJson(request).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        for (url in fallbackUrls) {
            try {
                val req = okhttp3.Request.Builder().url(url).post(jsonBody).build()
                val resp = client.newCall(req).execute()
                if (resp.isSuccessful) {
                    val respStr = resp.body?.string() ?: ""
                    val parsed = gson.fromJson(respStr, AuthResponse::class.java)
                    if (parsed != null && parsed.status) {
                        return Result.success(parsed)
                    }
                }
            } catch (_: Exception) {}
        }
        return null
    }

    private fun tryFallbackRegister(request: RegisterRequest): Result<AuthResponse>? {
        val fallbackUrls = listOf(
            "http://10.0.2.2/flow_backend/register.php",
            "http://172.23.176.89/flow_backend/register.php",
            "http://127.0.0.1/flow_backend/register.php"
        )
        val client = RetrofitClient.okHttpClient
        val jsonBody = gson.toJson(request).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        for (url in fallbackUrls) {
            try {
                val req = okhttp3.Request.Builder().url(url).post(jsonBody).build()
                val resp = client.newCall(req).execute()
                if (resp.isSuccessful) {
                    val respStr = resp.body?.string() ?: ""
                    val parsed = gson.fromJson(respStr, AuthResponse::class.java)
                    if (parsed != null && parsed.status) {
                        return Result.success(parsed)
                    }
                }
            } catch (_: Exception) {}
        }
        return null
    }

    override fun loginFlow(request: LoginRequest): Flow<Result<AuthResponse>> = flow {
        emit(login(request))
    }.flowOn(Dispatchers.IO)

    override fun registerFlow(request: RegisterRequest): Flow<Result<AuthResponse>> = flow {
        emit(register(request))
    }.flowOn(Dispatchers.IO)

    

    private fun handleResponse(response: Response<AuthResponse>): Result<AuthResponse> {
        if (response.isSuccessful) {
            val body = response.body()
            return if (body != null && body.status) {
                Result.success(body)
            } else {
                val errorMsg = body?.message?.ifBlank { null } ?: "Operation failed."
                Result.failure(Exception(errorMsg))
            }
        } else {
            val errorMessage = try {
                val rawJson = response.errorBody()?.string()
                if (!rawJson.isNullOrBlank()) {
                    val parsed = gson.fromJson(rawJson, AuthResponse::class.java)
                    parsed?.message?.ifBlank { null } ?: "Server returned error (${response.code()})"
                } else {
                    "Server returned error (${response.code()})"
                }
            } catch (ex: Exception) {
                "Server returned error (${response.code()}): ${response.message()}"
            }
            return Result.failure(Exception(errorMessage))
        }
    }
}

