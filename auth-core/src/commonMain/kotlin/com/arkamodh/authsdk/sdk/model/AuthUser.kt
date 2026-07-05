package com.arkamodh.authsdk.sdk.model

/**
 * Represents an authenticated user in the system.
 */
public class AuthUser(
    public val uid: String,
    public val email: String?,
    public val displayName: String?,
    public val isEmailVerified: Boolean
) {
    override fun toString(): String {
        return "AuthUser(uid='$uid', email=$email, displayName=$displayName, isEmailVerified=$isEmailVerified)"
    }
}
