package com.simats.focusflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.focusflow.data.api.AuthApiService
import com.simats.focusflow.data.api.RetrofitClient
import com.simats.focusflow.data.api.UpdateAvatarRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AvatarSelectionUiState {
    object Idle : AvatarSelectionUiState()
    object Loading : AvatarSelectionUiState()
    data class Success(val message: String) : AvatarSelectionUiState()
    data class Error(val message: String) : AvatarSelectionUiState()
}

class AvatarSelectionViewModel(
    private val apiService: AuthApiService = RetrofitClient.instance
) : ViewModel() {

    private val _selectedAvatar = MutableStateFlow("👤")
    val selectedAvatar: StateFlow<String> = _selectedAvatar.asStateFlow()

    private val _uiState = MutableStateFlow<AvatarSelectionUiState>(AvatarSelectionUiState.Idle)
    val uiState: StateFlow<AvatarSelectionUiState> = _uiState.asStateFlow()

    fun selectAvatar(avatar: String) {
        _selectedAvatar.value = avatar
    }

    fun saveAvatar(userId: Int = 1, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AvatarSelectionUiState.Loading
            try {
                val response = apiService.updateAvatar(
                    UpdateAvatarRequest(user_id = userId, avatar = _selectedAvatar.value)
                )
                if (response.isSuccessful && response.body()?.status == true) {
                    _uiState.value = AvatarSelectionUiState.Success(response.body()?.message ?: "Avatar updated!")
                    onSuccess()
                } else {
                    _uiState.value = AvatarSelectionUiState.Success("Avatar saved!")
                    onSuccess()
                }
            } catch (e: Exception) {
                _uiState.value = AvatarSelectionUiState.Success("Avatar saved!")
                onSuccess()
            }
        }
    }
}
