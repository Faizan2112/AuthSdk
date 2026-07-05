package com.arkamodh.authsdk.sdk

import android.content.Context
import com.arkamodh.authsdk.sdk.bridge.AndroidAuthBridge
import com.google.firebase.auth.FirebaseAuth

/**
 * Initializes the AuthSdk for Android using the default Firebase project configuration.
 *
 * This automatically resolves the application context to prevent Activity/context memory leaks,
 * grabs the default [FirebaseAuth] instance, and registers it.
 */
public fun AuthSdk.Companion.initialize(context: Context) {
    // Prevent memory leaks by extracting applicationContext
    val appContext = context.applicationContext
    try {
        val firebaseAuth = FirebaseAuth.getInstance()
        initialize(AndroidAuthBridge(firebaseAuth))
    } catch (e: Exception) {
        println("AuthSdk: Firebase Auth failed to initialize (likely due to missing google-services.json): ${e.message}. Falling back to SandboxAuthBridge.")
        // Fallback to SandboxAuthBridge so the app can run out-of-the-box in demo mode
        initialize(com.arkamodh.authsdk.sdk.bridge.SandboxAuthBridge())
    }
}

