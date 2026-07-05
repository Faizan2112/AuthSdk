package com.arkamodh.authsdk.sdk.bridge

import com.arkamodh.authsdk.sdk.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import android.content.Context
import com.arkamodh.authsdk.sdk.GoogleSignInActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


internal class AndroidAuthUser(private val firebaseUser: FirebaseUser) : NativeAuthUser {
    override val uid: String get() = firebaseUser.uid
    override val email: String? get() = firebaseUser.email
    override val displayName: String? get() = firebaseUser.displayName
    override val isEmailVerified: Boolean get() = firebaseUser.isEmailVerified
}

internal class AndroidAuthResult(private val authResult: com.google.firebase.auth.AuthResult) : NativeAuthResult {
    override val user: NativeAuthUser? get() = authResult.user?.let { AndroidAuthUser(it) }
}

internal class AndroidCancelableSubscription(private val onCancel: () -> Unit) : CancelableSubscription {
    override fun cancel() {
        onCancel()
    }
}

internal class AndroidAuthBridge(
    private val context: Context,
    private val firebaseAuth: FirebaseAuth
) : NativeAuthBridge {

    override fun signIn(
        email: String,
        password: String,
        completion: (NativeAuthResult?, Throwable?) -> Unit
    ) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result?.let { AndroidAuthResult(it) }
                    completion(result, null)
                } else {
                    val ex = task.exception ?: Exception("Failed to sign in")
                    completion(null, mapFirebaseException(ex))
                }
            }
    }

    override fun signUp(
        email: String,
        password: String,
        completion: (NativeAuthResult?, Throwable?) -> Unit
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result?.let { AndroidAuthResult(it) }
                    completion(result, null)
                } else {
                    val ex = task.exception ?: Exception("Failed to sign up")
                    completion(null, mapFirebaseException(ex))
                }
            }
    }

    override fun signInWithCredential(
        provider: AuthProvider,
        idToken: String,
        accessToken: String?,
        completion: (NativeAuthResult?, Throwable?) -> Unit
    ) {
        val credential = when (provider) {
            AuthProvider.GOOGLE -> GoogleAuthProvider.getCredential(idToken, null)
            AuthProvider.APPLE -> OAuthProvider.newCredentialBuilder("apple.com")
                .setIdToken(idToken)
                .setAccessToken(accessToken)
                .build()
        }

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result?.let { AndroidAuthResult(it) }
                    completion(result, null)
                } else {
                    val ex = task.exception ?: Exception("Failed to sign in with credential")
                    completion(null, mapFirebaseException(ex))
                }
            }
    }

    override fun getIdToken(
        forceRefresh: Boolean,
        completion: (String?, Throwable?) -> Unit
    ) {
        val user = firebaseAuth.currentUser
        if (user == null) {
            completion(null, UserNotFoundException("No authenticated user session found"))
            return
        }
        user.getIdToken(forceRefresh)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    completion(task.result.token, null)
                } else {
                    val ex = task.exception ?: Exception("Failed to get ID token")
                    completion(null, mapFirebaseException(ex))
                }
            }
    }

    override fun signOut(completion: (Throwable?) -> Unit) {
        try {
            firebaseAuth.signOut()
            completion(null)
        } catch (e: Exception) {
            completion(mapFirebaseException(e))
        }
    }

    override fun getCurrentUser(): NativeAuthUser? {
        return firebaseAuth.currentUser?.let { AndroidAuthUser(it) }
    }

    override fun observeAuthState(onChanged: (NativeAuthUser?) -> Unit): CancelableSubscription {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            onChanged(auth.currentUser?.let { AndroidAuthUser(it) })
        }
        firebaseAuth.addAuthStateListener(listener)
        return AndroidCancelableSubscription {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    override fun sendPasswordResetEmail(email: String, completion: (Throwable?) -> Unit) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    completion(null)
                } else {
                    val ex = task.exception ?: Exception("Failed to send password reset email")
                    completion(mapFirebaseException(ex))
                }
            }
    }

    override fun signInWithGoogle(completion: (NativeAuthResult?, Throwable?) -> Unit) {
        android.util.Log.d("AndroidAuthBridge", "signInWithGoogle: Google authentication flow initiated")
        CoroutineScope(Dispatchers.Main).launch {
            try {
                android.util.Log.d("AndroidAuthBridge", "signInWithGoogle: Launching GoogleSignInActivity launcher...")
                val idToken = GoogleSignInActivity.launch(context).await()
                android.util.Log.i("AndroidAuthBridge", "signInWithGoogle: Google ID Token obtained successfully. Submitting to Firebase Auth...")
                signInWithCredential(AuthProvider.GOOGLE, idToken, null) { result, error ->
                    if (error != null) {
                        android.util.Log.e("AndroidAuthBridge", "signInWithGoogle: Firebase sign-in failed: ${error.message}", error)
                    } else {
                        android.util.Log.i("AndroidAuthBridge", "signInWithGoogle: Firebase sign-in successful! User UID: ${result?.user?.uid}")
                    }
                    completion(result, error)
                }
            } catch (e: Exception) {
                android.util.Log.e("AndroidAuthBridge", "signInWithGoogle: Google sign-in flow failed: ${e.message}", e)
                completion(null, e)
            }
        }
    }

    private fun mapFirebaseException(exception: Exception): Throwable {
        return when (exception) {
            is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> {
                WeakPasswordException(exception.message ?: "Weak password", exception)
            }
            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                InvalidCredentialsException(exception.message ?: "Invalid credentials", exception)
            }
            is com.google.firebase.auth.FirebaseAuthUserCollisionException -> {
                UserCollisionException(exception.message ?: "Email already in use", exception)
            }
            is com.google.firebase.auth.FirebaseAuthInvalidUserException -> {
                UserNotFoundException(exception.message ?: "User not found or disabled", exception)
            }
            is com.google.firebase.FirebaseNetworkException -> {
                NetworkException(exception.message ?: "Network error occurred", exception)
            }
            is com.google.firebase.auth.FirebaseAuthException -> {
                val code = exception.errorCode
                UnknownAuthException(exception.message ?: "Firebase Auth error: $code", code, exception)
            }
            else -> {
                UnknownAuthException(exception.message ?: "Unknown error", null, exception)
            }
        }
    }
}
