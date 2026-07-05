package com.arkamodh.authsdk.sdk.repository

import com.arkamodh.authsdk.sdk.model.AuthResult
import com.arkamodh.authsdk.sdk.model.AuthUser
import com.arkamodh.authsdk.sdk.model.AuthProvider
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository representing the abstraction layer for authentication actions.
 */
public interface AuthRepository {
    public suspend fun signIn(email: String, password: String): AuthResult
    public suspend fun signUp(email: String, password: String): AuthResult
    public suspend fun signInWithCredential(provider: AuthProvider, idToken: String, accessToken: String? = null): AuthResult
    public suspend fun getIdToken(forceRefresh: Boolean): String?
    public suspend fun signOut()
    public fun getCurrentUser(): AuthUser?
    public val authStateFlow: Flow<AuthUser?>
    public suspend fun sendPasswordResetEmail(email: String)
    public suspend fun signInWithGoogle(): AuthResult
}
