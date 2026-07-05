# AuthCore KMM SDK

`auth-core` is a lightweight, high-performance Kotlin Multiplatform (KMM) Firebase Authentication SDK designed for Android and iOS. 

Built using **Clean Architecture** and **SOLID principles**, it provides a secure, battery-optimized, and memory-leak-safe abstraction over native Firebase libraries.

---

## Key Features
* **Zero-Dependency `commonMain`**: Avoids Kotlin/Gradle version mismatches or dependency conflicts in client projects.
* **Explicit API Mode**: Ensures clean encapsulation; internal helper implementations and container classes are compiled as `internal` and hidden from client applications.
* **Code Obfuscation**: Ships with pre-configured R8/Proguard `consumer-rules.pro` keeping public entry points while obfuscating all internal structures.
* **Federated Login Support**: Offloads heavy OAuth tasks (Google/Apple login) to native platforms, importing only tokens for secure Firebase Sign-In.
* **Access Token Retrieval**: Easily retrieve or force-refresh JSON Web Tokens (JWT) for secure backend authorization.
* **Battery & Memory Safe**: Uses push-based state observation via lifecycle-aware `Flow`s instead of active polling, and zero context-retention to prevent memory leaks.
* **Sandbox Mode**: Includes an offline-first memory bridge for fast previews, unit testing, and sandbox builds.

---

## 1. Setup Instructions

### Add Dependency
Include the SDK in your client application modules' `build.gradle.kts` dependencies:
```kotlin
dependencies {
    implementation(project(":auth-core"))
}
```

### Firebase Configuration Files
The SDK is configuration-free. Place the standard configuration files in your client host applications:
* **Android**: Place your `google-services.json` inside the root of the app module and apply the `com.google.gms.google-services` plugin.
* **iOS**: Place your `GoogleService-Info.plist` inside your Xcode project bundle.

---

## 2. Initialization

### Android Host App
Initialize the SDK in your Android `Application` class or `MainActivity`:
```kotlin
import com.arkamodh.authsdk.sdk.AuthSdk
import com.arkamodh.authsdk.sdk.initialize

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Safely extracts applicationContext to prevent memory leaks
        AuthSdk.initialize(this)
    }
}
```
*Note: If `google-services.json` is missing during local builds, the SDK automatically falls back to Sandbox Mode to prevent crashes and print a warning.*

---

### iOS Host App (Swift Bridge)
iOS apps resolve Firebase via Apple Swift Package Manager or CocoaPods. To avoid native cinterop conflicts and macOS compilation requirements on Windows, the iOS app implements `NativeAuthBridge` in Swift and registers it on startup.

#### Swift Bridge Implementation Example:
```swift
import Foundation
import FirebaseAuth
import AuthSdk // KMM Framework name

// 1. Wrap Native Auth User
class SwiftAuthUser: NSObject, NativeAuthUser {
    let uid: String
    let email: String?
    let displayName: String?
    let isEmailVerified: Bool
    
    init(user: User) {
        self.uid = user.uid
        self.email = user.email
        self.displayName = user.displayName
        self.isEmailVerified = user.isEmailVerified
    }
}

// 2. Wrap Native Auth Result
class SwiftAuthResult: NSObject, NativeAuthResult {
    let user: NativeAuthUser?
    
    init(user: NativeAuthUser?) {
        self.user = user
    }
}

// 3. Wrap Listener Subscription
class SwiftCancelableSubscription: NSObject, CancelableSubscription {
    let handle: AuthStateDidChangeListenerHandle
    
    init(handle: AuthStateDidChangeListenerHandle) {
        self.handle = handle
    }
    
    func cancel() {
        Auth.auth().removeStateDidChangeListener(handle)
    }
}

// 4. Implement NativeAuthBridge in Swift
class SwiftAuthBridge: NSObject, NativeAuthBridge {
    
    func signIn(email: String, password: String, completion: @escaping (NativeAuthResult?, Error?) -> Void) {
        Auth.auth().signIn(withEmail: email, password: password) { result, error in
            if let error = error {
                completion(nil, error)
            } else if let user = result?.user {
                completion(SwiftAuthResult(user: SwiftAuthUser(user: user)), nil)
            } else {
                completion(nil, NSError(domain: "AuthBridge", code: -1, userInfo: nil))
            }
        }
    }
    
    func signUp(email: String, password: String, completion: @escaping (NativeAuthResult?, Error?) -> Void) {
        Auth.auth().createUser(withEmail: email, password: password) { result, error in
            if let error = error {
                completion(nil, error)
            } else if let user = result?.user {
                completion(SwiftAuthResult(user: SwiftAuthUser(user: user)), nil)
            } else {
                completion(nil, NSError(domain: "AuthBridge", code: -1, userInfo: nil))
            }
        }
    }
    
    func signInWithCredential(provider: AuthProvider, idToken: String, accessToken: String?, completion: @escaping (NativeAuthResult?, Error?) -> Void) {
        var credential: AuthCredential? = nil
        
        if provider == .google {
            credential = GoogleAuthProvider.credential(withIDToken: idToken, accessToken: accessToken ?? "")
        } else if provider == .apple {
            credential = OAuthProvider.credential(withProviderID: "apple.com", idToken: idToken, accessToken: accessToken ?? "")
        }
        
        guard let firebaseCredential = credential else {
            completion(nil, NSError(domain: "AuthBridge", code: -2, userInfo: [NSLocalizedDescriptionKey: "Unsupported Provider"]))
            return
        }
        
        Auth.auth().signIn(with: firebaseCredential) { result, error in
            if let error = error {
                completion(nil, error)
            } else if let user = result?.user {
                completion(SwiftAuthResult(user: SwiftAuthUser(user: user)), nil)
            }
        }
    }
    
    func getIdToken(forceRefresh: Bool, completion: @escaping (String?, Error?) -> Void) {
        guard let currentUser = Auth.auth().currentUser else {
            completion(nil, NSError(domain: "AuthBridge", code: -3, userInfo: [NSLocalizedDescriptionKey: "No active user"]))
            return
        }
        currentUser.getIDTokenForcingRefresh(forceRefresh) { token, error in
            completion(token, error)
        }
    }
    
    func signOut(completion: @escaping (Error?) -> Void) {
        do {
            try Auth.auth().signOut()
            completion(nil)
        } catch {
            completion(error)
        }
    }
    
    func getCurrentUser() -> NativeAuthUser? {
        if let user = Auth.auth().currentUser {
            return SwiftAuthUser(user: user)
        }
        return nil
    }
    
    func observeAuthState(onChanged: @escaping (NativeAuthUser?) -> Void) -> CancelableSubscription {
        let handle = Auth.auth().addStateDidChangeListener { _, user in
            if let user = user {
                onChanged(SwiftAuthUser(user: user))
            } else {
                onChanged(nil)
            }
        }
        return SwiftCancelableSubscription(handle: handle)
    }
    
    func sendPasswordResetEmail(email: String, completion: @escaping (Error?) -> Void) {
        Auth.auth().sendPasswordReset(withEmail: email) { error in
            completion(error)
        }
    }
}
```

#### Register Bridge on App Startup:
In iOS `AppDelegate` or main application initialization block:
```swift
import SwiftUI
import FirebaseCore
import AuthSdk

@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()
        
        // Initialize the KMM AuthCore SDK with the swift bridge
        AuthSdk.companion.initialize(bridge: SwiftAuthBridge())
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

---

## 3. API Usage (Common Code)

Access all operations through the thread-safe `AuthSdk` singleton:

### Email/Password Sign In
```kotlin
val sdk = AuthSdk.getInstance()
try {
    val result = sdk.signIn(email = "user@domain.com", password = "secret_password")
    val uid = result.user?.uid
} catch (e: InvalidCredentialsException) {
    // Handle bad password/email format
} catch (e: UserNotFoundException) {
    // Handle user account missing
} catch (e: NetworkException) {
    // Handle connection timeout
}
```

### Email/Password Registration
```kotlin
try {
    val result = sdk.signUp(email = "newuser@domain.com", password = "securepassword")
    println("Registered new user with UID: ${result.user?.uid}")
} catch (e: UserCollisionException) {
    // Handle email already registered
} catch (e: WeakPasswordException) {
    // Handle weak passwords
}
```

### Sign In with Google / Apple (Federated)
```kotlin
// Retrieve token natively and hand off to SDK
coroutineScope.launch {
    try {
        val result = sdk.signInWithCredential(
            provider = AuthProvider.GOOGLE, 
            idToken = "google-id-token-from-google-signin-sdk"
        )
        println("Signed in user: ${result.user?.displayName}")
    } catch (e: AuthException) {
        // Handle failure
    }
}
```

### Retrieve JWT Access Token (for custom API Headers)
```kotlin
coroutineScope.launch {
    val jwtToken = sdk.getIdToken(forceRefresh = false)
    // Attach header: "Authorization: Bearer $jwtToken"
}
```

### Real-time Authentication State Observation
Observe changes reactively using Kotlin `Flow`s (which map to SwiftUI bindings or Compose states):
```kotlin
// Flow is lifecycle-aware and unregisters platform listeners automatically on cancellation
sdk.observeAuthState().collect { user ->
    if (user != null) {
        println("User is signed in: ${user.email}")
    } else {
        println("User is logged out")
    }
}
```

---

## 4. Sandbox Mode for Preview and Offline Development
If you need to test views, run previews, or test without configuring Firebase:
```kotlin
// Explicitly initialize with Sandbox/Mock bridge
AuthSdk.initialize(SandboxAuthBridge())
```
* **Sandbox Behavior**:
  * Simulates in-memory authentications.
  * Registering or signing in with password `< 6` characters throws `WeakPasswordException`.
  * Passing an email containing the word `"error"` throws `InvalidCredentialsException`.
  * Simulates Google/Apple login and JWT token retrieval.
