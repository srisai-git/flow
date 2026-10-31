package com.simats.focusflow.ui.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.focusflow.data.models.DashboardData
import com.simats.focusflow.data.models.HabitItem
import com.simats.focusflow.data.models.TaskItem
import com.simats.focusflow.data.repository.AiRepository
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
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

    init {
        // Automatically reload dashboard whenever a focus session completes or is logged
        viewModelScope.launch(Dispatchers.IO) {
            ProductivityRepository.sessionLoggedEventFlow.collect {
                loadDashboard()
            }
        }
    }

    fun initSessionManager(manager: SessionManager) {
        this.sessionManager = manager
        viewModelScope.launch(Dispatchers.IO) {
            manager.userAvatarFlow.collect { avatar ->
                if (!avatar.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(userAvatar = avatar) }
                    }
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            manager.userSessionFlow.collect { session ->
                if (session.userId != null && session.userId > 0) {
                    currentUserId = session.userId
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

    fun loadDashboard() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            var sessionAvatar = ""
            sessionManager?.let { sm ->
                val session = sm.userSessionFlow.first()
                if (session.userId != null && session.userId > 0) {
                    currentUserId = session.userId
                }
                sessionAvatar = session.userAvatarUri ?: session.userAvatar ?: ""
            }

            val result = repository.getDashboard(currentUserId)
            withContext(Dispatchers.Main) {
                result.fold(
                    onSuccess = { data ->
                        val finalAvatar = if (sessionAvatar.isNotBlank()) sessionAvatar else data.userAvatar
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                data = data,
                                userAvatar = finalAvatar
                            )
                        }
                    },
                    onFailure = { err ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                userAvatar = sessionAvatar,
                                errorMessage = err.localizedMessage
                            )
                        }
                    }
                )
            }

            // Fetch compact 1-liner productivity snippet from Groot AI independently (non-blocking)
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
                    onFailure = { /* keep default snippet */ }
                )
            }
        }
    }

    fun toggleTask(taskId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val tasks = _uiState.value.data.priorityTasks
            val task = tasks.find { it.id == taskId } ?: return@launch
            val newCompleted = !task.isCompleted

            // Optimistic update
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

            // Optimistic update
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
