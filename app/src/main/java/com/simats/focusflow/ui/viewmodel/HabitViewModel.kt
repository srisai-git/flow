package com.simats.focusflow.ui.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.focusflow.data.models.HabitItem
import com.simats.focusflow.data.models.HabitUserStats
import com.simats.focusflow.data.repository.AiRepository
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.repository.StreakRepository
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
enum class HabitTab {
    ROUTINE,
    INSIGHTS
}

@Immutable
enum class HabitInsightTimeframe(val label: String) {
    DAY("Day"),
    WEEK("Week"),
    MONTH("Month")
}

@Immutable
data class HabitReportData(
    val consistencyScore: Int = 0,
    val strongestHabit: String = "",
    val strongestHabitStreak: Int = 0,
    val dropOffDays: String = "",
    val recommendation: String = ""
)

@Immutable
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
    val errorMessage: String? = null,
    val habitReport: HabitReportData? = null,
    val isGeneratingReport: Boolean = false
) {
    val dueHabits: List<HabitItem>
        get() = habits.filter { isHabitDueToday(it.frequency) }

    val completedCount: Int
        get() = dueHabits.count { it.isCompletedToday }

    val totalCount: Int
        get() = dueHabits.size

    val completionRate: Float
        get() = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    companion object {
        fun isHabitDueToday(frequency: String): Boolean {
            val f = frequency.trim().lowercase()
            if (f == "daily" || f.isBlank()) return true
            val today = java.time.LocalDate.now().dayOfWeek
            if (f == "weekdays") {
                return today != java.time.DayOfWeek.SATURDAY && today != java.time.DayOfWeek.SUNDAY
            }
            if (f == "weekly" || f == "weekends") {
                return today == java.time.DayOfWeek.SATURDAY || today == java.time.DayOfWeek.SUNDAY
            }
            val dayMap = mapOf(
                "sun" to java.time.DayOfWeek.SUNDAY,
                "mon" to java.time.DayOfWeek.MONDAY,
                "tue" to java.time.DayOfWeek.TUESDAY,
                "wed" to java.time.DayOfWeek.WEDNESDAY,
                "thu" to java.time.DayOfWeek.THURSDAY,
                "fri" to java.time.DayOfWeek.FRIDAY,
                "sat" to java.time.DayOfWeek.SATURDAY
            )
            val tokens = f.split(",", " ", ";").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
            for (token in tokens) {
                val matched = dayMap.entries.firstOrNull { token.startsWith(it.key) }?.value
                if (matched == today) return true
            }
            return false
        }
    }
}

class HabitViewModel(
    private val repository: ProductivityRepository = ProductivityRepository(),
    private val aiRepository: AiRepository = AiRepository(),
    private var sessionManager: SessionManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitUiState(isLoading = true))
    val uiState: StateFlow<HabitUiState> = _uiState.asStateFlow()

    private var currentUserId: Int = 1

    fun initSessionManager(context: android.content.Context, manager: SessionManager) {
        initSessionManager(manager)
    }

    fun initSessionManager(manager: SessionManager) {
        this.sessionManager = manager
        loadHabits()
    }

    fun loadHabits() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            try {
                sessionManager?.let { sm ->
                    val session = sm.userSessionFlow.first()
                    if (session.userId != null && session.userId > 0) {
                        currentUserId = session.userId
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("HabitViewModel", "Failed to read session: ${e.message}")
            }

            val result = try {
                repository.getHabits(currentUserId)
            } catch (e: Exception) {
                android.util.Log.e("HabitViewModel", "Failed to get habits: ${e.message}")
                Result.failure(e)
            }
            launch {
                try {
                    StreakRepository.getInstance().getStreaks(currentUserId)
                } catch (e: Exception) {
                }
            }
            withContext(Dispatchers.Main) {
                result.fold(
                    onSuccess = { res ->
                        val insight = computeDynamicHabitInsight(res.habits, res.userStats)
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                habits = res.habits, 
                                userStats = res.userStats,
                                aiInsight = it.aiInsight ?: insight
                            ) 
                        }
                    },
                    onFailure = { err ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = err.localizedMessage) }
                    }
                )
            }
        }
    }

    fun loadAiInsight(forceRefresh: Boolean = false) {
        if (!forceRefresh && _uiState.value.aiInsight != null) return
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isAiLoading = true) }
            }
            val result = aiRepository.getHabitInsights(currentUserId)
            withContext(Dispatchers.Main) {
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
    }

    fun selectTab(tab: HabitTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab == HabitTab.INSIGHTS) {
            loadAiInsight()
            if (_uiState.value.habitReport == null) {
                generateHabitReport()
            }
        }
    }

    fun generateHabitReport() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isGeneratingReport = true) }
            }

            val habits = _uiState.value.habits
            val userStats = _uiState.value.userStats

            val consistencyScore = if (userStats.avgConsistencyPercent > 0) {
                userStats.avgConsistencyPercent.toInt().coerceIn(0, 100)
            } else if (habits.isNotEmpty()) {
                val comp = habits.count { it.isCompletedToday }
                ((comp.toFloat() / habits.size.toFloat()) * 100).toInt().coerceIn(0, 100)
            } else {
                75
            }

            val topHabit = habits.maxByOrNull { it.streakCount }
            val strongestTitle = topHabit?.title ?: (habits.firstOrNull()?.title ?: "Daily Habit")
            val strongestStreak = maxOf(topHabit?.streakCount ?: 0, userStats.currentStreak)

            val missedDays = userStats.weeklyChartData
                .filter { it.totalCount > 0 && it.completedCount < it.totalCount }
                .map { it.day }
            val dropOffDaysStr = when {
                missedDays.isNotEmpty() -> missedDays.joinToString(", ") + " (lower completion)"
                habits.any { !it.isCompletedToday } -> "Weekend & late evening routines"
                else -> "None detected (consistent all scheduled days)"
            }

            val prompt = """
                Analyze the following user habit performance and produce an actionable coaching report:
                - Active Habits: ${habits.joinToString { "${it.title} (Freq: ${it.frequency}, Streak: ${it.streakCount}d, CompletedToday: ${it.isCompletedToday})" }}
                - Current Overall Streak: ${userStats.currentStreak} days (Longest: ${userStats.longestStreak} days)
                - Consistency Score: $consistencyScore%
                - Strongest Routine: $strongestTitle ($strongestStreak day streak)
                - Drop-Off / Missed Days: $dropOffDaysStr
                Provide a single concise, highly actionable recommendation for overcoming drop-off days and sustaining habit streaks.
            """.trimIndent()

            var aiRecommendation: String? = null
            try {
                val res = aiRepository.getAiInsights(userId = currentUserId, queryType = "habit_report", prompt = prompt)
                res.onSuccess { resp ->
                    val text = resp.insight ?: resp.message
                    if (!text.isNullOrBlank() &&
                        !text.contains("offline", ignoreCase = true) &&
                        !text.contains("port 8000", ignoreCase = true) &&
                        !text.contains("deep-reasoning engine", ignoreCase = true)) {
                        aiRecommendation = text
                    }
                }
            } catch (_: Exception) {}

            val finalRecommendation = aiRecommendation ?: when {
                consistencyScore >= 80 -> "Pair your secondary habits immediately following $strongestTitle to anchor automatic execution across high-energy intervals."
                missedDays.isNotEmpty() -> "Set proactive calendar reminders on ${missedDays.first()} and reduce the initial daily commitment to under 10 minutes to eliminate friction."
                else -> "Anchor your daily routines immediately after your morning focus block to protect streak continuity and eliminate evening fatigue drop-off."
            }

            val report = HabitReportData(
                consistencyScore = consistencyScore,
                strongestHabit = strongestTitle,
                strongestHabitStreak = strongestStreak,
                dropOffDays = dropOffDaysStr,
                recommendation = finalRecommendation
            )

            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(habitReport = report, isGeneratingReport = false) }
            }
        }
    }

    fun selectTimeframe(timeframe: HabitInsightTimeframe) {
        _uiState.update { it.copy(selectedTimeframe = timeframe) }
    }

    fun selectHabitForStreak(habit: HabitItem?) {
        _uiState.update { it.copy(selectedHabitForStreak = habit) }
        if (habit != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    StreakRepository.getInstance().getStreaks(currentUserId)
                } catch (e: Exception) {
                }
            }
        }
    }

    fun setAddHabitDialogOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isAddHabitDialogOpen = isOpen) }
    }

    fun toggleHabit(habitId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
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
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(habits = currentList) }
                }

                repository.toggleHabit(currentUserId, habitId)
                launch {
                    try {
                        StreakRepository.getInstance().updateStreak(currentUserId, updatedStreak)
                    } catch (e: Exception) {
                    }
                }

                val refreshed = repository.getHabits(currentUserId)
                refreshed.onSuccess { res ->
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(habits = res.habits, userStats = res.userStats) }
                    }
                }
            }
        }
    }

    fun createHabit(title: String, icon: String = "bolt", frequency: String = "daily") {
        if (title.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isAddHabitDialogOpen = false) }
            }

            val result = repository.createHabit(
                userId = currentUserId,
                title = title.trim(),
                icon = icon,
                frequency = frequency
            )

            result.fold(
                onSuccess = { createdHabit ->
                    val updated = _uiState.value.habits.toMutableList().apply { add(createdHabit) }
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(habits = updated) }
                    }
                    val refreshed = repository.getHabits(currentUserId)
                    refreshed.onSuccess { res ->
                        withContext(Dispatchers.Main) {
                            _uiState.update { it.copy(habits = res.habits, userStats = res.userStats) }
                        }
                    }
                },
                onFailure = { err ->
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(errorMessage = err.localizedMessage) }
                    }
                }
            )
        }
    }

    fun deleteHabit(habitId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = _uiState.value.habits.filter { it.id != habitId }
            withContext(Dispatchers.Main) {
                _uiState.update { 
                    it.copy(
                        habits = updated,
                        selectedHabitForStreak = if (it.selectedHabitForStreak?.id == habitId) null else it.selectedHabitForStreak
                    ) 
                }
            }
            repository.deleteHabit(currentUserId, habitId)
            val refreshed = repository.getHabits(currentUserId)
            refreshed.onSuccess { res ->
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(habits = res.habits, userStats = res.userStats) }
                }
            }
        }
    }

    fun computeDynamicHabitInsight(habits: List<HabitItem>, stats: HabitUserStats): String {
        if (habits.isEmpty()) {
            return "Start building consistency by creating your first daily habit."
        }
        val topStreakHabit = habits.maxByOrNull { it.streakCount }
        val allCompletedToday = habits.isNotEmpty() && habits.all { it.isCompletedToday }
        val completedCount = habits.count { it.isCompletedToday }

        return when {
            allCompletedToday -> "Flawless routine today! All ${habits.size} daily habits completed."
            topStreakHabit != null && topStreakHabit.streakCount >= 3 ->
                "${topStreakHabit.title} is your longest standing streak at ${topStreakHabit.streakCount} days."
            completedCount > 0 ->
                "You're most consistent on weekday mornings ($completedCount/${habits.size} habits logged today)."
            else ->
                "Stay consistent today: complete your daily habits to maintain your ${maxOf(1, stats.currentStreak)}-day streak."
        }
    }
}
