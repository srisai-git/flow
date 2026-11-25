package com.simats.focusflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.focusflow.data.api.AuthApiService
import com.simats.focusflow.data.api.RetrofitClient
import com.simats.focusflow.data.api.UpdateAvatarRequest
import com.simats.focusflow.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class AvatarSelectionUiState {
    object Idle : AvatarSelectionUiState()
    object Loading : AvatarSelectionUiState()
    data class Success(val message: String) : AvatarSelectionUiState()
    data class Error(val message: String) : AvatarSelectionUiState()
}

class AvatarSelectionViewModel(
    private val apiService: AuthApiService = RetrofitClient.instance,
    private var sessionManager: SessionManager? = null
) : ViewModel() {

    private val _selectedAvatar = MutableStateFlow("avatar:person")
    val selectedAvatar: StateFlow<String> = _selectedAvatar.asStateFlow()

    private val _uiState = MutableStateFlow<AvatarSelectionUiState>(AvatarSelectionUiState.Idle)
    val uiState: StateFlow<AvatarSelectionUiState> = _uiState.asStateFlow()

    fun initSessionManager(manager: SessionManager) {
        this.sessionManager = manager
    }

    fun selectAvatar(avatar: String) {
        _selectedAvatar.value = avatar
    }

    fun saveAvatar(userId: Int = 1, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AvatarSelectionUiState.Loading
            val avatarChoice = _selectedAvatar.value
            val effectiveUserId = sessionManager?.userSessionFlow?.first()?.userId ?: userId
            try {
                apiService.updateAvatar(
                    UpdateAvatarRequest(user_id = effectiveUserId, avatar = avatarChoice)
                )

                sessionManager?.let { sm ->
                    val currentSession = sm.userSessionFlow.first()
                    sm.updateProfile(
                        name = currentSession.userName ?: "",
                        email = currentSession.userEmail ?: "",
                        bio = currentSession.userBio ?: "",
                        avatar = avatarChoice,
                        userId = effectiveUserId
                    )
                    sm.saveAvatarPath(avatarChoice, effectiveUserId)
                }

                _uiState.value = AvatarSelectionUiState.Success("Avatar updated!")
                onSuccess()
            } catch (e: Exception) {
                sessionManager?.let { sm ->
                    val currentSession = sm.userSessionFlow.first()
                    sm.updateProfile(
                        name = currentSession.userName ?: "",
                        email = currentSession.userEmail ?: "",
                        bio = currentSession.userBio ?: "",
                        avatar = avatarChoice,
                        userId = effectiveUserId
                    )
                    sm.saveAvatarPath(avatarChoice, effectiveUserId)
                }
                _uiState.value = AvatarSelectionUiState.Success("Avatar saved locally!")
                onSuccess()
            }
        }
    }
}
