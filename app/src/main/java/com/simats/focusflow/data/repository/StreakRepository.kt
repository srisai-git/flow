package com.simats.focusflow.data.repository

import android.util.Log
import com.simats.focusflow.data.api.FlowApiService
import com.simats.focusflow.data.api.RetrofitClient
import com.simats.focusflow.data.local.FocusSessionDatabase
import com.simats.focusflow.data.models.StreakData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StreakRepository(
    private val apiService: FlowApiService = RetrofitClient.flowApiService
) {
    companion object {
        private const val TAG = "StreakRepository"

        @Volatile
        private var instance: StreakRepository? = null

        @Volatile
        private var cachedStreakData: StreakData? = null
        @Volatile
        private var cachedStreakTime = 0L
        @Volatile
        private var cachedStreakUserId = -1

        fun getInstance(): StreakRepository {
            return instance ?: synchronized(this) {
                instance ?: StreakRepository().also { instance = it }
            }
        }
    }

    suspend fun getStreaks(userId: Int): Result<StreakData> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (cachedStreakUserId == userId && now - cachedStreakTime < 5000L && cachedStreakData != null) {
            return@withContext Result.success(cachedStreakData!!)
        }
        try {
            Log.d(TAG, "Fetching streaks over network for userId: $userId")
            val response = apiService.getStreaks(userId)
            if (response.isSuccessful && response.body()?.status == "success") {
                val data = response.body()?.data ?: StreakData(userId = userId)
                cachedStreakData = data
                cachedStreakTime = now
                cachedStreakUserId = userId
                Log.d(TAG, "Streaks fetched successfully: current=${data.currentStreak}, longest=${data.longestStreak}")
                Result.success(data)
            } else {
                val fallback = calculateFallbackStreak(userId)
                cachedStreakData = fallback
                cachedStreakTime = now
                cachedStreakUserId = userId
                Log.w(TAG, "Network streaks returned error code: ${response.code()}, falling back to local cache")
                Result.success(fallback)
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
                cachedStreakData = data
                cachedStreakTime = System.currentTimeMillis()
                cachedStreakUserId = userId
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
                ?: return StreakData(userId = userId, currentStreak = 0, longestStreak = 0, totalActiveDays = 0)
            val db = FocusSessionDatabase.getInstance(ctx)
            val sessions = db.getRecentSessions(userId, 500).filter {
                it.status.equals("completed", ignoreCase = true) && !it.sessionType.contains("break", ignoreCase = true)
            }
            val tasks = db.getLocalTasks(userId).filter { it.isCompleted }
            val activeDates = mutableSetOf<String>()
            for (s in sessions) {
                val d = if (s.completedAt.length >= 10) s.completedAt.take(10) else s.startedAt.take(10)
                if (d.length == 10 && d.contains("-")) activeDates.add(d)
            }
            for (t in tasks) {
                val d = t.createdAt?.take(10) ?: t.dueDate.take(10)
                if (d.length == 10 && d.contains("-")) activeDates.add(d)
            }
            val habits = try {
                ProductivityRepository().getHabits(userId).getOrNull()?.habits ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            if (habits.any { it.isCompletedToday }) {
                activeDates.add(todayStr)
            }

            val sortedDates = activeDates.sortedDescending()
            if (sortedDates.isEmpty()) {
                return StreakData(userId = userId, currentStreak = 0, longestStreak = 0, totalActiveDays = 0)
            }

            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayDate = cal.time
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayDate = cal.time
            val yesterdayStr = sdf.format(yesterdayDate)

            var currentStreak = 0
            if (sortedDates.first() == todayStr) {
                val checkCal = Calendar.getInstance().apply { time = todayDate }
                for (d in sortedDates) {
                    if (d == sdf.format(checkCal.time)) {
                        currentStreak++
                        checkCal.add(Calendar.DAY_OF_YEAR, -1)
                    } else {
                        break
                    }
                }
            } else if (sortedDates.first() == yesterdayStr) {
                val checkCal = Calendar.getInstance().apply { time = yesterdayDate }
                for (d in sortedDates) {
                    if (d == sdf.format(checkCal.time)) {
                        currentStreak++
                        checkCal.add(Calendar.DAY_OF_YEAR, -1)
                    } else {
                        break
                    }
                }
            } else {
                currentStreak = 0
            }

            var longestStreak = 0
            var tempStreak = 0
            var prevDate: Date? = null
            val ascDates = sortedDates.reversed().mapNotNull {
                try { sdf.parse(it) } catch (_: Exception) { null }
            }
            for (cur in ascDates) {
                if (prevDate == null) {
                    tempStreak = 1
                } else {
                    val diff = ((cur.time - prevDate.time) / (1000 * 60 * 60 * 24)).toInt()
                    if (diff == 1) {
                        tempStreak++
                    } else if (diff > 1) {
                        tempStreak = 1
                    }
                }
                prevDate = cur
                if (tempStreak > longestStreak) {
                    longestStreak = tempStreak
                }
            }
            if (longestStreak < currentStreak) {
                longestStreak = currentStreak
            }

            StreakData(
                userId = userId,
                currentStreak = currentStreak,
                longestStreak = longestStreak,
                totalActiveDays = sortedDates.size,
                lastActiveDate = sortedDates.firstOrNull()
            )
        } catch (e: Exception) {
            StreakData(userId = userId, currentStreak = 0, longestStreak = 0, totalActiveDays = 0)
        }
    }
}
