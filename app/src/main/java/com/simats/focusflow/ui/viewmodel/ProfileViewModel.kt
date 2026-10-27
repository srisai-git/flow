package com.simats.focusflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.focusflow.data.api.AuthApiService
import com.simats.focusflow.data.api.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val profile: UserProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel(
    private val apiService: AuthApiService? = null
) : ViewModel() {

    private val defaultProfile = UserProfile(
        id = 1,
        name = "Sri Sai",
        avatar = "👤",
        role = "Student",
        bio = "B.Sc. AI & Data Science student passionate about focus routines.",
        location = "Chennai, India",
        email = "sri.sai@example.com"
    )

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Success(defaultProfile))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(userId: Int = 1) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                if (apiService != null) {
                    val response = apiService.getProfile(userId)
                    if (response.isSuccessful && response.body() != null) {
                        _uiState.value = ProfileUiState.Success(response.body()!!)
                    } else {
                        _uiState.value = ProfileUiState.Success(defaultProfile)
                    }
                } else {
                    _uiState.value = ProfileUiState.Success(defaultProfile)
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Success(defaultProfile)
            }
        }
    }

    fun updateProfile(updatedProfile: UserProfile, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                if (apiService != null) {
                    val response = apiService.updateProfile(updatedProfile)
                    if (response.isSuccessful) {
                        _uiState.value = ProfileUiState.Success(updatedProfile)
                        onSuccess()
                    } else {
                        _uiState.value = ProfileUiState.Error(response.body()?.message ?: "Failed to update profile")
                    }
                } else {
                    _uiState.value = ProfileUiState.Success(updatedProfile)
                    onSuccess()
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Success(updatedProfile)
                onSuccess()
            }
        }
    }
}