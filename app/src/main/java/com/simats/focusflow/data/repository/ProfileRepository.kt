package com.simats.focusflow.data.repository

import com.simats.focusflow.data.api.FlowApiService
import com.simats.focusflow.data.api.RetrofitClient
import com.simats.focusflow.data.models.ProfileData
import com.simats.focusflow.data.models.ResetStatsRequest
import com.simats.focusflow.data.models.SavePreferencesRequest
import com.simats.focusflow.data.models.UpdateProfileRequest
import com.simats.focusflow.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository handling user profile data retrieval, live updates,
 * local DataStore synchronization, and stats management.
 */
class ProfileRepository(
    private val apiService: FlowApiService = RetrofitClient.flowApiService
) {

    suspend fun getProfile(userId: Int): Result<ProfileData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProfile(userId)
            if (response.isSuccessful && response.body()?.status == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.success(fallbackProfile(userId))
            }
        } catch (e: Exception) {
            Result.success(fallbackProfile(userId))
        }
    }

    suspend fun updateProfile(
        userId: Int,
        name: String,
        email: String,
        bio: String = "",
        avatar: String = "",
        sessionManager: SessionManager? = null
    ): Result<ProfileData> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateProfileRequest(
                userId = userId,
                name = name,
                email = email,
                bio = bio,
                avatar = avatar
            )
            val response = apiService.updateProfile(request)
            if (response.isSuccessful && response.body()?.status == true) {
                val updated = response.body()?.data ?: ProfileData(
                    id = userId,
                    name = name,
                    email = email,
                    bio = bio,
                    avatar = avatar
                )
                sessionManager?.updateProfile(name, email, bio, avatar)
                Result.success(updated)
            } else {
                // Graceful fallback to update session locally
                sessionManager?.updateProfile(name, email, bio, avatar)
                Result.success(
                    ProfileData(
                        id = userId,
                        name = name,
                        email = email,
                        bio = bio,
                        avatar = avatar
                    )
                )
            }
        } catch (e: Exception) {
            sessionManager?.updateProfile(name, email, bio, avatar)
            Result.success(
                ProfileData(
                    id = userId,
                    name = name,
                    email = email,
                    bio = bio,
                    avatar = avatar
                )
            )
        }
    }

    suspend fun updatePreferences(
        userId: Int,
        dailyFocusGoalMinutes: Int,
        preferredFocusDuration: Int
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateProfileRequest(
                userId = userId,
                name = "",
                email = "",
                dailyFocusGoalMinutes = dailyFocusGoalMinutes,
                preferredFocusDuration = preferredFocusDuration
            )
            val response = apiService.updateProfile(request)
            try {
                RetrofitClient.authApiService.savePreferences(
                    SavePreferencesRequest(
                        userId = userId,
                        primaryGoal = "Study",
                        preferredFocusDuration = preferredFocusDuration,
                        dailyFocusGoalMinutes = dailyFocusGoalMinutes,
                        remindersEnabled = 1
                    )
                )
            } catch (_: Exception) {}
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.success(true)
        }
    }

    suspend fun resetStats(userId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.resetStats(ResetStatsRequest(userId))
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.success(true)
        }
    }

    private fun fallbackProfile(userId: Int): ProfileData {
        return ProfileData(
            id = userId,
            name = "Focus User",
            email = "",
            avatar = "",
            bio = "",
            createdAt = "",
            memberSince = "",
            totalFocusHours = 0f,
            totalFocusMinutes = 0,
            completedTasksCount = 0,
            currentStreak = 0,
            dailyFocusGoalMinutes = 240,
            preferredFocusDuration = 25,
            remindersEnabled = true
        )
    }
}

