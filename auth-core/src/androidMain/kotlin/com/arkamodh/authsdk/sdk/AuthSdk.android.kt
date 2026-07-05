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
internal lateinit var sdkContext: Context

public fun AuthSdk.Companion.initialize(context: Context) {
    sdkContext = context.applicationContext
    initialize()
}

