package com.arkamodh.authsdk.sdk.usecase

import com.arkamodh.authsdk.sdk.model.AuthResult
import com.arkamodh.authsdk.sdk.model.AuthProvider
import com.arkamodh.authsdk.sdk.repository.AuthRepository

/**
 * Use case to sign in a user using a federated identity provider's tokens (Google, Apple, etc.).
 */
public class SignInWithCredentialUseCase(private val repository: AuthRepository) {
    public suspend operator fun invoke(
        provider: AuthProvider,
        idToken: String,
        accessToken: String? = null
    ): AuthResult {
        return repository.signInWithCredential(provider, idToken, accessToken)
    }
}
