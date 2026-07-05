package com.arkamodh.authsdk.sdk.bridge

import com.arkamodh.authsdk.sdk.model.*

/**
 * A bridge implementation that simulates Firebase Auth in memory.
 * Useful for local sandbox development, preview modes, and testing.
 */
public class SandboxAuthBridge : NativeAuthBridge {
    private var currentUser: NativeAuthUser? = null
    private val listeners = mutableListOf<(NativeAuthUser?) -> Unit>()

    private class SandboxUser(
        override val uid: String,
        override val email: String?,
        override val displayName: String?,
        override val isEmailVerified: Boolean
    ) : NativeAuthUser

    private class SandboxResult(override val user: NativeAuthUser?) : NativeAuthResult

    private class SandboxSubscription(private val onCancel: () -> Unit) : CancelableSubscription {
        override fun cancel() {
            onCancel()
        }
    }

    override fun signIn(
        email: String,
        password: String,
        completion: (NativeAuthResult?, Throwable?) -> Unit
    ) {
        if (email.contains("error") || password.length < 6) {
            completion(null, InvalidCredentialsException("Sandbox: Invalid email or password."))
            return
        }
        currentUser = SandboxUser("sandbox-uid-123", email, "Sandbox User", true)
        notifyListeners()
        completion(SandboxResult(currentUser), null)
    }

    override fun signUp(
        email: String,
        password: String,
        completion: (NativeAuthResult?, Throwable?) -> Unit
    ) {
        if (email.contains("error") || password.length < 6) {
            completion(null, WeakPasswordException("Sandbox: Password must be at least 6 characters."))
            return
        }
        currentUser = SandboxUser("sandbox-uid-123", email, "New Sandbox User", false)
        notifyListeners()
        completion(SandboxResult(currentUser), null)
    }

    override fun signInWithCredential(
        provider: AuthProvider,
        idToken: String,
        accessToken: String?,
        completion: (NativeAuthResult?, Throwable?) -> Unit
    ) {
        currentUser = SandboxUser("sandbox-${provider.name.lowercase()}-123", "${provider.name.lowercase()}@sandbox.com", "Sandbox Federated User", true)
        notifyListeners()
        completion(SandboxResult(currentUser), null)
    }

    override fun getIdToken(
        forceRefresh: Boolean,
        completion: (String?, Throwable?) -> Unit
    ) {
        if (currentUser == null) {
            completion(null, UserNotFoundException("Sandbox: No authenticated user session found."))
        } else {
            completion("sandbox-jwt-id-token", null)
        }
    }

    override fun signOut(completion: (Throwable?) -> Unit) {
        currentUser = null
        notifyListeners()
        completion(null)
    }

    override fun getCurrentUser(): NativeAuthUser? {
        return currentUser
    }

    override fun observeAuthState(onChanged: (NativeAuthUser?) -> Unit): CancelableSubscription {
        listeners.add(onChanged)
        onChanged(currentUser)
        return SandboxSubscription {
            listeners.remove(onChanged)
        }
    }

    override fun sendPasswordResetEmail(email: String, completion: (Throwable?) -> Unit) {
        if (email.contains("error")) {
            completion(InvalidCredentialsException("Sandbox: Invalid email address."))
        } else {
            completion(null)
        }
    }

    override fun signInWithGoogle(completion: (NativeAuthResult?, Throwable?) -> Unit) {
        currentUser = SandboxUser("sandbox-google-123", "google@sandbox.com", "Sandbox Google User", true)
        notifyListeners()
        completion(SandboxResult(currentUser), null)
    }

    private fun notifyListeners() {
        listeners.forEach { it(currentUser) }
    }
}
