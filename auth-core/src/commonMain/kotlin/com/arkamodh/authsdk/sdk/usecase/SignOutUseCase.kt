package com.arkamodh.authsdk.sdk.usecase

import com.arkamodh.authsdk.sdk.repository.AuthRepository

/**
 * Use case to sign out the currently authenticated user.
 */
public class SignOutUseCase(private val repository: AuthRepository) {
    public suspend operator fun invoke() {
        repository.signOut()
    }
}
