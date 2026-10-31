package com.simats.focusflow.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.focusflow.data.models.ProfileData
import com.simats.focusflow.data.repository.ProfileRepository
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.utils.ProfileImageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Immutable
data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: ProfileData = ProfileData(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showResetDialog: Boolean = false,
    val themeMode: String = "light"
)

class ProfileViewModel(
    private val repository: ProfileRepository = ProfileRepository(),
    private var sessionManager: SessionManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var currentUserId: Int = 1
    private var appContext: Context? = null

    fun initSessionManager(context: Context, manager: SessionManager) {
        this.appContext = context.applicationContext
        this.sessionManager = manager

        // Fast cold-start check: immediately load local avatar from disk if present
        val existingAvatar = ProfileImageManager.getAvatarPathIfExists(context, currentUserId)
        if (!existingAvatar.isNullOrBlank()) {
            _uiState.update { current ->
                current.copy(profile = current.profile.copy(avatar = existingAvatar))
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            manager.themeModeFlow.collect { mode ->
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(themeMode = mode) }
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            manager.userAvatarPath.collect { path ->
                if (!path.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        _uiState.update { current ->
                            current.copy(profile = current.profile.copy(avatar = path))
                        }
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
                withContext(Dispatchers.Main) {
                    _uiState.update { current ->
                        current.copy(
                            profile = current.profile.copy(
                                name = session.userName?.takeIf { it.isNotBlank() } ?: current.profile.name,
                                email = session.userEmail?.takeIf { it.isNotBlank() } ?: current.profile.email,
                                bio = session.userBio ?: current.profile.bio,
                                avatar = if (av.isNotBlank()) av else current.profile.avatar
                            )
                        )
                    }
                }
            }
        }
        loadProfile()
    }

    // Compatibility overload
    fun initSessionManager(manager: SessionManager) {
        this.sessionManager = manager
        viewModelScope.launch(Dispatchers.IO) {
            manager.themeModeFlow.collect { mode ->
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(themeMode = mode) }
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            manager.userAvatarPath.collect { path ->
                if (!path.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        _uiState.update { current ->
                            current.copy(profile = current.profile.copy(avatar = path))
                        }
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
                withContext(Dispatchers.Main) {
                    _uiState.update { current ->
                        current.copy(
                            profile = current.profile.copy(
                                name = session.userName?.takeIf { it.isNotBlank() } ?: current.profile.name,
                                email = session.userEmail?.takeIf { it.isNotBlank() } ?: current.profile.email,
                                bio = session.userBio ?: current.profile.bio,
                                avatar = if (av.isNotBlank()) av else current.profile.avatar
                            )
                        )
                    }
                }
            }
        }
        loadProfile()
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            sessionManager?.setThemeMode(mode)
            withContext(Dispatchers.Main) {
                _uiState.update { 
                    it.copy(
                        themeMode = mode, 
                        successMessage = if (mode == "light") "Clean Light theme activated." else "Default Dark theme activated."
                    ) 
                }
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            sessionManager?.let { sm ->
                val session = sm.userSessionFlow.first()
                if (session.userId != null && session.userId > 0) {
                    currentUserId = session.userId
                }
            }

            val result = repository.getProfile(currentUserId)
            withContext(Dispatchers.Main) {
                result.fold(
                    onSuccess = { data ->
                        val session = sessionManager?.userSessionFlow?.first()
                        val sName = session?.userName
                        val sEmail = session?.userEmail
                        val sAvatar = session?.userAvatarUri ?: session?.userAvatar
                        val sBio = session?.userBio

                        // If remote avatar is a URL/base64 and no local file exists, cache in background
                        appContext?.let { ctx ->
                            if (!data.avatar.isNullOrBlank() && !data.avatar.startsWith("avatar:") && (data.avatar.startsWith("http") || data.avatar.startsWith("data:"))) {
                                viewModelScope.launch(Dispatchers.IO) {
                                    val cached = ProfileImageManager.cacheRemoteAvatarToInternalStorage(ctx, data.avatar, currentUserId)
                                    if (cached != null) {
                                        sessionManager?.saveAvatarPath(cached, currentUserId)
                                    }
                                }
                            }
                        }

                        val localPath = appContext?.let { ProfileImageManager.getAvatarPathIfExists(it, currentUserId) }
                        val effectiveAvatar = when {
                            !localPath.isNullOrBlank() -> localPath
                            !sAvatar.isNullOrBlank() -> sAvatar
                            data.avatar.isNotBlank() -> data.avatar
                            else -> ""
                        }

                        val finalData = data.copy(
                            name = if (!sName.isNullOrBlank()) sName else data.name,
                            email = if (!sEmail.isNullOrBlank()) sEmail else data.email,
                            avatar = effectiveAvatar,
                            bio = if (!sBio.isNullOrBlank()) sBio else data.bio
                        )
                        _uiState.update { it.copy(isLoading = false, profile = finalData) }
                    },
                    onFailure = { err ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = err.localizedMessage) }
                    }
                )
            }
        }
    }

    fun toggleReminders() {
        val updated = !_uiState.value.profile.remindersEnabled
        val updatedProfile = _uiState.value.profile.copy(remindersEnabled = updated)
        _uiState.update {
            it.copy(
                profile = updatedProfile,
                successMessage = if (updated) "Daily focus reminders enabled." else "Daily focus reminders turned off."
            )
        }
    }

    fun setResetDialogOpen(open: Boolean) {
        _uiState.update { it.copy(showResetDialog = open) }
    }

    fun resetStats() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(showResetDialog = false, isLoading = true) }
            }
            val result = repository.resetStats(currentUserId)
            withContext(Dispatchers.Main) {
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                profile = it.profile.copy(
                                    totalFocusHours = 0f,
                                    totalFocusMinutes = 0,
                                    completedTasksCount = 0,
                                    currentStreak = 0
                                ),
                                successMessage = "Focus history and stats reset successfully."
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

    fun updateFocusPreferences(dailyGoalHours: Int, focusBlockMinutes: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val totalMinutes = dailyGoalHours * 60
            val currentProfile = _uiState.value.profile
            val updatedProfile = currentProfile.copy(
                dailyFocusGoalMinutes = totalMinutes,
                preferredFocusDuration = focusBlockMinutes
            )
            withContext(Dispatchers.Main) {
                _uiState.update { 
                    it.copy(
                        profile = updatedProfile,
                        successMessage = "Focus preferences saved: $dailyGoalHours hrs/day, ${focusBlockMinutes}m rhythm."
                    ) 
                }
            }
            repository.updatePreferences(
                userId = currentUserId,
                dailyFocusGoalMinutes = totalMinutes,
                preferredFocusDuration = focusBlockMinutes
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
