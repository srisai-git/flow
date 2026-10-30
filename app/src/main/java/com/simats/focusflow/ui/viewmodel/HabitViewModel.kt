package com.simats.focusflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.focusflow.data.models.HabitItem
import com.simats.focusflow.data.models.HabitUserStats
import com.simats.focusflow.data.repository.AiRepository
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HabitTab {
    ROUTINE,
    INSIGHTS
}

enum class HabitInsightTimeframe(val label: String) {
    DAY("Day"),
    WEEK("Week"),
    MONTH("Month")
}

data class HabitUiState(
    val isLoading: Boolean = false,
    val habits: List<HabitItem> = emptyList(),
    val userStats: HabitUserStats = HabitUserStats(),
    val aiInsight: String? = null,
    val isAiLoading: Boolean = false,
    val isAddHabitDialogOpen: Boolean = false,
    val selectedTab: HabitTab = HabitTab.ROUTINE,
    val selectedTimeframe: HabitInsightTimeframe = HabitInsightTimeframe.WEEK,
    val selectedHabitForStreak: HabitItem? = null,
    val errorMessage: String? = null
) {
    val completedCount: Int
        get() = habits.count { it.isCompletedToday }

    val totalCount: Int
        get() = habits.size

    val completionRate: Float
        get() = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
}

class HabitViewModel(
    private val repository: ProductivityRepository = ProductivityRepository(),
    private val aiRepository: AiRepository = AiRepository(),
    private var sessionManager: SessionManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitUiState(isLoading = true))
    val uiState: StateFlow<HabitUiState> = _uiState.asStateFlow()

    private var currentUserId: Int = 1

    fun initSessionManager(manager: SessionManager) {
        this.sessionManager = manager
        loadHabits()
    }

    fun loadHabits() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            sessionManager?.let { sm ->
                val session = sm.userSessionFlow.first()
                if (session.userId != null && session.userId > 0) {
                    currentUserId = session.userId
                }
            }

            val result = repository.getHabits(currentUserId)
            result.fold(
                onSuccess = { res ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            habits = res.habits, 
                            userStats = res.userStats
                        ) 
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = err.localizedMessage) }
                }
            )
        }
    }

    fun loadAiInsight(forceRefresh: Boolean = false) {
        if (!forceRefresh && _uiState.value.aiInsight != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true) }
            val result = aiRepository.getHabitInsights(currentUserId)
            result.fold(
                onSuccess = { insight ->
                    _uiState.update { it.copy(isAiLoading = false, aiInsight = insight) }
                },
                onFailure = {
                    _uiState.update { it.copy(isAiLoading = false) }
                }
            )
        }
    }

    fun selectTab(tab: HabitTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab == HabitTab.INSIGHTS) {
            loadAiInsight()
        }
    }

    fun selectTimeframe(timeframe: HabitInsightTimeframe) {
        _uiState.update { it.copy(selectedTimeframe = timeframe) }
    }

    fun selectHabitForStreak(habit: HabitItem?) {
        _uiState.update { it.copy(selectedHabitForStreak = habit) }
    }

    fun setAddHabitDialogOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isAddHabitDialogOpen = isOpen) }
    }

    fun toggleHabit(habitId: Int) {
        viewModelScope.launch {
            val currentList = _uiState.value.habits.toMutableList()
            val index = currentList.indexOfFirst { it.id == habitId }
            if (index != -1) {
                val target = currentList[index]
                val updatedStatus = !target.isCompletedToday
                val updatedStreak = if (updatedStatus) target.streakCount + 1 else maxOf(0, target.streakCount - 1)

                currentList[index] = target.copy(
                    isCompletedToday = updatedStatus,
                    streakCount = updatedStreak
                )
                _uiState.update { it.copy(habits = currentList) }

                repository.toggleHabit(currentUserId, habitId)

                // Refresh backend stats silently to update real charts & streaks
                val refreshed = repository.getHabits(currentUserId)
                refreshed.onSuccess { res ->
                    _uiState.update { it.copy(habits = res.habits, userStats = res.userStats) }
                }
            }
        }
    }

    fun createHabit(title: String, icon: String = "bolt", frequency: String = "daily") {
        if (title.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isAddHabitDialogOpen = false) }

            val result = repository.createHabit(
                userId = currentUserId,
                title = title.trim(),
                icon = icon,
                frequency = frequency
            )

            result.fold(
                onSuccess = { createdHabit ->
                    val updated = _uiState.value.habits.toMutableList().apply { add(createdHabit) }
                    _uiState.update { it.copy(habits = updated) }
                    // Refresh stats
                    val refreshed = repository.getHabits(currentUserId)
                    refreshed.onSuccess { res ->
                        _uiState.update { it.copy(habits = res.habits, userStats = res.userStats) }
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(errorMessage = err.localizedMessage) }
                }
            )
        }
    }

    fun deleteHabit(habitId: Int) {
        viewModelScope.launch {
            val updated = _uiState.value.habits.filter { it.id != habitId }
            _uiState.update { 
                it.copy(
                    habits = updated,
                    selectedHabitForStreak = if (it.selectedHabitForStreak?.id == habitId) null else it.selectedHabitForStreak
                ) 
            }
            repository.deleteHabit(currentUserId, habitId)
            val refreshed = repository.getHabits(currentUserId)
            refreshed.onSuccess { res ->
                _uiState.update { it.copy(habits = res.habits, userStats = res.userStats) }
            }
        }
    }
}
