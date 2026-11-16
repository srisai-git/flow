package com.simats.focusflow.data.models

import com.google.gson.annotations.SerializedName



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



data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)



data class RegisterRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)



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



data class PreferencesResponse(
    @SerializedName("status")
    val status: Boolean = false,

    @SerializedName("message")
    val message: String = ""
)



data class UpdateTokenRequest(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("fcm_token")
    val fcmToken: String
)



data class UpdateTokenResponse(
    @SerializedName("status")
    val status: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("user_id")
    val userId: Int? = null
)
