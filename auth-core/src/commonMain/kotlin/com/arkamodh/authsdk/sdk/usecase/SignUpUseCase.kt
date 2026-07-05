package com.arkamodh.authsdk.sdk.usecase

import com.arkamodh.authsdk.sdk.model.AuthResult
import com.arkamodh.authsdk.sdk.repository.AuthRepository

/**
 * Use case to sign up / register a user with an email and password.
 */
public class SignUpUseCase(private val repository: AuthRepository) {
    public suspend operator fun invoke(email: String, password: String): AuthResult {
        return repository.signUp(email, password)
    }
}
