package com.arkamodh.authsdk.sdk.bridge

public actual class GoogleSignInLauncher actual constructor() {
    public companion object {
        public var iosLauncher: ((onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) -> Unit)? = null
    }

    public actual fun launch(
        onSuccess: (idToken: String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val launcher = iosLauncher
        if (launcher != null) {
            launcher(onSuccess, onFailure)
        } else {
            onFailure(Exception("iOS GoogleSignInLauncher is not configured from Swift. Set GoogleSignInLauncher.Companion.iosLauncher in iOS app startup."))
        }
    }
}
