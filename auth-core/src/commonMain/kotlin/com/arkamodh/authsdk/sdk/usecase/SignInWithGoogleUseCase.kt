package com.arkamodh.authsdk.sdk.usecase

import com.arkamodh.authsdk.sdk.bridge.GoogleSignInLauncher
import com.arkamodh.authsdk.sdk.model.AuthResult
import com.arkamodh.authsdk.sdk.model.AuthProvider
import com.arkamodh.authsdk.sdk.repository.AuthRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Use case to sign in a user using the native Google Sign-in flow.
 */
public class SignInWithGoogleUseCase(private val repository: AuthRepository) {
    public suspend operator fun invoke(): AuthResult = suspendCancellableCoroutine { continuation ->
        val launcher = GoogleSignInLauncher()
        launcher.launch(
            onSuccess = { idToken ->
                // Hand the token off to the repository to complete Firebase sign-in
                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        val result = repository.signInWithCredential(AuthProvider.GOOGLE, idToken)
                        continuation.resume(result)
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }
            },
            onFailure = { error ->
                continuation.resumeWithException(error)
            }
        )
    }
}
