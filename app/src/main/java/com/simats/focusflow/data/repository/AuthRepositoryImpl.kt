package com.simats.focusflow.data.repository

import android.util.Log
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

class AuthRepositoryImpl(
    private val apiService: AuthApiService = RetrofitClient.instance,
    private val gson: Gson = Gson()
) : AuthRepository {

    override suspend fun login(request: LoginRequest): Result<AuthResponse> = withContext(Dispatchers.IO) {
        val targetUrl = "${RetrofitClient.baseUrl}login.php"
        try {
            val response = apiService.login(request)
            val requestUrl = response.raw().request.url.toString().ifBlank { targetUrl }
            val statusCode = response.code()
            Log.d("FlowAuth", "Request URL: $requestUrl | HTTP Status: $statusCode")
            handleResponse(response)
        } catch (e: Exception) {
            Log.e("FlowAuth", "Request URL: $targetUrl | Error: ${e.message}", e)
            Result.failure(
                Exception(e.localizedMessage ?: "Cannot connect to auth server at $targetUrl")
            )
        }
    }

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> = withContext(Dispatchers.IO) {
        val targetUrl = "${RetrofitClient.baseUrl}register.php"
        try {
            val response = apiService.register(request)
            val requestUrl = response.raw().request.url.toString().ifBlank { targetUrl }
            val statusCode = response.code()
            Log.d("FlowAuth", "Request URL: $requestUrl | HTTP Status: $statusCode")
            handleResponse(response)
        } catch (e: Exception) {
            Log.e("FlowAuth", "Request URL: $targetUrl | Error: ${e.message}", e)
            Result.failure(
                Exception(e.localizedMessage ?: "Cannot connect to auth server at $targetUrl")
            )
        }
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
