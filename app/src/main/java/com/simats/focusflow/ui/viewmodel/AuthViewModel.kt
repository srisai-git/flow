package com.simats.focusflow.ui.viewmodel

import android.content.Context
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
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.utils.ProfileImageManager
import kotlinx.coroutines.Dispatchers
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
 * state hoisting, session persistence via DataStore, and network dispatches.
 */
class AuthViewModel(
    private val repository: AuthRepository = AuthRepositoryImpl(),
    private val apiService: AuthApiService = RetrofitClient.instance,
    private var sessionManager: SessionManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var appContext: Context? = null

    /**
     * Binds the SessionManager singleton for persistence with context for avatar caching.
     */
    fun initSessionManager(context: Context, manager: SessionManager) {
        this.appContext = context.applicationContext
        this.sessionManager = manager
    }

    /**
     * Binds the SessionManager singleton for persistence.
     */
    fun initSessionManager(manager: SessionManager) {
        this.sessionManager = manager
    }

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
     * Validates input, triggers user login, and persists session in DataStore upon success.
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
                    val authenticatedUser = response.user ?: User(
                        id = 1,
                        name = trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                        email = trimmedEmail
                    )

                    // Persist session to DataStore (returning users are considered onboarded)
                    val effectiveAvatar = authenticatedUser.avatarUrl.ifBlank { authenticatedUser.avatar }
                    sessionManager?.saveSession(
                        userId = authenticatedUser.id,
                        name = authenticatedUser.name,
                        email = authenticatedUser.email,
                        avatar = effectiveAvatar,
                        bio = authenticatedUser.bio,
                        isOnboardingCompleted = true
                    )

                    // If remote avatar exists, cache it to internal storage in background
                    appContext?.let { ctx ->
                        if (effectiveAvatar.isNotBlank() && !effectiveAvatar.startsWith("avatar:") && (effectiveAvatar.startsWith("http") || effectiveAvatar.startsWith("data:"))) {
                            viewModelScope.launch(Dispatchers.IO) {
                                val localPath = ProfileImageManager.cacheRemoteAvatarToInternalStorage(ctx, effectiveAvatar, authenticatedUser.id)
                                if (localPath != null) {
                                    sessionManager?.saveAvatarPath(localPath, authenticatedUser.id)
                                }
                            }
                        }
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            errorMessage = null,
                            successMessage = response.message.ifBlank { "Login successful!" },
                            user = authenticatedUser,
                            token = response.token ?: authenticatedUser.id.toString()
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
     * Validates input, registers a new account, and persists initial session in DataStore.
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

                    // Persist initial session (new users still need to complete onboarding)
                    sessionManager?.saveSession(
                        userId = newUser.id,
                        name = newUser.name,
                        email = newUser.email,
                        isOnboardingCompleted = false
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
     * Clears user session from DataStore and resets UI state.
     */
    fun logout(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            sessionManager?.clearSession()
            resetState()
            onComplete()
        }
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
