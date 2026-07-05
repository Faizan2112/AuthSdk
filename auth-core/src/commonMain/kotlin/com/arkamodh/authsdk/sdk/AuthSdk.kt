package com.arkamodh.authsdk.sdk

import com.arkamodh.authsdk.sdk.bridge.NativeAuthBridge
import com.arkamodh.authsdk.sdk.di.SdkContainer
import com.arkamodh.authsdk.sdk.usecase.*

/**
 * Main entrance facade for the AuthSdk.
 */
public class AuthSdk private constructor(internal val container: SdkContainer) {

    public val signIn: SignInUseCase get() = container.signInUseCase
    public val signUp: SignUpUseCase get() = container.signUpUseCase
    public val signOut: SignOutUseCase get() = container.signOutUseCase
    public val resetPassword: ResetPasswordUseCase get() = container.resetPasswordUseCase
    public val getCurrentUser: GetCurrentUserUseCase get() = container.getCurrentUserUseCase
    public val observeAuthState: ObserveAuthStateUseCase get() = container.observeAuthStateUseCase
    public val signInWithCredential: SignInWithCredentialUseCase get() = container.signInWithCredentialUseCase
    public val getIdToken: GetIdTokenUseCase get() = container.getIdTokenUseCase
    public val signInWithGoogle: SignInWithGoogleUseCase get() = container.signInWithGoogleUseCase

    public companion object {
        private var instance: AuthSdk? = null

        /**
         * Initializes the SDK with a platform-specific NativeAuthBridge.
         */
        public fun initialize(bridge: NativeAuthBridge) {
            if (instance == null) {
                instance = AuthSdk(SdkContainer(bridge))
            }
        }

        /**
         * Returns the initialized instance of AuthSdk.
         * Throws [IllegalStateException] if the SDK has not been initialized.
         */
        public fun getInstance(): AuthSdk {
            return instance ?: throw IllegalStateException("AuthSdk has not been initialized. Call initialize() first.")
        }

        /**
         * Resets the singleton instance. Intended for testing purposes.
         */
        internal fun reset() {
            instance = null
        }
    }
}

