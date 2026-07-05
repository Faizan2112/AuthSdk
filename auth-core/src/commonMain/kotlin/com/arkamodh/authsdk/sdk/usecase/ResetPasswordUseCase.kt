package com.arkamodh.authsdk.sdk.usecase

import com.arkamodh.authsdk.sdk.repository.AuthRepository

/**
 * Use case to request a password reset email for a given user email address.
 */
public class ResetPasswordUseCase(private val repository: AuthRepository) {
    public suspend operator fun invoke(email: String) {
        repository.sendPasswordResetEmail(email)
    }
}
