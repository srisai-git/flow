package com.simats.focusflow.data.repository

import android.util.Log
import com.simats.focusflow.data.api.FlowApiService
import com.simats.focusflow.data.api.RetrofitClient
import com.simats.focusflow.data.local.FocusSessionDatabase
import com.simats.focusflow.data.models.StreakData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing streak data, syncing over active ngrok/network endpoints,
 * and gracefully falling back to local Room database cache on offline/network errors.
 */
class StreakRepository(
    private val apiService: FlowApiService = RetrofitClient.flowApiService
) {
    companion object {
        private const val TAG = "StreakRepository"

        @Volatile
        private var instance: StreakRepository? = null

        fun getInstance(): StreakRepository {
            return instance ?: synchronized(this) {
                instance ?: StreakRepository().also { instance = it }
            }
        }
    }

    suspend fun getStreaks(userId: Int): Result<StreakData> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching streaks over network for userId: $userId")
            val response = apiService.getStreaks(userId)
            if (response.isSuccessful && response.body()?.status == "success") {
                val data = response.body()?.data ?: StreakData(userId = userId)
                Log.d(TAG, "Streaks fetched successfully: current=${data.currentStreak}, longest=${data.longestStreak}")
                Result.success(data)
            } else {
                Log.w(TAG, "Network streaks returned error code: ${response.code()}, falling back to local cache")
                Result.success(calculateFallbackStreak(userId))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Network streaks failed: ${e.message}, falling back to local DB cache")
            Result.success(calculateFallbackStreak(userId))
        }
    }

    suspend fun updateStreak(userId: Int, streakCount: Int): Result<StreakData> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating streak on backend for userId: $userId, count: $streakCount")
            val response = apiService.updateStreak(userId, streakCount)
            if (response.isSuccessful && response.body()?.status == "success") {
                val data = response.body()?.data ?: StreakData(userId = userId, currentStreak = streakCount)
                Result.success(data)
            } else {
                Result.success(calculateFallbackStreak(userId))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Network updateStreak failed: ${e.message}")
            Result.success(calculateFallbackStreak(userId))
        }
    }

    private suspend fun calculateFallbackStreak(userId: Int): StreakData {
        return try {
            val ctx = ProductivityRepository.appContext
            if (ctx == null) {
                return StreakData(userId = userId, currentStreak = 1, longestStreak = 1)
            }
            val db = FocusSessionDatabase.getInstance(ctx)
            val sessions = db.getRecentSessions(userId, 100)
            val days = sessions.mapNotNull { it.startedAt.take(10).ifBlank { null } }.distinct().size
            StreakData(
                userId = userId,
                currentStreak = maxOf(1, days),
                longestStreak = maxOf(1, days),
                totalActiveDays = maxOf(1, days)
            )
        } catch (e: Exception) {
            StreakData(userId = userId, currentStreak = 1, longestStreak = 1)
        }
    }
}
