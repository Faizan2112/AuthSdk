# Proguard and R8 Consumer Rules for AuthSdk

# Keep the main facade
-keep public class com.arkamodh.authsdk.sdk.AuthSdk {
    public *;
}

# Keep public domain models
-keep public class com.arkamodh.authsdk.sdk.model.AuthUser {
    public *;
}
-keep public class com.arkamodh.authsdk.sdk.model.AuthResult {
    public *;
}
-keep public class com.arkamodh.authsdk.sdk.model.AuthProvider {
    public *;
}
-keep public class com.arkamodh.authsdk.sdk.model.AuthException {
    public *;
}
-keep public class com.arkamodh.authsdk.sdk.model.InvalidCredentialsException {
    public *;
}
-keep public class com.arkamodh.authsdk.sdk.model.UserNotFoundException {
    public *;
}
-keep public class com.arkamodh.authsdk.sdk.model.UserCollisionException {
    public *;
}
-keep public class com.arkamodh.authsdk.sdk.model.WeakPasswordException {
    public *;
}
-keep public class com.arkamodh.authsdk.sdk.model.NetworkException {
    public *;
}
-keep public class com.arkamodh.authsdk.sdk.model.UnknownAuthException {
    public *;
}

# Keep public use cases
-keep public class com.arkamodh.authsdk.sdk.usecase.SignInUseCase { public *; }
-keep public class com.arkamodh.authsdk.sdk.usecase.SignUpUseCase { public *; }
-keep public class com.arkamodh.authsdk.sdk.usecase.SignOutUseCase { public *; }
-keep public class com.arkamodh.authsdk.sdk.usecase.ResetPasswordUseCase { public *; }
-keep public class com.arkamodh.authsdk.sdk.usecase.GetCurrentUserUseCase { public *; }
-keep public class com.arkamodh.authsdk.sdk.usecase.ObserveAuthStateUseCase { public *; }
-keep public class com.arkamodh.authsdk.sdk.usecase.SignInWithCredentialUseCase { public *; }
-keep public class com.arkamodh.authsdk.sdk.usecase.GetIdTokenUseCase { public *; }

# Keep platform bridges since they are accessed by client platforms (Android / iOS)
-keep public interface com.arkamodh.authsdk.sdk.bridge.NativeAuthBridge { public *; }
-keep public interface com.arkamodh.authsdk.sdk.bridge.NativeAuthUser { public *; }
-keep public interface com.arkamodh.authsdk.sdk.bridge.NativeAuthResult { public *; }
-keep public interface com.arkamodh.authsdk.sdk.bridge.CancelableSubscription { public *; }
