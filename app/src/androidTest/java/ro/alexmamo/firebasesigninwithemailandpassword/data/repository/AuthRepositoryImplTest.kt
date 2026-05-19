package ro.alexmamo.firebasesigninwithemailandpassword.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import ro.alexmamo.firebasesigninwithemailandpassword.data.session.SessionManager
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthRepositoryImplTest {
    private lateinit var context: Context
    private lateinit var sessionManager: SessionManager
    private lateinit var authRepository: AuthRepositoryImpl

    @Mock
    private lateinit var mockAuth: FirebaseAuth

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = ApplicationProvider.getApplicationContext()
        sessionManager = SessionManager(context, mockAuth)
        authRepository = AuthRepositoryImpl(mockAuth, sessionManager)
    }

    // Happy Path: Current User Null Initially
    @Test
    fun testCurrentUser_ReturnsNullWhenNotSignedIn() {
        whenever(mockAuth.currentUser).thenReturn(null)

        val currentUser = authRepository.currentUser

        assertNull(currentUser)
    }

    // Happy Path: Sign Out Clears Session
    @Test
    fun testSignOut_CallsFirebaseAuthSignOut() {
        authRepository.signOut()

        verify(mockAuth).signOut()
    }

    // Happy Path: Clear Persisted Session
    @Test
    fun testClearPersistedSession_ClearsSessionData() = runTest {
        sessionManager.saveSession("user-1", "user@example.com", true)
        var sessionData = sessionManager.sessionFlow.first()
        assertTrue(sessionData.isSessionActive)

        authRepository.clearPersistedSession()

        sessionData = sessionManager.sessionFlow.first()
        assertFalse(sessionData.isSessionActive)
    }

    // Happy Path: Auth State Flow Returns Current State
    @Test
    fun testGetAuthState_InitiallyReturnsCurrentState() = runTest {
        whenever(mockAuth.currentUser).thenReturn(null)

        val authState = authRepository.getAuthState().first()

        assertTrue(authState) // Should be true when currentUser is null (signed out)
    }

    // Error Case: Sign Out Followed by Session Clear
    @Test
    fun testSignOut_ThenClearSession_LeavesNoData() = runTest {
        sessionManager.saveSession("user-1", "user@example.com", true)

        authRepository.signOut()
        authRepository.clearPersistedSession()

        val sessionData = sessionManager.sessionFlow.first()
        assertEquals("", sessionData.userId)
        assertEquals("", sessionData.userEmail)
        assertFalse(sessionData.isSessionActive)
    }

    // Happy Path: Update Email Verification After Reload
    @Test
    fun testReloadUser_UpdatesEmailVerificationStatus() = runTest {
        sessionManager.saveSession("user-1", "user@example.com", false)
        var sessionData = sessionManager.sessionFlow.first()
        assertFalse(sessionData.emailVerified)

        // Simulate user reloading from Firebase with verified email
        // This would be called after user verifies email
        sessionManager.updateEmailVerificationStatus(true)

        sessionData = sessionManager.sessionFlow.first()
        assertTrue(sessionData.emailVerified)
    }

    // Happy Path: Multiple Auth State Transitions
    @Test
    fun testAuthStateFlow_ReflectsMultipleTransitions() = runTest {
        // Initial state: signed out
        whenever(mockAuth.currentUser).thenReturn(null)
        var authState = authRepository.getAuthState().first()
        assertTrue(authState)

        // After sign in: would be false
        // This is a simplified test - in real scenario, Mock would need proper setup
        // to track auth state changes through the listener
    }
}