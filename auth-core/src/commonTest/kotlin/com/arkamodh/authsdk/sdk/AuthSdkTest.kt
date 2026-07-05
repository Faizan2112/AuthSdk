package com.arkamodh.authsdk.sdk

import com.arkamodh.authsdk.sdk.bridge.*
import com.arkamodh.authsdk.sdk.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class MockAuthUser(
    override val uid: String,
    override val email: String?,
    override val displayName: String?,
    override val isEmailVerified: Boolean
) : NativeAuthUser

class MockAuthResult(
    override val user: NativeAuthUser?
) : NativeAuthResult

class MockCancelableSubscription(private val onCancel: () -> Unit) : CancelableSubscription {
    override fun cancel() {
        onCancel()
    }
}

class MockAuthBridge : NativeAuthBridge {
    var currentUser: MockAuthUser? = null
    var shouldFail: Boolean = false
    var errorToThrow: Throwable = InvalidCredentialsException("Mock invalid credentials")
    var idTokenToReturn: String? = "mock-jwt-token"
    val listeners = mutableListOf<(NativeAuthUser?) -> Unit>()

    override fun signIn(
        email: String,
        password: String,
        completion: (NativeAuthResult?, Throwable?) -> Unit
    ) {
        if (shouldFail) {
            completion(null, errorToThrow)
        } else {
            currentUser = MockAuthUser("123", email, "Mock User", true)
            notifyListeners()
            completion(MockAuthResult(currentUser), null)
        }
    }

    override fun signUp(
        email: String,
        password: String,
        completion: (NativeAuthResult?, Throwable?) -> Unit
    ) {
        if (shouldFail) {
            completion(null, errorToThrow)
        } else {
            currentUser = MockAuthUser("123", email, "Mock User", false)
            notifyListeners()
            completion(MockAuthResult(currentUser), null)
        }
    }

    override fun signInWithCredential(
        provider: AuthProvider,
        idToken: String,
        accessToken: String?,
        completion: (NativeAuthResult?, Throwable?) -> Unit
    ) {
        if (shouldFail) {
            completion(null, errorToThrow)
        } else {
            currentUser = MockAuthUser("google-123", "google@mock.com", "Google User", true)
            notifyListeners()
            completion(MockAuthResult(currentUser), null)
        }
    }

    override fun getIdToken(
        forceRefresh: Boolean,
        completion: (String?, Throwable?) -> Unit
    ) {
        if (shouldFail) {
            completion(null, errorToThrow)
        } else {
            completion(idTokenToReturn, null)
        }
    }

    override fun signOut(completion: (Throwable?) -> Unit) {
        if (shouldFail) {
            completion(errorToThrow)
        } else {
            currentUser = null
            notifyListeners()
            completion(null)
        }
    }

    override fun getCurrentUser(): NativeAuthUser? = currentUser

    override fun observeAuthState(onChanged: (NativeAuthUser?) -> Unit): CancelableSubscription {
        listeners.add(onChanged)
        onChanged(currentUser)
        return MockCancelableSubscription {
            listeners.remove(onChanged)
        }
    }

    override fun sendPasswordResetEmail(email: String, completion: (Throwable?) -> Unit) {
        if (shouldFail) {
            completion(errorToThrow)
        } else {
            completion(null)
        }
    }

    fun notifyListeners() {
        listeners.forEach { it(currentUser) }
    }
}

class AuthSdkTest {

    private lateinit var mockBridge: MockAuthBridge

    @BeforeTest
    fun setUp() {
        AuthSdk.reset()
        mockBridge = MockAuthBridge()
        AuthSdk.initialize(mockBridge)
    }

    @Test
    fun testInitialization() {
        val sdk = AuthSdk.getInstance()
        assertNotNull(sdk)
    }

    @Test
    fun testSignInSuccess() = runTest {
        val sdk = AuthSdk.getInstance()
        val result = sdk.signIn("test@example.com", "password")
        
        assertNotNull(result.user)
        assertEquals("123", result.user.uid)
        assertEquals("test@example.com", result.user.email)
        assertTrue(result.user.isEmailVerified)
    }

    @Test
    fun testSignInFailure() = runTest {
        val sdk = AuthSdk.getInstance()
        mockBridge.shouldFail = true
        
        assertFailsWith<InvalidCredentialsException> {
            sdk.signIn("wrong@example.com", "badpass")
        }
    }

    @Test
    fun testSignUpSuccess() = runTest {
        val sdk = AuthSdk.getInstance()
        val result = sdk.signUp("new@example.com", "password")
        
        assertNotNull(result.user)
        assertEquals("123", result.user.uid)
        assertEquals("new@example.com", result.user.email)
        assertFalse(result.user.isEmailVerified) // SignUp defaults isEmailVerified to false in mock
    }

    @Test
    fun testSignOut() = runTest {
        val sdk = AuthSdk.getInstance()
        
        // Sign in first
        sdk.signIn("test@example.com", "password")
        assertNotNull(sdk.getCurrentUser())
        
        // Sign out
        sdk.signOut()
        assertNull(sdk.getCurrentUser())
    }

    @Test
    fun testObserveAuthStateFlow() = runTest {
        val sdk = AuthSdk.getInstance()
        val authStates = mutableListOf<AuthUser?>()
        
        val job = launch {
            sdk.observeAuthState().collect { user ->
                authStates.add(user)
            }
        }
        
        // Let the collector start and receive initial state
        testScheduler.runCurrent()
        
        // Initial state should be null (first element is null)
        assertEquals(1, authStates.size)
        assertNull(authStates[0])
        
        // Trigger signIn
        sdk.signIn("test@example.com", "password")
        
        // Let the collector receive the signed-in state
        testScheduler.runCurrent()
        
        assertEquals(2, authStates.size)
        assertNotNull(authStates[1])
        assertEquals("test@example.com", authStates[1]?.email)
        
        // Trigger signOut
        sdk.signOut()
        
        // Let the collector receive the signed-out state
        testScheduler.runCurrent()
        
        assertEquals(3, authStates.size)
        assertNull(authStates[2])
        
        job.cancel()
    }

    @Test
    fun testSignInWithCredential() = runTest {
        val sdk = AuthSdk.getInstance()
        val result = sdk.signInWithCredential(AuthProvider.GOOGLE, "id-token", "access-token")
        
        assertNotNull(result.user)
        assertEquals("google-123", result.user.uid)
        assertEquals("google@mock.com", result.user.email)
    }

    @Test
    fun testGetIdToken() = runTest {
        val sdk = AuthSdk.getInstance()
        val token = sdk.getIdToken(forceRefresh = false)
        
        assertEquals("mock-jwt-token", token)
    }

    @Test
    fun testSendPasswordResetEmail() = runTest {
        val sdk = AuthSdk.getInstance()
        // Should compile and run without exceptions
        sdk.resetPassword("test@example.com")
    }
}
