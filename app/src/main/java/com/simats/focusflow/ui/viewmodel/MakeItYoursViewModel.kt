package com.simats.focusflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.focusflow.data.api.AuthApiService
import com.simats.focusflow.data.api.RetrofitClient
import com.simats.focusflow.data.models.SavePreferencesRequest
import com.simats.focusflow.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State holding user selections on the "Make It Yours" onboarding screen.
 */
data class MakeItYoursState(
    val selectedFocusArea: String = "Coding & Projects",
    val dailyGoalHours: Int = 4,
    val preferredFocusStyle: String = "Pomodoro 25/5",
    val isLoading: Boolean = false,
    val isComplete: Boolean = false,
    val userId: Int = 1,
    val errorMessage: String? = null
)

/**
 * ViewModel orchestrating user customization options after Signup,
 * exposing StateFlow and handling saving preferences to flow_backend.
 */
class MakeItYoursViewModel(
    private val apiService: AuthApiService = RetrofitClient.instance,
    private var sessionManager: SessionManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(MakeItYoursState())
    val uiState: StateFlow<MakeItYoursState> = _uiState.asStateFlow()

    fun initSessionManager(manager: SessionManager) {
        this.sessionManager = manager
    }

    fun setUserId(id: Int) {
        _uiState.update { it.copy(userId = if (id > 0) id else 1) }
    }

    fun onFocusAreaSelected(area: String) {
        _uiState.update { it.copy(selectedFocusArea = area, errorMessage = null) }
    }

    fun onDailyGoalHoursChanged(hours: Int) {
        val clamped = hours.coerceIn(1, 12)
        _uiState.update { it.copy(dailyGoalHours = clamped, errorMessage = null) }
    }

    fun onFocusStyleSelected(style: String) {
        _uiState.update { it.copy(preferredFocusStyle = style, errorMessage = null) }
    }

    fun onSavePreferences(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val focusDuration = when (currentState.preferredFocusStyle) {
                "Pomodoro 25/5" -> 25
                "Deep Session 50/10" -> 50
                "Flow State" -> 60
                else -> 45
            }

            val goalMinutes = currentState.dailyGoalHours * 60

            val request = SavePreferencesRequest(
                userId = currentState.userId,
                primaryGoal = currentState.selectedFocusArea,
                preferredFocusDuration = focusDuration,
                dailyFocusGoalMinutes = goalMinutes,
                remindersEnabled = 1,
                reminderTime = "09:00 AM"
            )

            try {
                apiService.savePreferences(request)
            } catch (e: Exception) {
                // Fail-safe fallback for offline / disconnected situations
            }

            // Mark onboarding as completed in DataStore session
            sessionManager?.setOnboardingCompleted(true)

            _uiState.update { it.copy(isLoading = false, isComplete = true) }
            onSuccess()
        }
    }
}
