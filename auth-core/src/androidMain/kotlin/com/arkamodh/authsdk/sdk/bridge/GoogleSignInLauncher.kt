package com.arkamodh.authsdk.sdk.bridge

import com.arkamodh.authsdk.sdk.GoogleSignInActivity
import com.arkamodh.authsdk.sdk.sdkContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

public actual class GoogleSignInLauncher actual constructor() {
    public actual fun launch(
        onSuccess: (idToken: String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        android.util.Log.d("GoogleSignInLauncher", "launch: Launching transparent GoogleSignInActivity")
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val idToken = GoogleSignInActivity.launch(sdkContext).await()
                android.util.Log.i("GoogleSignInLauncher", "launch: Google Sign-In succeeded, token obtained")
                onSuccess(idToken)
            } catch (e: Exception) {
                android.util.Log.e("GoogleSignInLauncher", "launch: Google Sign-In failed: ${e.message}", e)
                onFailure(e)
            }
        }
    }
}
