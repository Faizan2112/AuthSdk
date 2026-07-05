package com.arkamodh.authsdk.sdk.bridge

/**
 * Bridge interface representing a platform-specific Firebase user.
 */
public interface NativeAuthUser {
    public val uid: String
    public val email: String?
    public val displayName: String?
    public val isEmailVerified: Boolean
}
