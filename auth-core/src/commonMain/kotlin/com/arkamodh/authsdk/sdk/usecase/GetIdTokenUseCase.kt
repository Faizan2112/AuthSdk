package com.arkamodh.authsdk.sdk.usecase

import com.arkamodh.authsdk.sdk.repository.AuthRepository

/**
 * Use case to retrieve the user's Firebase Auth ID token (JWT) to attach to custom backend APIs.
 */
public class GetIdTokenUseCase(private val repository: AuthRepository) {
    public suspend operator fun invoke(forceRefresh: Boolean): String? {
        return repository.getIdToken(forceRefresh)
    }
}
