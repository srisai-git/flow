package com.simats.focusflow.data.models

import com.google.gson.annotations.SerializedName

/**
 * User data payload returned by the authentication endpoints.
 */
data class User(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("email")
    val email: String = ""
)

/**
 * Request payload for logging in to flow_backend.
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

/**
 * Request payload for registering a new user in flow_backend.
 */
data class RegisterRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

/**
 * Response payload returned from flow_backend auth endpoints.
 */
data class AuthResponse(
    @SerializedName("status")
    val status: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("user")
    val user: User? = null,

    @SerializedName("user_id")
    val userId: Int? = null,

    @SerializedName("token")
    val token: String? = null
) {
    val isSuccess: Boolean
        get() = status
}

