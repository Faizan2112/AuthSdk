package com.arkamodh.authsdk.sdk.usecase

import com.arkamodh.authsdk.sdk.model.AuthUser
import com.arkamodh.authsdk.sdk.repository.AuthRepository

/**
 * Use case to retrieve the currently signed-in user (if any).
 */
public class GetCurrentUserUseCase(private val repository: AuthRepository) {
    public operator fun invoke(): AuthUser? {
        return repository.getCurrentUser()
    }
}
