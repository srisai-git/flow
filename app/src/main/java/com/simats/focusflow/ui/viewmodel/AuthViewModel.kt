package com.simats.focusflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.focusflow.data.api.AuthApiService
import com.simats.focusflow.data.api.ResetPasswordRequest
import com.simats.focusflow.data.api.RetrofitClient
import com.simats.focusflow.data.models.LoginRequest
import com.simats.focusflow.data.models.RegisterRequest
import com.simats.focusflow.data.models.User
import com.simats.focusflow.data.repository.AuthRepository
import com.simats.focusflow.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for authentication screens holding loading status, success indicator,
 * error/success messages, and the authenticated user or token.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val user: User? = null,
    val token: String? = null
)

/**
 * ViewModel orchestrating authentication logic, input validation,
 * state hoisting, and asynchronous network dispatches via Coroutines.
 */
class AuthViewModel(
    private val repository: AuthRepository = AuthRepositoryImpl(),
    private val apiService: AuthApiService = RetrofitClient.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * Clears transient error messages from state.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Resets authentication state to its initial idle state.
     */
    fun resetState() {
        _uiState.value = AuthUiState()
    }

    // Alias for backwards compatibility
    fun resetUiState() = resetState()

    /**
     * Validates input and triggers user login.
     */
    fun onLogin(
        email: String,
        password: String,
        onSuccess: (() -> Unit)? = null
    ) {
        val trimmedEmail = email.trim()

        // Validation
        if (trimmedEmail.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter your email address.", isSuccess = false) }
            return
        }

        if (!isValidEmail(trimmedEmail)) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid email address.", isSuccess = false) }
            return
        }

        if (password.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter your password.", isSuccess = false) }
            return
        }

        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters.", isSuccess = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false) }

            val result = repository.login(LoginRequest(email = trimmedEmail, password = password))

            result.fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            errorMessage = null,
                            successMessage = response.message.ifBlank { "Login successful!" },
                            user = response.user,
                            token = response.token ?: response.user?.id?.toString()
                        )
                    }
                    onSuccess?.invoke()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = false,
                            errorMessage = error.localizedMessage ?: "Login failed. Please check your credentials."
                        )
                    }
                }
            )
        }
    }

    /**
     * Validates input and triggers new user registration.
     */
    fun onRegister(
        name: String,
        email: String,
        password: String,
        confirmPassword: String? = null,
        onSuccess: (() -> Unit)? = null
    ) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()

        // Validation
        if (trimmedName.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter your full name.", isSuccess = false) }
            return
        }

        if (trimmedName.length < 2) {
            _uiState.update { it.copy(errorMessage = "Name must be at least 2 characters long.", isSuccess = false) }
            return
        }

        if (trimmedEmail.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter your email address.", isSuccess = false) }
            return
        }

        if (!isValidEmail(trimmedEmail)) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid email address.", isSuccess = false) }
            return
        }

        if (password.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter a password.", isSuccess = false) }
            return
        }

        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters.", isSuccess = false) }
            return
        }

        if (confirmPassword != null) {
            if (confirmPassword.isEmpty()) {
                _uiState.update { it.copy(errorMessage = "Please confirm your password.", isSuccess = false) }
                return
            }
            if (password != confirmPassword) {
                _uiState.update { it.copy(errorMessage = "Passwords do not match.", isSuccess = false) }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false) }

            val result = repository.register(RegisterRequest(name = trimmedName, email = trimmedEmail, password = password))

            result.fold(
                onSuccess = { response ->
                    val newUser = response.user ?: User(
                        id = response.userId ?: 0,
                        name = trimmedName,
                        email = trimmedEmail
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            errorMessage = null,
                            successMessage = response.message.ifBlank { "Account created successfully!" },
                            user = newUser,
                            token = response.token ?: response.userId?.toString()
                        )
                    }
                    onSuccess?.invoke()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = false,
                            errorMessage = error.localizedMessage ?: "Signup failed. Please try again."
                        )
                    }
                }
            )
        }
    }

    /**
     * Backward compatibility helper for existing login callers.
     */
    fun login(email: String, password: String, onSuccess: () -> Unit) {
        onLogin(email = email, password = password, onSuccess = onSuccess)
    }

    /**
     * Password reset feature helper used by ForgotPasswordScreen.
     */
    fun resetPassword(
        email: String,
        newPassword: String,
        confirmPassword: String,
        onSuccess: (String) -> Unit
    ) {
        val trimmedEmail = email.trim()

        if (trimmedEmail.isBlank() || !isValidEmail(trimmedEmail)) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid email address.") }
            return
        }
        if (newPassword.length < 6) {
            _uiState.update { it.copy(errorMessage = "New password must be at least 6 characters.") }
            return
        }
        if (newPassword != confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = apiService.resetPassword(ResetPasswordRequest(email = trimmedEmail, new_password = newPassword))
                if (response.isSuccessful && response.body()?.status == true) {
                    val msg = response.body()?.message ?: "Password reset successfully!"
                    _uiState.update { it.copy(isLoading = false, isSuccess = true, successMessage = msg) }
                    onSuccess(msg)
                } else {
                    val msg = response.body()?.message ?: "Failed to reset password. Please try again."
                    _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Network error: ${e.localizedMessage ?: "Unable to connect"}"
                    )
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
