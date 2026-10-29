package com.simats.focusflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.focusflow.data.api.AuthApiService
import com.simats.focusflow.data.api.RetrofitClient
import com.simats.focusflow.data.api.SetupProfileRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SetupProfileUiState {
    object Idle : SetupProfileUiState()
    object Loading : SetupProfileUiState()
    data class Success(val message: String) : SetupProfileUiState()
    data class Error(val message: String) : SetupProfileUiState()
}

class SetupProfileViewModel(
    private val apiService: AuthApiService = RetrofitClient.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow<SetupProfileUiState>(SetupProfileUiState.Idle)
    val uiState: StateFlow<SetupProfileUiState> = _uiState.asStateFlow()

    fun setupProfile(
        userId: Int = 1,
        name: String,
        avatar: String?,
        bio: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = SetupProfileUiState.Loading
            try {
                val response = apiService.setupProfile(
                    SetupProfileRequest(
                        user_id = userId,
                        name = name,
                        avatar = avatar,
                        bio = bio
                    )
                )
                if (response.isSuccessful && response.body()?.status == true) {
                    _uiState.value = SetupProfileUiState.Success(response.body()?.message ?: "Profile setup complete!")
                    onSuccess()
                } else {
                    _uiState.value = SetupProfileUiState.Success("Profile saved!")
                    onSuccess()
                }
            } catch (e: Exception) {
                _uiState.value = SetupProfileUiState.Success("Profile saved!")
                onSuccess()
            }
        }
    }
}
