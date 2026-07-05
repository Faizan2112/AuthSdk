package com.arkamodh.authsdk.sdk.usecase

import com.arkamodh.authsdk.sdk.model.AuthResult
import com.arkamodh.authsdk.sdk.repository.AuthRepository

/**
 * Use case to sign in a user with an email and password.
 */
public class SignInUseCase(private val repository: AuthRepository) {
    public suspend operator fun invoke(email: String, password: String): AuthResult {
        return repository.signIn(email, password)
    }
}
