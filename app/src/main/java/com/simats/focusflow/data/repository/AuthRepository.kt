package com.simats.focusflow.data.repository

import com.simats.focusflow.data.models.AuthResponse
import com.simats.focusflow.data.models.LoginRequest
import com.simats.focusflow.data.models.RegisterRequest
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining authentication operations.
 * Returns Kotlin Result<AuthResponse> or Flow<Result<AuthResponse>>.
 */
interface AuthRepository {

    /**
     * Authenticates the user with email and password.
     */
    suspend fun login(request: LoginRequest): Result<AuthResponse>

    /**
     * Registers a new user with name, email, and password.
     */
    suspend fun register(request: RegisterRequest): Result<AuthResponse>

    /**
     * Reactive stream for user login.
     */
    fun loginFlow(request: LoginRequest): Flow<Result<AuthResponse>>

    /**
     * Reactive stream for user registration.
     */
    fun registerFlow(request: RegisterRequest): Flow<Result<AuthResponse>>
}

