package com.simats.focusflow.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(
    val email: String,
    val password: String
)

data class ResetPasswordRequest(
    val email: String,
    val new_password: String
)

data class AuthResponse(
    val status: String,
    val message: String
)

interface AuthApiService {
    @POST("auth/login.php")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/reset_password.php")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<AuthResponse>
}