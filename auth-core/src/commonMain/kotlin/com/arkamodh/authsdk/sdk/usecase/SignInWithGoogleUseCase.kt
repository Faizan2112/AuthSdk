package com.arkamodh.authsdk.sdk.usecase

import com.arkamodh.authsdk.sdk.model.AuthResult
import com.arkamodh.authsdk.sdk.repository.AuthRepository

/**
 * Use case to sign in a user using the native Google Sign-in flow.
 */
public class SignInWithGoogleUseCase(private val repository: AuthRepository) {
    public suspend operator fun invoke(): AuthResult {
        return repository.signInWithGoogle()
    }
}
