package com.simats.focusflow.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.focusflow.data.api.AuthApiService
import com.simats.focusflow.data.api.RetrofitClient
import com.simats.focusflow.data.models.SavePreferencesRequest
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.service.FocusTimerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MakeItYoursState(
    val selectedFocusArea: String = "Coding & Projects",
    val selectedGoalId: String = "3h",
    val dailyGoalHours: Int = 3,
    val defaultIntervalMinutes: Int = 45,
    val preferredFocusStyle: String = "Balanced",
    val isLoading: Boolean = false,
    val isComplete: Boolean = false,
    val userId: Int = 1,
    val errorMessage: String? = null
)

class MakeItYoursViewModel(
    private val apiService: AuthApiService = RetrofitClient.instance,
    private var sessionManager: SessionManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(MakeItYoursState())
    val uiState: StateFlow<MakeItYoursState> = _uiState.asStateFlow()
    private var appContext: Context? = null

    fun initSessionManager(manager: SessionManager) {
        this.sessionManager = manager
    }

    fun initSessionManager(context: Context, manager: SessionManager) {
        this.appContext = context.applicationContext
        this.sessionManager = manager
    }

    fun setUserId(id: Int) {
        _uiState.update { it.copy(userId = if (id > 0) id else 1) }
    }

    fun onFocusAreaSelected(area: String) {
        _uiState.update { it.copy(selectedFocusArea = area, errorMessage = null) }
    }

    fun onSelectDailyGoal(id: String, goalHours: Int, intervalMinutes: Int) {
        _uiState.update {
            it.copy(
                selectedGoalId = id,
                dailyGoalHours = goalHours,
                defaultIntervalMinutes = intervalMinutes,
                errorMessage = null
            )
        }
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

        viewModelScope.launch(Dispatchers.IO) {
            val goalMinutes = currentState.dailyGoalHours * 60
            val intervalMinutes = currentState.defaultIntervalMinutes

            sessionManager?.saveFocusGoalPreferences(goalMinutes, intervalMinutes)
            sessionManager?.setOnboardingCompleted(true)

            appContext?.let { ctx ->
                try {
                    FocusTimerService.setDurations(ctx, intervalMinutes, 5, 15)
                } catch (_: Exception) {}
            }

            ProductivityRepository.invalidateDashboardCache()

            val request = SavePreferencesRequest(
                userId = currentState.userId,
                primaryGoal = currentState.selectedFocusArea,
                preferredFocusDuration = intervalMinutes,
                dailyFocusGoalMinutes = goalMinutes,
                remindersEnabled = 1,
                reminderTime = "09:00 AM"
            )

            try {
                apiService.savePreferences(request)
            } catch (_: Exception) {}

            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isLoading = false, isComplete = true) }
                onSuccess()
            }
        }
    }
}
