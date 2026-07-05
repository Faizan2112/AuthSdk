package com.arkamodh.authsdk.sdk.bridge

/**
 * iOS Helper class or markers.
 *
 * Swift developers can implement the [NativeAuthBridge] interface directly in Swift:
 *
 * ```swift
 * import AuthSdk
 * import FirebaseAuth
 *
 * class SwiftAuthBridge: NativeAuthBridge {
 *     // Implement swift methods delegating to Auth.auth()
 * }
 * ```
 */
public class IosAuthBridgeHelper {
    public fun getBridgeDetails(): String = "IosAuthBridge is ready to be implemented by Swift."
}
