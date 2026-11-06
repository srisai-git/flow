package com.simats.focusflow.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.focusflow.data.local.FocusSessionDatabase
import com.simats.focusflow.data.models.FocusSessionRecord
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.service.FocusTimerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Immutable
enum class TimerMode(val title: String) {
    FOCUS("Focus"),
    SHORT_BREAK("Short Break"),
    LONG_BREAK("Long Break")
}

@Immutable
data class TimerUiState(
    val currentMode: TimerMode = TimerMode.FOCUS,
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val completedSessions: Int = 0,
    val recentSessions: List<FocusSessionRecord> = emptyList(),
    val toastMessage: String? = null,
    val isSettingsOpen: Boolean = false,
    val isHistoryOpen: Boolean = false
) {
    val progress: Float
        get() = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f

    val formattedTime: String
        get() {
            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            return String.format("%02d:%02d", mins, secs)
        }

    fun getDurationMinutes(mode: TimerMode = currentMode): Int {
        return when (mode) {
            TimerMode.FOCUS -> focusMinutes
            TimerMode.SHORT_BREAK -> shortBreakMinutes
            TimerMode.LONG_BREAK -> longBreakMinutes
        }
    }
}

class TimerViewModel(
    private val repository: ProductivityRepository = ProductivityRepository(),
    private var sessionManager: SessionManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var appContext: Context? = null
    private var currentUserId: Int = 1
    @Volatile private var isSavingSession = false

    init {
        viewModelScope.launch {
            FocusTimerService.timerStateFlow.collect { serviceState ->
                _uiState.update { current ->
                    current.copy(
                        currentMode = serviceState.currentMode,
                        focusMinutes = serviceState.focusMinutes,
                        shortBreakMinutes = serviceState.shortBreakMinutes,
                        longBreakMinutes = serviceState.longBreakMinutes,
                        totalSeconds = serviceState.totalSeconds,
                        remainingSeconds = serviceState.remainingSeconds,
                        isRunning = serviceState.isRunning,
                        completedSessions = serviceState.completedSessions,
                        toastMessage = serviceState.toastMessage
                    )
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            ProductivityRepository.sessionLoggedEventFlow.collect {
                loadSessionHistory()
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            FocusSessionDatabase.sessionTableChangedFlow.collect {
                loadSessionHistory()
            }
        }
    }

    fun initSessionManager(context: Context, manager: SessionManager) {
        this.appContext = context.applicationContext
        this.sessionManager = manager
        ProductivityRepository.init(context)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val session = manager.userSessionFlow.first()
                if (session.userId != null && session.userId > 0) {
                    currentUserId = session.userId
                }
            } catch (e: Exception) {
                android.util.Log.e("TimerViewModel", "Failed to get user session: ${e.message}")
            }
            loadSessionHistory()
        }
    }

    // Compatibility overload
    fun initSessionManager(manager: SessionManager) {
        this.sessionManager = manager
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val session = manager.userSessionFlow.first()
                if (session.userId != null && session.userId > 0) {
                    currentUserId = session.userId
                }
            } catch (e: Exception) {
                android.util.Log.e("TimerViewModel", "Failed to get user session: ${e.message}")
            }
            loadSessionHistory()
        }
    }

    fun loadSessionHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val rawLocalSessions = appContext?.let { ctx ->
                try {
                    FocusSessionDatabase.getInstance(ctx).getRecentSessions(currentUserId, 50)
                } catch (e: Exception) {
                    android.util.Log.e("TimerViewModel", "Failed to get local sessions: ${e.message}")
                    emptyList()
                }
            } ?: emptyList()

            val distinctLocal = rawLocalSessions
                .filter { it.userId == currentUserId }
                .distinctBy { session ->
                    if (session.id > 0) "id_${session.id}" else "${session.startedAt}_${session.durationMinutes}"
                }
                .sortedByDescending { it.id }

            if (distinctLocal.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    _uiState.update { current ->
                        current.copy(
                            recentSessions = distinctLocal,
                            completedSessions = maxOf(current.completedSessions, distinctLocal.size)
                        )
                    }
                }
            }

            val result = try {
                repository.getRecentSessions(currentUserId)
            } catch (e: Exception) {
                android.util.Log.e("TimerViewModel", "Failed to get recent sessions: ${e.message}")
                Result.failure(e)
            }
            withContext(Dispatchers.Main) {
                result.fold(
                    onSuccess = { list ->
                        val distinct = (distinctLocal + list)
                            .filter { it.userId == currentUserId }
                            .distinctBy { session ->
                                if (session.id > 0) "id_${session.id}" else "${session.startedAt}_${session.durationMinutes}"
                            }
                            .sortedByDescending { it.id }

                        _uiState.update { current ->
                            current.copy(
                                recentSessions = distinct,
                                completedSessions = maxOf(current.completedSessions, distinct.size)
                            )
                        }
                    },
                    onFailure = { /* retain local */ }
                )
            }
        }
    }

    fun resetSessionState() {
        _uiState.value = TimerUiState(
            currentMode = TimerMode.FOCUS,
            focusMinutes = 25,
            shortBreakMinutes = 5,
            longBreakMinutes = 15,
            totalSeconds = 25 * 60,
            remainingSeconds = 25 * 60,
            isRunning = false,
            completedSessions = 0,
            recentSessions = emptyList(),
            toastMessage = null,
            isSettingsOpen = false,
            isHistoryOpen = false
        )
        currentUserId = -1
    }

    fun setSettingsOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = isOpen) }
    }

    fun setHistoryOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isHistoryOpen = isOpen) }
        if (isOpen) {
            loadSessionHistory()
        }
    }

    fun updateCustomDurations(focus: Int, shortBreak: Int, longBreak: Int) {
        val f = focus.coerceIn(5, 90)
        val sb = shortBreak.coerceIn(1, 15)
        val lb = longBreak.coerceIn(10, 30)

        _uiState.update { it.copy(isSettingsOpen = false) }
        appContext?.let { ctx ->
            FocusTimerService.setDurations(ctx, f, sb, lb)
        }
    }

    fun selectMode(mode: TimerMode) {
        appContext?.let { ctx ->
            FocusTimerService.selectMode(ctx, mode)
        }
    }

    fun toggleStartPause() {
        appContext?.let { ctx ->
            if (_uiState.value.isRunning) {
                FocusTimerService.pauseTimer(ctx)
            } else {
                FocusTimerService.startTimer(ctx, currentUserId)
            }
        }
    }

    fun startTimer() {
        appContext?.let { ctx ->
            FocusTimerService.startTimer(ctx, currentUserId)
        }
    }

    fun pauseTimer() {
        appContext?.let { ctx ->
            FocusTimerService.pauseTimer(ctx)
        }
    }

    fun resetTimer() {
        appContext?.let { ctx ->
            FocusTimerService.resetTimer(ctx)
        }
    }

    fun skipSession() {
        appContext?.let { ctx ->
            FocusTimerService.skipSession(ctx)
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    /**
     * Manually or programmatically records a completed focus session and propagates
     * updates to local database, remote API (save_session.php), timer history, and profile stats.
     */
    fun recordCompletedSession(
        durationMinutes: Int,
        sessionType: String = "pomodoro",
        taskId: Int? = null,
        taskTitle: String? = null,
        taskCategory: String? = null
    ) {
        if (isSavingSession) return
        isSavingSession = true

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val now = Date()
        val endTime = sdf.format(now)
        val cal = Calendar.getInstance()
        cal.time = now
        cal.add(Calendar.MINUTE, -durationMinutes)
        val startTime = sdf.format(cal.time)
        val sessionUuid = java.util.UUID.randomUUID().toString()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.logSession(
                    userId = currentUserId,
                    durationMinutes = durationMinutes,
                    sessionType = sessionType,
                    taskId = taskId,
                    durationSeconds = durationMinutes * 60,
                    startTime = startTime,
                    endTime = endTime,
                    taskTitle = taskTitle,
                    taskCategory = taskCategory,
                    sessionUuid = sessionUuid
                )
                loadSessionHistory()
            } finally {
                isSavingSession = false
            }
        }
    }
}
