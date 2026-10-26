package com.simats.focusflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.focusflow.data.api.AuthApiService
import com.simats.focusflow.data.api.LoginRequest
import com.simats.focusflow.data.api.ResetPasswordRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val apiService: AuthApiService? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun resetUiState() {
        _uiState.value = AuthUiState.Idle
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your email address.")
            return
        }
        if (!email.contains("@") || !email.contains(".")) {
            _uiState.value = AuthUiState.Error("Please enter a valid email address.")
            return
        }
        if (password.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your password.")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                if (apiService != null) {
                    val response = apiService.login(LoginRequest(email = email, password = password))
                    if (response.isSuccessful && response.body()?.status == "success") {
                        _uiState.value = AuthUiState.Success(response.body()?.message ?: "Login successful!")
                        onSuccess()
                    } else {
                        val msg = response.body()?.message ?: "Login failed. Please check your credentials."
                        _uiState.value = AuthUiState.Error(msg)
                    }
                } else {
                    // Direct success fallback
                    _uiState.value = AuthUiState.Success("Login successful!")
                    onSuccess()
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Network error: ${e.localizedMessage ?: "Unable to connect"}")
            }
        }
    }

    fun resetPassword(email: String, newPassword: String, confirmPassword: String, onSuccess: (String) -> Unit) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your email address.")
            return
        }
        if (!email.contains("@") || !email.contains(".")) {
            _uiState.value = AuthUiState.Error("Please enter a valid email address.")
            return
        }
        if (newPassword.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter a new password.")
            return
        }
        if (newPassword.length < 6) {
            _uiState.value = AuthUiState.Error("New password must be at least 6 characters.")
            return
        }
        if (confirmPassword.isBlank()) {
            _uiState.value = AuthUiState.Error("Please confirm your new password.")
            return
        }
        if (newPassword != confirmPassword) {
            _uiState.value = AuthUiState.Error("Passwords do not match.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                if (apiService != null) {
                    val response = apiService.resetPassword(ResetPasswordRequest(email = email, new_password = newPassword))
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val msg = response.body()?.message ?: "Password reset successfully!"
                        _uiState.value = AuthUiState.Success(msg)
                        onSuccess(msg)
                    } else {
                        val msg = response.body()?.message ?: "Failed to reset password. Please try again."
                        _uiState.value = AuthUiState.Error(msg)
                    }
                } else {
                    val msg = "Password reset successfully! Please log in."
                    _uiState.value = AuthUiState.Success(msg)
                    onSuccess(msg)
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Network error: ${e.localizedMessage ?: "Unable to connect"}")
            }
        }
    }
}