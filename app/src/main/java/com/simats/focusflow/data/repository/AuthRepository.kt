package com.simats.focusflow.data.repository

import com.simats.focusflow.data.models.AuthResponse
import com.simats.focusflow.data.models.LoginRequest
import com.simats.focusflow.data.models.RegisterRequest
import kotlinx.coroutines.flow.Flow



interface AuthRepository {

    

    suspend fun login(request: LoginRequest): Result<AuthResponse>

    

    suspend fun register(request: RegisterRequest): Result<AuthResponse>

    

    fun loginFlow(request: LoginRequest): Flow<Result<AuthResponse>>

    

    fun registerFlow(request: RegisterRequest): Flow<Result<AuthResponse>>
}

