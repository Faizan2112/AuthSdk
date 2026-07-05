package com.arkamodh.authsdk.sdk.bridge

/**
 * Bridge interface representing a platform-specific Firebase authentication result.
 */
public interface NativeAuthResult {
    public val user: NativeAuthUser?
}
