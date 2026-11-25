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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody



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

        @Volatile
        private var cachedDashboardData: DashboardData? = null
        @Volatile
        private var cachedDashboardTime = 0L
        @Volatile
        private var cachedDashboardUserId = -1

        fun invalidateDashboardCache() {
            cachedDashboardTime = 0L
            cachedDashboardData = null
        }

        fun clearUserCache() {
            lastLogSessionTime = 0L
            lastLogSessionParams = ""
            invalidateDashboardCache()
        }

        fun notifySessionLogged() {
            invalidateDashboardCache()
            _sessionLoggedEventFlow.tryEmit(Unit)
            FocusSessionDatabase.notifyTableChanged()
        }

        private val _habitTableChangedFlow = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 10)
        val habitTableChangedFlow: SharedFlow<Unit> = _habitTableChangedFlow.asSharedFlow()

        fun notifyHabitChanged() {
            invalidateDashboardCache()
            _habitTableChangedFlow.tryEmit(Unit)
        }

        suspend fun syncPendingItems() {
            val repository = ProductivityRepository()
            repository.performSyncPendingItems()
        }
    }

    suspend fun performSyncPendingItems() = withContext(Dispatchers.IO) {
        val ctx = appContext ?: return@withContext
        val db = FocusSessionDatabase.getInstance(ctx)
        var hasSyncedAny = false

        try {
            val unsyncedTasks = db.getUnsyncedTasks()
            for (task in unsyncedTasks) {
                try {
                    val request = CreateTaskRequest(
                        userId = task.userId,
                        title = task.title,
                        description = task.description,
                        priority = task.priority,
                        dueDate = task.dueDate,
                        scheduledTime = task.scheduledTime,
                        category = task.category,
                        deadlineTimestamp = task.deadlineTimestamp
                    )
                    val response = apiService.createTask(request)
                    if (response.isSuccessful && response.body()?.status == true) {
                        val serverId = response.body()?.data?.id ?: 0
                        db.markTaskSynced(task.clientId, serverId)
                        if (task.deadlineTimestamp != null && task.deadlineTimestamp > System.currentTimeMillis() && !task.isCompleted && serverId > 0) {
                            com.simats.focusflow.service.TaskDeadlineScheduler.scheduleTaskDeadline(ctx, serverId, task.title, task.deadlineTimestamp)
                        }
                        if (task.isCompleted && serverId > 0) {
                            try {
                                apiService.toggleTask(ToggleTaskRequest(task.userId, serverId, "toggle", true))
                            } catch (_: Exception) {}
                        }
                        hasSyncedAny = true
                    }
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}

        try {
            val unsyncedSessions = db.getUnsyncedSessions()
            for (session in unsyncedSessions) {
                try {
                    val request = LogSessionRequest(
                        userId = session.userId,
                        durationMinutes = session.durationMinutes,
                        sessionType = session.sessionType,
                        taskId = session.taskId,
                        tag = session.taskCategory.ifBlank { null },
                        status = session.status,
                        startedAt = session.startedAt,
                        completedAt = session.completedAt,
                        sessionUuid = session.sessionUuid
                    )
                    val response = try {
                        apiService.saveSession(request)
                    } catch (_: Exception) {
                        apiService.logSession(request)
                    }
                    if (response.isSuccessful && response.body()?.status == true) {
                        db.markSessionSynced(session.sessionUuid)
                        hasSyncedAny = true
                    }
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}

        try {
            val unsyncedDistractions = db.getUnsyncedDistractions()
            for (distraction in unsyncedDistractions) {
                try {
                    val req = LogDistractionRequest(
                        userId = distraction.userId,
                        reason = distraction.reason,
                        category = distraction.category,
                        durationSeconds = distraction.durationSeconds
                    )
                    val resp = apiService.logDistraction(req)
                    if (resp.isSuccessful && resp.body()?.status == true) {
                        db.markDistractionSynced(distraction.clientId)
                        hasSyncedAny = true
                    }
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}

        if (hasSyncedAny) {
            invalidateDashboardCache()
            _sessionLoggedEventFlow.tryEmit(Unit)
            FocusSessionDatabase.notifyTableChanged()
            FocusSessionDatabase.notifyTaskTableChanged()
            FocusSessionDatabase.notifyDistractionTableChanged()
        }
    }

    suspend fun getDashboard(userId: Int): Result<DashboardData> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (cachedDashboardUserId == userId && now - cachedDashboardTime < 4000L && cachedDashboardData != null) {
            return@withContext Result.success(cachedDashboardData!!)
        }
        try {
            val response = apiService.getDashboard(userId)
            if (response.isSuccessful && response.body()?.status == true) {
                val data = response.body()?.data ?: DashboardData()
                cachedDashboardData = data
                cachedDashboardTime = now
                cachedDashboardUserId = userId
                Result.success(data)
            } else {
                Result.success(DashboardData())
            }
        } catch (e: Exception) {
            Result.success(
                cachedDashboardData ?: DashboardData(
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
        val localTasks = appContext?.let { ctx ->
            try {
                FocusSessionDatabase.getInstance(ctx).getLocalTasks(userId)
            } catch (_: Exception) {
                emptyList()
            }
        } ?: emptyList()

        try {
            val response = apiService.getTasks(userId)
            if (response.isSuccessful && response.body()?.status == true) {
                val rawRemoteTasks = response.body()?.data ?: emptyList()
                val remoteTasks = rawRemoteTasks.map { t ->
                    val effectiveDate = if (t.dueDate.isNotBlank() && t.dueDate != "0000-00-00") {
                        t.dueDate
                    } else if (t.deadlineTimestamp != null && t.deadlineTimestamp > 0) {
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(t.deadlineTimestamp))
                    } else {
                        t.dueDate
                    }
                    val effectiveTime = if (!t.scheduledTime.isNullOrBlank()) {
                        t.scheduledTime
                    } else if (t.deadlineTimestamp != null && t.deadlineTimestamp > 0) {
                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(t.deadlineTimestamp))
                    } else null
                    t.copy(dueDate = effectiveDate, scheduledTime = effectiveTime)
                }
                val merged = appContext?.let { ctx ->
                    try {
                        FocusSessionDatabase.getInstance(ctx).mergeAndCacheServerTasks(userId, remoteTasks)
                    } catch (_: Exception) {
                        (localTasks.filter { !it.isSynced } + remoteTasks).distinctBy {
                            if (!it.isSynced) {
                                if (it.clientId.isNotBlank()) "client_${it.clientId}" else "local_${it.id}"
                            } else {
                                "server_${it.id}"
                            }
                        }
                    }
                } ?: (localTasks.filter { !it.isSynced } + remoteTasks).distinctBy {
                    if (!it.isSynced) {
                        if (it.clientId.isNotBlank()) "client_${it.clientId}" else "local_${it.id}"
                    } else {
                        "server_${it.id}"
                    }
                }
                appContext?.let { ctx ->
                    for (item in merged) {
                        if (!item.isCompleted && item.deadlineTimestamp != null && item.deadlineTimestamp > System.currentTimeMillis()) {
                            com.simats.focusflow.service.TaskDeadlineScheduler.scheduleTaskDeadline(ctx, item.id, item.title, item.deadlineTimestamp)
                        } else if (item.isCompleted && item.deadlineTimestamp != null) {
                            com.simats.focusflow.service.TaskDeadlineScheduler.cancelTaskDeadline(ctx, item.id)
                        }
                    }
                }
                Result.success(merged)
            } else {
                Result.success(localTasks)
            }
        } catch (e: Exception) {
            Result.success(localTasks)
        }
    }

    suspend fun createTask(
        userId: Int,
        title: String,
        description: String = "",
        priority: String = "medium",
        dueDate: String = "",
        category: String = "General",
        scheduledTime: String? = null,
        deadlineTimestamp: Long? = null
    ): Result<TaskItem> = withContext(Dispatchers.IO) {
        invalidateDashboardCache()
        val clientId = java.util.UUID.randomUUID().toString()
        val effectiveDueDate = if (dueDate.isNotBlank() && dueDate != "0000-00-00") {
            dueDate
        } else if (deadlineTimestamp != null && deadlineTimestamp > 0) {
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(deadlineTimestamp))
        } else {
            "Today"
        }
        val effectiveScheduledTime = scheduledTime ?: if (deadlineTimestamp != null && deadlineTimestamp > 0) {
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(deadlineTimestamp))
        } else null

        val initialTask = TaskItem(
            id = (System.currentTimeMillis() % 100000).toInt(),
            userId = userId,
            title = title,
            description = description,
            priority = priority,
            dueDate = effectiveDueDate,
            scheduledTime = effectiveScheduledTime,
            deadlineTimestamp = deadlineTimestamp,
            category = category,
            isCompleted = false,
            isSynced = false,
            clientId = clientId
        )

        appContext?.let { ctx ->
            try {
                FocusSessionDatabase.getInstance(ctx).insertTaskLocally(initialTask, isSynced = false)
                if (deadlineTimestamp != null && deadlineTimestamp > System.currentTimeMillis()) {
                    com.simats.focusflow.service.TaskDeadlineScheduler.scheduleTaskDeadline(ctx, initialTask.id, initialTask.title, deadlineTimestamp)
                }
            } catch (_: Exception) {}
        }

        try {
            val request = CreateTaskRequest(userId, title, description, priority, effectiveDueDate, effectiveScheduledTime, category, deadlineTimestamp)
            val response = apiService.createTask(request)
            if (response.isSuccessful && response.body()?.status == true) {
                val created = response.body()?.data ?: initialTask
                val finalItem = created.copy(
                    isSynced = true,
                    clientId = clientId,
                    dueDate = effectiveDueDate,
                    scheduledTime = effectiveScheduledTime,
                    deadlineTimestamp = deadlineTimestamp
                )
                appContext?.let { ctx ->
                    try {
                        FocusSessionDatabase.getInstance(ctx).markTaskSynced(clientId, finalItem.id)
                        if (deadlineTimestamp != null && deadlineTimestamp > System.currentTimeMillis()) {
                            com.simats.focusflow.service.TaskDeadlineScheduler.scheduleTaskDeadline(ctx, finalItem.id, finalItem.title, deadlineTimestamp)
                        }
                    } catch (_: Exception) {}
                }
                Result.success(finalItem)
            } else {
                Result.success(initialTask)
            }
        } catch (e: Exception) {
            Result.success(initialTask)
        }
    }

    suspend fun updateTask(
        userId: Int,
        taskId: Int,
        title: String,
        description: String = "",
        priority: String = "medium",
        dueDate: String = "",
        category: String = "General",
        scheduledTime: String? = null,
        deadlineTimestamp: Long? = null,
        clientId: String = ""
    ): Result<TaskItem> = withContext(Dispatchers.IO) {
        invalidateDashboardCache()
        val effectiveDueDate = if (dueDate.isNotBlank() && dueDate != "0000-00-00") {
            dueDate
        } else if (deadlineTimestamp != null && deadlineTimestamp > 0) {
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(deadlineTimestamp))
        } else {
            "Today"
        }
        val effectiveScheduledTime = scheduledTime ?: if (deadlineTimestamp != null && deadlineTimestamp > 0) {
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(deadlineTimestamp))
        } else null

        val updatedTask = TaskItem(
            id = taskId,
            userId = userId,
            title = title,
            description = description,
            priority = priority,
            dueDate = effectiveDueDate,
            scheduledTime = effectiveScheduledTime,
            deadlineTimestamp = deadlineTimestamp,
            category = category,
            isCompleted = false,
            isSynced = false,
            clientId = clientId
        )

        appContext?.let { ctx ->
            try {
                FocusSessionDatabase.getInstance(ctx).updateTaskLocally(updatedTask, isSynced = false)
            } catch (_: Exception) {}
            if (deadlineTimestamp != null && deadlineTimestamp > System.currentTimeMillis()) {
                com.simats.focusflow.service.TaskDeadlineScheduler.scheduleTaskDeadline(ctx, taskId, title, deadlineTimestamp)
            } else {
                com.simats.focusflow.service.TaskDeadlineScheduler.cancelTaskDeadline(ctx, taskId)
            }
        }

        try {
            val request = UpdateTaskRequest(
                action = "update",
                userId = userId,
                taskId = taskId,
                title = title,
                description = description,
                priority = priority,
                dueDate = effectiveDueDate,
                scheduledTime = effectiveScheduledTime,
                category = category,
                deadlineTimestamp = deadlineTimestamp
            )
            val response = apiService.updateTask(request)
            if (response.isSuccessful && response.body()?.status == true) {
                val finalItem = (response.body()?.data ?: updatedTask).copy(
                    isSynced = true,
                    clientId = clientId,
                    dueDate = effectiveDueDate,
                    scheduledTime = effectiveScheduledTime,
                    deadlineTimestamp = deadlineTimestamp
                )
                appContext?.let { ctx ->
                    try {
                        FocusSessionDatabase.getInstance(ctx).updateTaskLocally(finalItem, isSynced = true)
                    } catch (_: Exception) {}
                }
                Result.success(finalItem)
            } else {
                Result.success(updatedTask)
            }
        } catch (e: Exception) {
            Result.success(updatedTask)
        }
    }

    suspend fun toggleTask(userId: Int, taskId: Int, isCompleted: Boolean? = null): Result<Boolean> = withContext(Dispatchers.IO) {
        invalidateDashboardCache()
        val targetCompleted = isCompleted ?: true
        appContext?.let { ctx ->
            try {
                FocusSessionDatabase.getInstance(ctx).updateTaskCompletionLocally(taskId, targetCompleted)
            } catch (_: Exception) {}
            if (targetCompleted) {
                com.simats.focusflow.service.TaskDeadlineScheduler.cancelTaskDeadline(ctx, taskId)
            }
        }
        try {
            val request = ToggleTaskRequest(userId, taskId, "toggle", isCompleted)
            val response = apiService.toggleTask(request)
            if (response.isSuccessful && response.body()?.status == true) {
                val comp = response.body()?.isCompleted ?: targetCompleted
                appContext?.let { ctx ->
                    try {
                        FocusSessionDatabase.getInstance(ctx).updateTaskCompletionLocally(taskId, comp)
                    } catch (_: Exception) {}
                }
                Result.success(comp)
            } else {
                Result.success(targetCompleted)
            }
        } catch (e: Exception) {
            Result.success(targetCompleted)
        }
    }

    suspend fun deleteTask(userId: Int, taskId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        invalidateDashboardCache()
        appContext?.let { ctx ->
            try {
                FocusSessionDatabase.getInstance(ctx).deleteTaskLocally(taskId)
            } catch (_: Exception) {}
            com.simats.focusflow.service.TaskDeadlineScheduler.cancelTaskDeadline(ctx, taskId)
        }
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
        tag: String? = null,
        status: String = "completed",
        sessionUuid: String = java.util.UUID.randomUUID().toString()
    ): Result<LogSessionResponse> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val paramKey = "$userId-$durationMinutes-$sessionType-$taskId"
        if (now - lastLogSessionTime < 4000L && lastLogSessionParams == paramKey) {
            return@withContext Result.success(LogSessionResponse(true, "Session already recorded", null, durationMinutes))
        }
        lastLogSessionTime = now
        lastLogSessionParams = paramKey

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
                    taskCategory = taskCategory ?: tag,
                    sessionUuid = sessionUuid,
                    isSynced = false
                )
            } catch (e: Exception) {
            }
        }

        val result = try {
            val request = LogSessionRequest(
                userId = userId,
                durationMinutes = durationMinutes,
                sessionType = sessionType,
                taskId = taskId,
                tag = tag ?: taskCategory,
                status = status,
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
                appContext?.let { ctx ->
                    try {
                        FocusSessionDatabase.getInstance(ctx).markSessionSynced(sessionUuid)
                    } catch (_: Exception) {}
                }
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
                val remoteUuids = remoteList.mapNotNull { it.sessionUuid.trim().ifEmpty { null } }.toSet()
                val merged = (localList.map { local ->
                    if (local.sessionUuid.isNotBlank() && remoteUuids.contains(local.sessionUuid.trim())) {
                        local.copy(isSynced = true)
                    } else {
                        local
                    }
                } + remoteList).distinctBy { session ->
                    val uuid = session.sessionUuid.trim()
                    if (uuid.isNotEmpty()) {
                        "uuid_$uuid"
                    } else {
                        val timeKey = session.startedAt.take(16)
                        "${session.userId}_${timeKey}_${session.durationMinutes}"
                    }
                }
                Result.success(merged)
            } else {
                Result.success(localList)
            }
        } catch (e: Exception) {
            Result.success(localList)
        }
    }

    

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
            notifyHabitChanged()
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
            notifyHabitChanged()
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
            notifyHabitChanged()
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

    suspend fun logDistraction(
        userId: Int,
        reason: String,
        category: String = "Other",
        durationSeconds: Int = 0
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val clientId = java.util.UUID.randomUUID().toString()
        appContext?.let { ctx ->
            try {
                FocusSessionDatabase.getInstance(ctx).insertDistraction(
                    userId = userId,
                    reason = reason,
                    category = category,
                    durationSeconds = durationSeconds,
                    isSynced = false,
                    clientId = clientId
                )
            } catch (_: Exception) {}
        }
        try {
            val req = LogDistractionRequest(
                userId = userId,
                reason = reason,
                category = category,
                durationSeconds = durationSeconds
            )
            val resp = apiService.logDistraction(req)
            if (resp.isSuccessful && resp.body()?.status == true) {
                appContext?.let { ctx ->
                    try {
                        FocusSessionDatabase.getInstance(ctx).markDistractionSynced(clientId)
                    } catch (_: Exception) {}
                }
                Result.success(true)
            } else {
                Result.success(true)
            }
        } catch (_: Exception) {
            Result.success(true)
        }
    }

    suspend fun getDistractions(userId: Int): Result<List<DistractionLogItem>> = withContext(Dispatchers.IO) {
        val local = appContext?.let { ctx ->
            try {
                FocusSessionDatabase.getInstance(ctx).getDistractions(userId)
            } catch (_: Exception) {
                emptyList()
            }
        } ?: emptyList()

        try {
            val resp = apiService.getDistractions(userId)
            if (resp.isSuccessful && resp.body()?.status == true) {
                val remote = resp.body()?.data ?: emptyList()
                val merged = (local + remote).distinctBy { item ->
                    if (item.clientId.isNotBlank()) "client_${item.clientId}"
                    else if (item.id > 0) "id_${item.id}"
                    else "${item.createdAt}_${item.reason}"
                }
                Result.success(merged)
            } else {
                Result.success(local)
            }
        } catch (_: Exception) {
            Result.success(local)
        }
    }

    suspend fun askGroot(message: String): Result<String> = withContext(Dispatchers.IO) {
        val brainResult = com.simats.focusflow.utils.LocalGrootBrain.process(
            query = message,
            context = appContext,
            userId = cachedDashboardUserId.takeIf { it > 0 } ?: 1
        )
        Result.success(brainResult.reply)
    }
}

