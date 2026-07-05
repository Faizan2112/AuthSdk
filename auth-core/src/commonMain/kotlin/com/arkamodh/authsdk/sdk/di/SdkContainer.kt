package com.arkamodh.authsdk.sdk.di

import com.arkamodh.authsdk.sdk.bridge.NativeAuthBridge
import com.arkamodh.authsdk.sdk.repository.AuthRepository
import com.arkamodh.authsdk.sdk.repository.AuthRepositoryImpl
import com.arkamodh.authsdk.sdk.usecase.*

/**
 * Internal dependency injection container. Holds all the singletons of the SDK core.
 */
internal class SdkContainer {
    val bridge: NativeAuthBridge = NativeAuthBridge()
    val repository: AuthRepository = AuthRepositoryImpl(bridge)

    val signInUseCase: SignInUseCase = SignInUseCase(repository)
    val signUpUseCase: SignUpUseCase = SignUpUseCase(repository)
    val signOutUseCase: SignOutUseCase = SignOutUseCase(repository)
    val resetPasswordUseCase: ResetPasswordUseCase = ResetPasswordUseCase(repository)
    val getCurrentUserUseCase: GetCurrentUserUseCase = GetCurrentUserUseCase(repository)
    val observeAuthStateUseCase: ObserveAuthStateUseCase = ObserveAuthStateUseCase(repository)
    val signInWithCredentialUseCase: SignInWithCredentialUseCase = SignInWithCredentialUseCase(repository)
    val getIdTokenUseCase: GetIdTokenUseCase = GetIdTokenUseCase(repository)
    val signInWithGoogleUseCase: SignInWithGoogleUseCase = SignInWithGoogleUseCase(repository)
}
