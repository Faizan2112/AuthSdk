package com.arkamodh.authsdk.sdk.bridge

import com.arkamodh.authsdk.sdk.model.AuthProvider

public actual class NativeAuthBridge actual constructor() {
    public companion object {
        public var iosBridge: NativeAuthBridgeDelegate? = null
        public var useSandbox: Boolean = false
    }

    private val delegate: NativeAuthBridgeDelegate by lazy {
        if (useSandbox) {
            SandboxAuthBridge()
        } else {
            iosBridge ?: throw IllegalStateException("iOS NativeAuthBridge.iosBridge delegate has not been set in Swift startup.")
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
