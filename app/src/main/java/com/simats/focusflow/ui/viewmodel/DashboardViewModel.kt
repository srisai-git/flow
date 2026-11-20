package com.simats.focusflow.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.focusflow.data.models.DashboardData
import com.simats.focusflow.data.models.HabitItem
import com.simats.focusflow.data.models.TaskItem
import com.simats.focusflow.data.repository.AiRepository
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.data.TokenSyncHelper
import com.simats.focusflow.utils.ProfileImageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Immutable
data class DashboardUiState(
    val isLoading: Boolean = false,
    val data: DashboardData = DashboardData(),
    val userAvatar: String = "",
    val aiSnippet: String = "Compounding consistency leads to effortless daily flow.",
    val isStreakModalOpen: Boolean = false,
    val errorMessage: String? = null
)

class DashboardViewModel(
    private val repository: ProductivityRepository = ProductivityRepository(),
    private val aiRepository: AiRepository = AiRepository(),
    private var sessionManager: SessionManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var currentUserId: Int = 1
    private var avatarPathJob: Job? = null
    private var avatarFlowJob: Job? = null
    private var sessionFlowJob: Job? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            ProductivityRepository.sessionLoggedEventFlow.collect {
                loadDashboard(showLoadingIndicator = false)
            }
        }
    }

    private var appContext: Context? = null

    fun initSessionManager(context: Context, manager: SessionManager) {
        this.appContext = context.applicationContext
        val existingAvatar = ProfileImageManager.getAvatarPathIfExists(context, currentUserId)
        if (!existingAvatar.isNullOrBlank()) {
            _uiState.update { it.copy(userAvatar = existingAvatar) }
        }
        initSessionManager(manager)
    }

    fun initSessionManager(manager: SessionManager) {
        this.sessionManager = manager
        avatarPathJob?.cancel()
        avatarFlowJob?.cancel()
        sessionFlowJob?.cancel()
        avatarPathJob = viewModelScope.launch(Dispatchers.IO) {
            manager.userAvatarPath.collect { path ->
                if (!path.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(userAvatar = path) }
                    }
                }
            }
        }
        avatarFlowJob = viewModelScope.launch(Dispatchers.IO) {
            manager.userAvatarFlow.collect { avatar ->
                if (!avatar.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(userAvatar = avatar) }
                    }
                }
            }
        }
        sessionFlowJob = viewModelScope.launch(Dispatchers.IO) {
            manager.userSessionFlow.collect { session ->
                if (session.userId != null && session.userId > 0) {
                    currentUserId = session.userId
                    appContext?.let { ctx ->
                        TokenSyncHelper.syncForCurrentUser(ctx)
                    }
                }
                val av = session.userAvatarUri ?: session.userAvatar ?: ""
                val sName = session.userName
                withContext(Dispatchers.Main) {
                    _uiState.update { current ->
                        current.copy(
                            userAvatar = if (av.isNotBlank()) av else current.userAvatar,
                            data = if (!sName.isNullOrBlank()) current.data.copy(userName = sName) else current.data
                        )
                    }
                }
            }
        }
        loadDashboard()
    }

    fun setStreakModalOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isStreakModalOpen = isOpen) }
    }

    fun loadDashboard(showLoadingIndicator: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            if (showLoadingIndicator) {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                }
            }

            var sessionAvatar = ""
            var sessionName = ""
            var localGoalMinutes = 0
            try {
                sessionManager?.let { sm ->
                    val session = sm.userSessionFlow.first()
                    if (session.userId != null && session.userId > 0) {
                        currentUserId = session.userId
                    }
                    sessionAvatar = session.userAvatarUri ?: session.userAvatar ?: ""
                    sessionName = session.userName ?: ""
                    localGoalMinutes = sm.dailyFocusGoalMinutesFlow.first()
                }
            } catch (e: Exception) {
                android.util.Log.e("DashboardViewModel", "Session read error: ${e.message}")
            }

            val result = try {
                repository.getDashboard(currentUserId)
            } catch (e: Exception) {
                android.util.Log.e("DashboardViewModel", "Repository error: ${e.message}")
                Result.failure(e)
            }

            withContext(Dispatchers.Main) {
                result.fold(
                    onSuccess = { data ->
                        val finalAvatar = if (sessionAvatar.isNotBlank()) sessionAvatar else data.userAvatar
                        val resolvedGoal = if (localGoalMinutes > 0) localGoalMinutes else data.focusGoalMinutes
                        val finalData = if (data.userName.isNotBlank()) {
                            data.copy(focusGoalMinutes = resolvedGoal)
                        } else if (sessionName.isNotBlank()) {
                            data.copy(userName = sessionName, focusGoalMinutes = resolvedGoal)
                        } else {
                            data.copy(userName = "User", focusGoalMinutes = resolvedGoal)
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                data = finalData,
                                userAvatar = finalAvatar
                            )
                        }
                    },
                    onFailure = { err ->
                        val fallbackName = if (sessionName.isNotBlank()) sessionName else "User"
                        _uiState.update { current ->
                            val resolvedGoal = if (localGoalMinutes > 0) localGoalMinutes else current.data.focusGoalMinutes
                            current.copy(
                                isLoading = false,
                                data = current.data.copy(userName = fallbackName, focusGoalMinutes = resolvedGoal),
                                userAvatar = sessionAvatar,
                                errorMessage = err.localizedMessage
                            )
                        }
                    }
                )
            }

            viewModelScope.launch(Dispatchers.IO) {
                val aiRes = aiRepository.getAiInsights(currentUserId, queryType = "daily_summary")
                aiRes.fold(
                    onSuccess = { res ->
                        val insight = res.insight?.lines()?.firstOrNull()?.trim()
                        if (!insight.isNullOrBlank()) {
                            withContext(Dispatchers.Main) {
                                _uiState.update { it.copy(aiSnippet = insight) }
                            }
                        }
                    },
                    onFailure = {  }
                )
            }
        }
    }

    fun toggleTask(taskId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val tasks = _uiState.value.data.priorityTasks
            val task = tasks.find { it.id == taskId } ?: return@launch
            val newCompleted = !task.isCompleted

            val updatedTasks = tasks.map {
                if (it.id == taskId) it.copy(isCompleted = newCompleted) else it
            }
            val currentData = _uiState.value.data
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        data = currentData.copy(
                            priorityTasks = updatedTasks,
                            completedTaskCount = if (newCompleted) currentData.completedTaskCount + 1 else (currentData.completedTaskCount - 1).coerceAtLeast(0),
                            pendingTaskCount = if (newCompleted) (currentData.pendingTaskCount - 1).coerceAtLeast(0) else currentData.pendingTaskCount + 1
                        )
                    )
                }
            }

            repository.toggleTask(currentUserId, taskId, newCompleted)
        }
    }

    fun toggleHabit(habitId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val habits = _uiState.value.data.habits
            val habit = habits.find { it.id == habitId } ?: return@launch
            val newCompleted = !habit.isCompletedToday

            val updatedHabits = habits.map {
                if (it.id == habitId) it.copy(isCompletedToday = newCompleted) else it
            }
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(data = it.data.copy(habits = updatedHabits))
                }
            }

            repository.toggleHabit(currentUserId, habitId)
        }
    }
}
