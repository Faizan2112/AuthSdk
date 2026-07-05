package com.arkamodh.authsdk.sdk.repository

import com.arkamodh.authsdk.sdk.bridge.NativeAuthBridge
import com.arkamodh.authsdk.sdk.bridge.NativeAuthUser
import com.arkamodh.authsdk.sdk.bridge.NativeAuthResult
import com.arkamodh.authsdk.sdk.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Internal concrete repository adapting platform bridge callbacks to coroutines.
 */
internal class AuthRepositoryImpl(
    private val bridge: NativeAuthBridge
) : AuthRepository {

    private fun mapUser(nativeUser: NativeAuthUser?): AuthUser? {
        if (nativeUser == null) return null
        return AuthUser(
            uid = nativeUser.uid,
            email = nativeUser.email,
            displayName = nativeUser.displayName,
            isEmailVerified = nativeUser.isEmailVerified
        )
    }

    override suspend fun signIn(email: String, password: String): AuthResult = suspendCancellableCoroutine { continuation ->
        bridge.signIn(email, password) { result, error ->
            if (error != null) {
                continuation.resumeWithException(mapError(error))
            } else {
                continuation.resume(AuthResult(mapUser(result?.user)))
            }
        }
    }

    override suspend fun signUp(email: String, password: String): AuthResult = suspendCancellableCoroutine { continuation ->
        bridge.signUp(email, password) { result, error ->
            if (error != null) {
                continuation.resumeWithException(mapError(error))
            } else {
                continuation.resume(AuthResult(mapUser(result?.user)))
            }
        }
    }

    override suspend fun signInWithCredential(
        provider: AuthProvider,
        idToken: String,
        accessToken: String?
    ): AuthResult = suspendCancellableCoroutine { continuation ->
        bridge.signInWithCredential(provider, idToken, accessToken) { result, error ->
            if (error != null) {
                continuation.resumeWithException(mapError(error))
            } else {
                continuation.resume(AuthResult(mapUser(result?.user)))
            }
        }
    }

    override suspend fun getIdToken(forceRefresh: Boolean): String? = suspendCancellableCoroutine { continuation ->
        bridge.getIdToken(forceRefresh) { token, error ->
            if (error != null) {
                continuation.resumeWithException(mapError(error))
            } else {
                continuation.resume(token)
            }
        }
    }

    override suspend fun signOut(): Unit = suspendCancellableCoroutine { continuation ->
        bridge.signOut { error ->
            if (error != null) {
                continuation.resumeWithException(mapError(error))
            } else {
                continuation.resume(Unit)
            }
        }
    }

    override fun getCurrentUser(): AuthUser? {
        return mapUser(bridge.getCurrentUser())
    }

    override val authStateFlow: Flow<AuthUser?> = callbackFlow {
        val subscription = bridge.observeAuthState { nativeUser ->
            trySend(mapUser(nativeUser))
        }
        awaitClose {
            subscription.cancel()
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Unit = suspendCancellableCoroutine { continuation ->
        bridge.sendPasswordResetEmail(email) { error ->
            if (error != null) {
                continuation.resumeWithException(mapError(error))
            } else {
                continuation.resume(Unit)
            }
        }
    }

    private fun mapError(error: Throwable): Throwable {
        return if (error is AuthException) {
            error
        } else {
            UnknownAuthException(error.message ?: "Unknown authentication error", cause = error)
        }
    }
}
