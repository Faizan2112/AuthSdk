package com.arkamodh.authsdk.sdk.model

/**
 * Represents the result of an authentication operation.
 */
public class AuthResult(
    public val user: AuthUser?
) {
    override fun toString(): String {
        return "AuthResult(user=$user)"
    }
}
