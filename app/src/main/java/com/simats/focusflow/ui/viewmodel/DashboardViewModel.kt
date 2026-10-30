package com.simats.focusflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.focusflow.data.models.DashboardData
import com.simats.focusflow.data.models.HabitItem
import com.simats.focusflow.data.models.TaskItem
import com.simats.focusflow.data.repository.AiRepository
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    fun initSessionManager(manager: SessionManager) {
        this.sessionManager = manager
        loadDashboard()
    }

    fun setStreakModalOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isStreakModalOpen = isOpen) }
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            var sessionAvatar = ""
            sessionManager?.let { sm ->
                val session = sm.userSessionFlow.first()
                if (session.userId != null && session.userId > 0) {
                    currentUserId = session.userId
                }
                sessionAvatar = session.userAvatar ?: ""
            }

            val result = repository.getDashboard(currentUserId)
            result.fold(
                onSuccess = { data ->
                    val finalAvatar = if (data.userAvatar.isNotBlank()) data.userAvatar else sessionAvatar
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

            // Fetch compact 1-liner productivity snippet from Groot AI
            launch {
                val aiRes = aiRepository.getAiInsights(currentUserId, queryType = "daily_summary")
                aiRes.fold(
                    onSuccess = { res ->
                        val insight = res.insight?.lines()?.firstOrNull()?.trim()
                        if (!insight.isNullOrBlank()) {
                            _uiState.update { it.copy(aiSnippet = insight) }
                        }
                    },
                    onFailure = { /* keep default snippet */ }
                )
            }
        }
    }

    fun toggleTask(taskId: Int) {
        viewModelScope.launch {
            val currentTasks = _uiState.value.data.priorityTasks.toMutableList()
            val index = currentTasks.indexOfFirst { it.id == taskId }
            if (index != -1) {
                val updatedStatus = !currentTasks[index].isCompleted
                currentTasks[index] = currentTasks[index].copy(isCompleted = updatedStatus)
                _uiState.update { it.copy(data = it.data.copy(priorityTasks = currentTasks)) }

                repository.toggleTask(currentUserId, taskId, updatedStatus)
            }
        }
    }

    fun toggleHabit(habitId: Int) {
        viewModelScope.launch {
            val currentHabits = _uiState.value.data.habits.toMutableList()
            val index = currentHabits.indexOfFirst { it.id == habitId }
            if (index != -1) {
                val targetHabit = currentHabits[index]
                val updatedStatus = !targetHabit.isCompletedToday
                val updatedStreak = if (updatedStatus) targetHabit.streakCount + 1 else maxOf(0, targetHabit.streakCount - 1)

                currentHabits[index] = targetHabit.copy(
                    isCompletedToday = updatedStatus,
                    streakCount = updatedStreak
                )
                _uiState.update { it.copy(data = it.data.copy(habits = currentHabits)) }

                repository.toggleHabit(currentUserId, habitId)
            }
        }
    }
}
