package com.arkamodh.authsdk.sdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.CompletableDeferred

internal class GoogleSignInActivity : Activity() {
    companion object {
        private const val TAG = "GoogleSignInActivity"
        private var deferredResult: CompletableDeferred<String>? = null

        fun launch(context: Context): CompletableDeferred<String> {
            Log.d(TAG, "launch: Initiating GoogleSignInActivity launch")
            val deferred = CompletableDeferred<String>()
            deferredResult = deferred
            val intent = Intent(context, GoogleSignInActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return deferred
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: GoogleSignInActivity transparent activity created")
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("224565820396-85i4pk65fnubofaa09amfbl20br28s8m.apps.googleusercontent.com")
            .requestEmail()
            .build()
            
        val client = GoogleSignIn.getClient(this, gso)
        
        Log.d(TAG, "onCreate: Signing out existing session first to force account chooser display")
        client.signOut().addOnCompleteListener { task ->
            Log.d(TAG, "onCreate: Existing session sign out completed. Launching Google Sign-in picker intent")
            startActivityForResult(client.signInIntent, 1001)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: received requestCode=$requestCode, resultCode=$resultCode")
        if (requestCode == 1001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    Log.i(TAG, "onActivityResult: Google Sign-in succeeded for account ${account.email}")
                    deferredResult?.complete(idToken)
                } else {
                    val err = Exception("Google Sign-In succeeded, but ID Token was null.")
                    Log.e(TAG, "onActivityResult: ID Token was null", err)
                    deferredResult?.completeExceptionally(err)
                }
            } catch (e: ApiException) {
                Log.e(TAG, "onActivityResult: Google Sign-In failed with ApiException. StatusCode: ${e.statusCode}. (Hint: Status code 10 means DEVELOPER_ERROR - usually caused by incorrect/missing SHA-1 keystore footprint configured in the Firebase Console)", e)
                deferredResult?.completeExceptionally(e)
            } catch (e: Exception) {
                Log.e(TAG, "onActivityResult: Google Sign-In failed with unexpected error: ${e.message}", e)
                deferredResult?.completeExceptionally(e)
            }
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: GoogleSignInActivity transparent activity destroyed")
        if (deferredResult?.isActive == true) {
            Log.w(TAG, "onDestroy: Flow terminated before completion. Returning cancellation error.")
            deferredResult?.completeExceptionally(Exception("Google Sign-In was cancelled or closed."))
        }
    }
}
