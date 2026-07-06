package com.arkamodh.authsdk.sdk.bridge

import com.arkamodh.authsdk.sdk.model.AuthProvider

/**
 * Interface that platforms implement to delegate Firebase Auth operations.
 */
public interface NativeAuthBridgeDelegate {
    public fun signIn(
        email: String,
        password: String,
        completion: (NativeAuthResult?, Throwable?) -> Unit
    )

    public fun signUp(
        email: String,
        password: String,
        completion: (NativeAuthResult?, Throwable?) -> Unit
    )

    public fun signInWithCredential(
        provider: AuthProvider,
        idToken: String,
        accessToken: String?,
        completion: (NativeAuthResult?, Throwable?) -> Unit
    )

    public fun getIdToken(
        forceRefresh: Boolean,
        completion: (String?, Throwable?) -> Unit
    )

    public fun signOut(completion: (Throwable?) -> Unit)

    public fun getCurrentUser(): NativeAuthUser?

    public fun observeAuthState(onChanged: (NativeAuthUser?) -> Unit): CancelableSubscription

    public fun sendPasswordResetEmail(
        email: String,
        completion: (Throwable?) -> Unit
    )
}
