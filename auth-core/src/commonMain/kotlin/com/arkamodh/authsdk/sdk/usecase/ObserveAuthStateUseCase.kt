package com.arkamodh.authsdk.sdk.usecase

import com.arkamodh.authsdk.sdk.model.AuthUser
import com.arkamodh.authsdk.sdk.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to observe the real-time authentication state changes of the user.
 */
public class ObserveAuthStateUseCase(private val repository: AuthRepository) {
    public operator fun invoke(): Flow<AuthUser?> {
        return repository.authStateFlow
    }
}
