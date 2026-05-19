package ro.alexmamo.firebasesigninwithemailandpassword.data.session

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
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SessionManagerTest {
    private lateinit var context: Context
    private lateinit var sessionManager: SessionManager

    @Mock
    private lateinit var mockAuth: FirebaseAuth

    @Mock
    private lateinit var mockUser: FirebaseUser

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = ApplicationProvider.getApplicationContext()
        sessionManager = SessionManager(context, mockAuth)
    }

    // Happy Path: Save Session
    @Test
    fun testSaveSession_SavesUserData() = runTest {
        val userId = "test-user-123"
        val userEmail = "test@example.com"
        val emailVerified = true

        sessionManager.saveSession(userId, userEmail, emailVerified)

        val sessionData = sessionManager.sessionFlow.first()
        assertEquals(userId, sessionData.userId)
        assertEquals(userEmail, sessionData.userEmail)
        assertTrue(sessionData.emailVerified)
        assertTrue(sessionData.isSessionActive)
    }

    // Happy Path: Update Email Verification Status
    @Test
    fun testUpdateEmailVerificationStatus_UpdatesVerificationFlag() = runTest {
        sessionManager.saveSession("user-1", "user@example.com", false)

        var sessionData = sessionManager.sessionFlow.first()
        assertFalse(sessionData.emailVerified)

        sessionManager.updateEmailVerificationStatus(true)

        sessionData = sessionManager.sessionFlow.first()
        assertTrue(sessionData.emailVerified)
    }

    // Happy Path: Clear Session
    @Test
    fun testClearSession_ClearsAllData() = runTest {
        sessionManager.saveSession("user-1", "user@example.com", true)

        var sessionData = sessionManager.sessionFlow.first()
        assertTrue(sessionData.isSessionActive)

        sessionManager.clearSession()

        sessionData = sessionManager.sessionFlow.first()
        assertEquals("", sessionData.userId)
        assertEquals("", sessionData.userEmail)
        assertFalse(sessionData.emailVerified)
        assertFalse(sessionData.isSessionActive)
    }

    // Happy Path: Session Survives Process Death
    @Test
    fun testSession_PersistsAfterProcessDeath() = runTest {
        sessionManager.saveSession("user-123", "verify@example.com", false)

        val sessionDataBefore = sessionManager.sessionFlow.first()

        // Recreate SessionManager (simulating app restart)
        val newSessionManager = SessionManager(context, mockAuth)

        val sessionDataAfter = newSessionManager.sessionFlow.first()
        assertEquals(sessionDataBefore.userId, sessionDataAfter.userId)
        assertEquals(sessionDataBefore.userEmail, sessionDataAfter.userEmail)
        assertEquals(sessionDataBefore.emailVerified, sessionDataAfter.emailVerified)
    }

    // Error Case: Invalid Session (Firebase Auth returns null)
    @Test
    fun testIsSessionValid_ReturnsFalseWhenAuthIsNull() = runTest {
        sessionManager.saveSession("user-1", "user@example.com", true)

        whenever(mockAuth.currentUser).thenReturn(null)

        val isValid = sessionManager.isSessionValid()

        assertFalse(isValid)
        val sessionData = sessionManager.sessionFlow.first()
        assertFalse(sessionData.isSessionActive)
    }

    // Error Case: Validation Clears Stale Session
    @Test
    fun testIsSessionValid_ClearsSessionWhenInvalid() = runTest {
        sessionManager.saveSession("user-1", "user@example.com", true)
        var sessionData = sessionManager.sessionFlow.first()
        assertTrue(sessionData.isSessionActive)

        whenever(mockAuth.currentUser).thenReturn(null)

        sessionManager.isSessionValid()

        sessionData = sessionManager.sessionFlow.first()
        assertFalse(sessionData.isSessionActive)
    }

    // Happy Path: Multiple Session Updates
    @Test
    fun testMultipleSessionUpdates_MaintainsIntegrity() = runTest {
        // First save
        sessionManager.saveSession("user-1", "user1@example.com", false)
        var sessionData = sessionManager.sessionFlow.first()
        assertEquals("user-1", sessionData.userId)

        // Update email verification
        sessionManager.updateEmailVerificationStatus(true)
        sessionData = sessionManager.sessionFlow.first()
        assertTrue(sessionData.emailVerified)

        // Clear and save new session (user 2 logs in)
        sessionManager.clearSession()
        sessionManager.saveSession("user-2", "user2@example.com", true)
        sessionData = sessionManager.sessionFlow.first()
        assertEquals("user-2", sessionData.userId)
        assertEquals("user2@example.com", sessionData.userEmail)
    }

    // Happy Path: Session Metadata Recorded
    @Test
    fun testSaveSession_RecordsLastSignInTime() = runTest {
        val beforeTime = System.currentTimeMillis()

        sessionManager.saveSession("user-1", "user@example.com", false)

        val sessionData = sessionManager.sessionFlow.first()
        val lastSignInTime = sessionData.lastSignInTime.toLongOrNull() ?: 0

        assertTrue(lastSignInTime >= beforeTime)
    }
}