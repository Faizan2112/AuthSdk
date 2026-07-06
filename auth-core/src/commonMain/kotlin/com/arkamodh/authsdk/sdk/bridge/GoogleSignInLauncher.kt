package com.arkamodh.authsdk.sdk.bridge

/**
 * Platform-specific class designed to launch the native Google Sign-in Account Chooser
 * and return the OAuth ID Token.
 */
public expect class GoogleSignInLauncher() {
    public fun launch(
        onSuccess: (idToken: String) -> Unit,
        onFailure: (Throwable) -> Unit
    )
}
