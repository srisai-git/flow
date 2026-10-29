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
import java.io.IOException

/**
 * Concrete implementation of AuthRepository handling API calls,
 * thread-switching via Coroutines Dispatchers.IO, and graceful error parsing.
 */
class AuthRepositoryImpl(
    private val apiService: AuthApiService = RetrofitClient.instance,
    private val gson: Gson = Gson()
) : AuthRepository {

    override suspend fun login(request: LoginRequest): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(request)
            handleResponse(response)
        } catch (e: IOException) {
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
            handleResponse(response)
        } catch (e: IOException) {
            Result.failure(
                Exception("Cannot connect to server. Please ensure XAMPP is running and your device can reach the host at ${RetrofitClient.baseUrl}.")
            )
        } catch (e: Exception) {
            Result.failure(
                Exception(e.localizedMessage ?: "An unexpected error occurred during signup.")
            )
        }
    }

    override fun loginFlow(request: LoginRequest): Flow<Result<AuthResponse>> = flow {
        emit(login(request))
    }.flowOn(Dispatchers.IO)

    override fun registerFlow(request: RegisterRequest): Flow<Result<AuthResponse>> = flow {
        emit(register(request))
    }.flowOn(Dispatchers.IO)

    /**
     * Inspects HTTP response, deserializing JSON error bodies when status code is 4xx or 5xx.
     */
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
            // Parse error response payload from PHP (e.g. {"status": false, "message": "Email already registered."})
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

