package com.simats.focusflow.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.focusflow.data.repository.ProfileRepository
import com.simats.focusflow.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val name: String = "",
    val email: String = "",
    val bio: String = "",
    val avatar: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class EditProfileViewModel(
    private val repository: ProfileRepository = ProfileRepository(),
    private var sessionManager: SessionManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private var currentUserId: Int = 1

    fun initSessionManager(manager: SessionManager) {
        this.sessionManager = manager
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            sessionManager?.let { sm ->
                val session = sm.userSessionFlow.first()
                if (session.userId != null && session.userId > 0) {
                    currentUserId = session.userId
                }
            }

            val result = repository.getProfile(currentUserId)
            result.fold(
                onSuccess = { p ->
                    val session = sessionManager?.userSessionFlow?.first()
                    val sessionName = session?.userName
                    val sessionEmail = session?.userEmail
                    val sessionBio = session?.userBio
                    val sessionAvatar = session?.userAvatar
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = if (!sessionName.isNullOrBlank()) sessionName else p.name,
                            email = if (!sessionEmail.isNullOrBlank()) sessionEmail else p.email,
                            bio = if (!sessionBio.isNullOrBlank()) sessionBio else p.bio,
                            avatar = if (!sessionAvatar.isNullOrBlank()) sessionAvatar else if (p.avatar.isNotBlank()) p.avatar else ""
                        )
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoading = false) }
                }
            )
        }
    }

    fun onNameChange(name: String) {
        _uiState.update {
            it.copy(
                name = name,
                nameError = if (name.trim().length < 2) "Name must be at least 2 characters" else null,
                errorMessage = null
            )
        }
    }

    fun onEmailChange(email: String) {
        val isValid = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
        _uiState.update {
            it.copy(
                email = email,
                emailError = if (!isValid) "Please enter a valid email address" else null,
                errorMessage = null
            )
        }
    }

    fun onBioChange(bio: String) {
        _uiState.update { it.copy(bio = bio) }
    }

    fun onAvatarChange(avatar: String) {
        _uiState.update { it.copy(avatar = avatar) }
    }

    fun saveProfile(onSuccess: () -> Unit) {
        val name = _uiState.value.name.trim()
        val email = _uiState.value.email.trim()
        val bio = _uiState.value.bio.trim()
        val avatar = _uiState.value.avatar

        if (name.length < 2) {
            _uiState.update { it.copy(nameError = "Name must be at least 2 characters") }
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(emailError = "Please enter a valid email address") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.updateProfile(
                userId = currentUserId,
                name = name,
                email = email,
                bio = bio,
                avatar = avatar,
                sessionManager = sessionManager
            )

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            successMessage = "Profile updated successfully!"
                        )
                    }
                    onSuccess()
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.localizedMessage ?: "Failed to update profile"
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}

