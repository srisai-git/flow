package com.simats.focusflow.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class LoginRequest(
    val email: String,
    val password: String
)

data class ResetPasswordRequest(
    val email: String,
    val new_password: String
)

data class UpdateAvatarRequest(
    val user_id: Int,
    val avatar: String
)

data class SetupProfileRequest(
    val user_id: Int,
    val name: String,
    val avatar: String?,
    val bio: String?
)

data class UserProfile(
    val id: Int = 1,
    val name: String,
    val avatar: String? = "👤",
    val role: String,
    val bio: String? = null,
    val location: String? = null,
    val email: String
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

    @GET("user/profile.php")
    suspend fun getProfile(@Query("user_id") userId: Int): Response<UserProfile>

    @POST("user/update_profile.php")
    suspend fun updateProfile(@Body profile: UserProfile): Response<AuthResponse>

    @POST("profile/update_avatar.php")
    suspend fun updateAvatar(@Body request: UpdateAvatarRequest): Response<AuthResponse>

    @POST("profile/setup_profile.php")
    suspend fun setupProfile(@Body request: SetupProfileRequest): Response<AuthResponse>
}