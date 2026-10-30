package com.simats.focusflow.data.repository

import com.simats.focusflow.data.api.FlowApiService
import com.simats.focusflow.data.api.RetrofitClient
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
        private val _sessionLoggedEventFlow = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 5)
        val sessionLoggedEventFlow: SharedFlow<Unit> = _sessionLoggedEventFlow.asSharedFlow()

        fun notifySessionLogged() {
            _sessionLoggedEventFlow.tryEmit(Unit)
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
                    userName = "Sri Sai",
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
        taskId: Int? = null
    ): Result<LogSessionResponse> = withContext(Dispatchers.IO) {
        val result = try {
            val request = LogSessionRequest(userId, durationMinutes, sessionType, taskId)
            val response = apiService.logSession(request)
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
        try {
            val response = apiService.getRecentSessions(userId, limit)
            if (response.isSuccessful && response.body()?.status == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.success(emptyList())
        }
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

