package com.simats.focusflow.data.repository

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import android.content.Context
import com.simats.focusflow.data.api.FlowApiService
import com.simats.focusflow.data.api.RetrofitClient
import com.simats.focusflow.data.local.FocusSessionDatabase
import com.simats.focusflow.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext

/**
 * Repository handling data caching, network dispatching,
 * and error fallbacks for Dashboard, Tasks, Timer, and Habits.
 */
class ProductivityRepository(
    private val apiService: FlowApiService = RetrofitClient.flowApiService
) {

    companion object {
        private val _sessionLoggedEventFlow = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 10)
        val sessionLoggedEventFlow: SharedFlow<Unit> = _sessionLoggedEventFlow.asSharedFlow()

        @Volatile
        private var lastLogSessionTime = 0L
        @Volatile
        private var lastLogSessionParams = ""

        @Volatile
        var appContext: Context? = null

        fun init(context: Context) {
            appContext = context.applicationContext
        }

        fun clearUserCache() {
            lastLogSessionTime = 0L
            lastLogSessionParams = ""
        }

        fun notifySessionLogged() {
            _sessionLoggedEventFlow.tryEmit(Unit)
            FocusSessionDatabase.notifyTableChanged()
        }
    }

    suspend fun getDashboard(userId: Int): Result<DashboardData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDashboard(userId)
            if (response.isSuccessful && response.body()?.status == true) {
                val data = response.body()?.data ?: DashboardData()
                Result.success(data)
            } else {
                Result.success(DashboardData())
            }
        } catch (e: Exception) {
            // Clean initial state for offline
            Result.success(
                DashboardData(
                    userName = "Focus User",
                    focusMinutesToday = 0,
                    focusGoalMinutes = 240,
                    pendingTaskCount = 0,
                    completedTaskCount = 0,
                    streakDays = 0,
                    priorityTasks = emptyList(),
                    habits = emptyList()
                )
            )
        }
    }

    suspend fun getTasks(userId: Int): Result<List<TaskItem>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTasks(userId)
            if (response.isSuccessful && response.body()?.status == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }

    suspend fun createTask(
        userId: Int,
        title: String,
        description: String = "",
        priority: String = "medium",
        dueDate: String = "",
        category: String = "General"
    ): Result<TaskItem> = withContext(Dispatchers.IO) {
        try {
            val request = CreateTaskRequest(userId, title, description, priority, dueDate, category)
            val response = apiService.createTask(request)
            if (response.isSuccessful && response.body()?.status == true) {
                val created = response.body()?.data ?: TaskItem(
                    id = System.currentTimeMillis().toInt(),
                    userId = userId,
                    title = title,
                    description = description,
                    priority = priority,
                    dueDate = dueDate,
                    category = category
                )
                Result.success(created)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to create task"))
            }
        } catch (e: Exception) {
            // Local fallback
            Result.success(
                TaskItem(
                    id = (System.currentTimeMillis() % 100000).toInt(),
                    userId = userId,
                    title = title,
                    description = description,
                    priority = priority,
                    dueDate = dueDate,
                    category = category
                )
            )
        }
    }

    suspend fun toggleTask(userId: Int, taskId: Int, isCompleted: Boolean? = null): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = ToggleTaskRequest(userId, taskId, "toggle", isCompleted)
            val response = apiService.toggleTask(request)
            if (response.isSuccessful && response.body()?.status == true) {
                Result.success(response.body()?.isCompleted ?: true)
            } else {
                Result.success(isCompleted ?: true)
            }
        } catch (e: Exception) {
            Result.success(isCompleted ?: true)
        }
    }

    suspend fun deleteTask(userId: Int, taskId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = DeleteTaskRequest(userId, taskId)
            val response = apiService.deleteTask(request)
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.success(true)
        }
    }

    suspend fun logSession(
        userId: Int,
        durationMinutes: Int,
        sessionType: String = "pomodoro",
        taskId: Int? = null,
        durationSeconds: Int = durationMinutes * 60,
        startTime: String? = null,
        endTime: String? = null,
        taskTitle: String? = null,
        taskCategory: String? = null,
        sessionUuid: String = java.util.UUID.randomUUID().toString()
    ): Result<LogSessionResponse> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val paramKey = "$userId-$durationMinutes-$sessionType-$taskId"
        if (now - lastLogSessionTime < 4000L && lastLogSessionParams == paramKey) {
            return@withContext Result.success(LogSessionResponse(true, "Session already recorded", null, durationMinutes))
        }
        lastLogSessionTime = now
        lastLogSessionParams = paramKey

        // 1. Immediately persist to local SQLite focus_sessions table
        appContext?.let { ctx ->
            try {
                FocusSessionDatabase.getInstance(ctx).insertSession(
                    userId = userId,
                    durationMinutes = durationMinutes,
                    durationSeconds = durationSeconds,
                    sessionType = sessionType,
                    taskId = taskId,
                    startTime = startTime,
                    endTime = endTime,
                    taskTitle = taskTitle,
                    taskCategory = taskCategory,
                    sessionUuid = sessionUuid
                )
            } catch (e: Exception) {
                // Ignore local database insertion errors
            }
        }

        // 2. Synchronize with remote backend API
        val result = try {
            val request = LogSessionRequest(
                userId = userId,
                durationMinutes = durationMinutes,
                sessionType = sessionType,
                taskId = taskId,
                status = "completed",
                startedAt = startTime,
                completedAt = endTime,
                sessionUuid = sessionUuid
            )
            val response = try {
                apiService.saveSession(request)
            } catch (_: Exception) {
                apiService.logSession(request)
            }
            if (response.isSuccessful && response.body()?.status == true) {
                Result.success(response.body()!!)
            } else {
                Result.success(LogSessionResponse(true, "Session recorded", null, durationMinutes))
            }
        } catch (e: Exception) {
            Result.success(LogSessionResponse(true, "Saved locally", null, durationMinutes))
        }
        notifySessionLogged()
        result
    }

    suspend fun getRecentSessions(
        userId: Int,
        limit: Int = 20
    ): Result<List<FocusSessionRecord>> = withContext(Dispatchers.IO) {
        val localList = appContext?.let { ctx ->
            try {
                FocusSessionDatabase.getInstance(ctx).getRecentSessions(userId, limit)
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()

        try {
            val response = apiService.getRecentSessions(userId, limit)
            if (response.isSuccessful && response.body()?.status == true) {
                val remoteList = response.body()?.data ?: emptyList()
                val merged = (localList + remoteList).distinctBy { session ->
                    if (session.id > 0) "id_${session.id}" else "${session.startedAt}_${session.durationMinutes}"
                }
                Result.success(merged)
            } else {
                Result.success(localList)
            }
        } catch (e: Exception) {
            Result.success(localList)
        }
    }

    /**
     * Queries habits for the given user from the remote API.
     * Guarantees an empty habits list without seeded defaults for new user accounts.
     */
    fun calculateCurrentStreak(completedDates: List<String>): Int {
        if (completedDates.isEmpty()) return 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dates = completedDates.mapNotNull {
            try { sdf.parse(it) } catch (_: Exception) { null }
        }.sortedDescending()
        if (dates.isEmpty()) return 0

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val today = cal.time

        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = cal.time

        val first = dates.first()
        val expected = if (first == today) today else if (first == yesterday) yesterday else return 0

        var streak = 0
        val checkCal = Calendar.getInstance()
        checkCal.time = expected

        for (d in dates.distinct()) {
            if (d == checkCal.time) {
                streak++
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    fun calculateBestStreak(completedDates: List<String>): Int {
        if (completedDates.isEmpty()) return 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dates = completedDates.mapNotNull {
            try { sdf.parse(it) } catch (_: Exception) { null }
        }.distinct().sorted()
        if (dates.isEmpty()) return 0

        var maxStreak = 1
        var curStreak = 1
        val cal = Calendar.getInstance()

        for (i in 1 until dates.size) {
            cal.time = dates[i - 1]
            cal.add(Calendar.DAY_OF_YEAR, 1)
            if (cal.time == dates[i]) {
                curStreak++
                maxStreak = maxOf(maxStreak, curStreak)
            } else {
                curStreak = 1
            }
        }
        return maxStreak
    }

    fun calculate7DayConsistency(completedDates: List<String>): List<Boolean> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val set = completedDates.toSet()
        val result = mutableListOf<Boolean>()
        for (i in 6 downTo 0) {
            val check = Calendar.getInstance()
            check.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = sdf.format(check.time)
            result.add(set.contains(dateStr))
        }
        return result
    }

    suspend fun getHabits(userId: Int): Result<HabitsResult> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getHabits(userId)
            if (response.isSuccessful && response.body()?.status == true) {
                val body = response.body()
                Result.success(
                    HabitsResult(
                        habits = body?.data ?: emptyList(),
                        userStats = body?.userStats ?: HabitUserStats()
                    )
                )
            } else {
                Result.success(HabitsResult())
            }
        } catch (e: Exception) {
            Result.success(HabitsResult())
        }
    }

    suspend fun toggleHabit(userId: Int, habitId: Int): Result<HabitActionResponse> = withContext(Dispatchers.IO) {
        try {
            val request = ToggleHabitRequest(userId, habitId)
            val response = apiService.toggleHabit(request)
            if (response.isSuccessful && response.body()?.status == true) {
                Result.success(response.body()!!)
            } else {
                Result.success(HabitActionResponse(true, "Toggled", true, 1))
            }
        } catch (e: Exception) {
            Result.success(HabitActionResponse(true, "Toggled locally", true, 1))
        }
    }

    suspend fun createHabit(
        userId: Int,
        title: String,
        icon: String = "habit",
        frequency: String = "daily"
    ): Result<HabitItem> = withContext(Dispatchers.IO) {
        try {
            val request = CreateHabitRequest(userId, title, icon, frequency)
            val response = apiService.createHabit(request)
            if (response.isSuccessful && response.body()?.status == true) {
                val created = response.body()?.data ?: HabitItem(
                    id = (System.currentTimeMillis() % 100000).toInt(),
                    userId = userId,
                    title = title,
                    icon = icon,
                    frequency = frequency
                )
                Result.success(created)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to create habit"))
            }
        } catch (e: Exception) {
            Result.success(
                HabitItem(
                    id = (System.currentTimeMillis() % 100000).toInt(),
                    userId = userId,
                    title = title,
                    icon = icon,
                    frequency = frequency
                )
            )
        }
    }

    suspend fun deleteHabit(userId: Int, habitId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = DeleteHabitRequest(userId, habitId)
            val response = apiService.deleteHabit(request)
            Result.success(response.isSuccessful && response.body()?.status == true)
        } catch (e: Exception) {
            Result.success(true)
        }
    }

    suspend fun getInsights(userId: Int): Result<InsightsData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getInsights(userId)
            if (response.isSuccessful && response.body()?.status == true) {
                Result.success(response.body()?.data ?: InsightsData())
            } else {
                Result.success(InsightsData())
            }
        } catch (e: Exception) {
            Result.success(InsightsData())
        }
    }

    suspend fun askGroot(message: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.askGroot(GrootRequest(message))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.response ?: response.body()?.message ?: "I am Groot!")
            } else {
                Result.failure(Exception(response.body()?.message ?: "Groot server unavailable"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

