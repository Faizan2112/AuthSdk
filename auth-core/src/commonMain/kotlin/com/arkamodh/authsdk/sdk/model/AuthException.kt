package com.arkamodh.authsdk.sdk.model

/**
 * Base exception for all authentication-related failures.
 */
public sealed class AuthException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Thrown when credentials (email, password, etc.) are invalid, incorrect, or malformed.
 */
public class InvalidCredentialsException(
    message: String,
    cause: Throwable? = null
) : AuthException(message, cause)

/**
 * Thrown when attempting to sign in to an account that does not exist.
 */
public class UserNotFoundException(
    message: String,
    cause: Throwable? = null
) : AuthException(message, cause)

/**
 * Thrown when attempting to sign up with an email address that is already registered.
 */
public class UserCollisionException(
    message: String,
    cause: Throwable? = null
) : AuthException(message, cause)

/**
 * Thrown when the password does not meet the minimum safety requirements.
 */
public class WeakPasswordException(
    message: String,
    cause: Throwable? = null
) : AuthException(message, cause)

/**
 * Thrown when the authentication request fails due to network or connection issues.
 */
public class NetworkException(
    message: String,
    cause: Throwable? = null
) : AuthException(message, cause)

/**
 * Fallback exception for any unhandled or unknown authentication error codes.
 */
public class UnknownAuthException(
    message: String,
    public val code: String? = null,
    cause: Throwable? = null
) : AuthException(message, cause)
