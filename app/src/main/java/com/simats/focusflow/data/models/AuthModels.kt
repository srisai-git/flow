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
    val email: String = "",

    @SerializedName("avatar")
    val avatar: String = "",

    @SerializedName("avatar_url")
    val avatarUrl: String = "",

    @SerializedName("bio")
    val bio: String = ""
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

/**
 * Request payload for saving user preferences from the onboarding customization screen.
 */
data class SavePreferencesRequest(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("primary_goal")
    val primaryGoal: String,

    @SerializedName("preferred_focus_duration")
    val preferredFocusDuration: Int,

    @SerializedName("daily_focus_goal_minutes")
    val dailyFocusGoalMinutes: Int,

    @SerializedName("reminders_enabled")
    val remindersEnabled: Int = 1,

    @SerializedName("reminder_time")
    val reminderTime: String = "09:00 AM"
)

/**
 * Response payload returned when saving preferences.
 */
data class PreferencesResponse(
    @SerializedName("status")
    val status: Boolean = false,

    @SerializedName("message")
    val message: String = ""
)
