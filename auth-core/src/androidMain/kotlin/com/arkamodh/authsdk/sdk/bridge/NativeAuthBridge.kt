package com.arkamodh.authsdk.sdk.bridge

import com.arkamodh.authsdk.sdk.model.AuthProvider
import com.arkamodh.authsdk.sdk.sdkContext
import com.google.firebase.auth.FirebaseAuth

public actual class NativeAuthBridge actual constructor() {
    public companion object {
        public var useSandbox: Boolean = false
    }

    private val delegate: NativeAuthBridgeDelegate by lazy {
        if (useSandbox) {
            SandboxAuthBridge()
        } else {
            try {
                AndroidAuthBridge(sdkContext, FirebaseAuth.getInstance())
            } catch (e: Exception) {
                println("NativeAuthBridge: Failed to initialize AndroidAuthBridge (likely due to missing google-services.json): ${e.message}. Falling back to SandboxAuthBridge.")
                SandboxAuthBridge()
            }
        }
    }

    public actual fun signIn(
        email: String,
        password: String,
        completion: (NativeAuthResult?, Throwable?) -> Unit
    ) {
        delegate.signIn(email, password, completion)
    }

    public actual fun signUp(
        email: String,
        password: String,
        completion: (NativeAuthResult?, Throwable?) -> Unit
    ) {
        delegate.signUp(email, password, completion)
    }

    public actual fun signInWithCredential(
        provider: AuthProvider,
        idToken: String,
        accessToken: String?,
        completion: (NativeAuthResult?, Throwable?) -> Unit
    ) {
        delegate.signInWithCredential(provider, idToken, accessToken, completion)
    }

    public actual fun getIdToken(
        forceRefresh: Boolean,
        completion: (String?, Throwable?) -> Unit
    ) {
        delegate.getIdToken(forceRefresh, completion)
    }

    public actual fun signOut(completion: (Throwable?) -> Unit) {
        delegate.signOut(completion)
    }

    public actual fun getCurrentUser(): NativeAuthUser? {
        return delegate.getCurrentUser()
    }

    public actual fun observeAuthState(onChanged: (NativeAuthUser?) -> Unit): CancelableSubscription {
        return delegate.observeAuthState(onChanged)
    }

    public actual fun sendPasswordResetEmail(
        email: String,
        completion: (Throwable?) -> Unit
    ) {
        delegate.sendPasswordResetEmail(email, completion)
    }
}
