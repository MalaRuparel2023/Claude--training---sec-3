package ro.alexmamo.firebasesigninwithemailandpassword.presentation.profile

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import ro.alexmamo.firebasesigninwithemailandpassword.data.repository.AuthRepositoryImpl
import ro.alexmamo.firebasesigninwithemailandpassword.data.session.SessionManager
import ro.alexmamo.firebasesigninwithemailandpassword.domain.repository.AuthRepository
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProfileViewModelTest {
    private lateinit var context: Context
    private lateinit var application: Application
    private lateinit var sessionManager: SessionManager
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: ProfileViewModel

    @Mock
    private lateinit var mockAuth: FirebaseAuth

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = ApplicationProvider.getApplicationContext()
        application = context as Application
        sessionManager = SessionManager(context, mockAuth)
        authRepository = AuthRepositoryImpl(mockAuth, sessionManager)
    }

    // Happy Path: Load User Data Initializes ViewModel
    @Test
    fun testInit_LoadsUserData() = runTest {
        whenever(mockAuth.currentUser).thenReturn(null)

        viewModel = ProfileViewModel(application, authRepository)

        val email = viewModel.email.first()
        assertEquals("", email)
    }

    // Happy Path: Sign Out Clears Session
    @Test
    fun testSignOut_ClearsSessionData() = runTest {
        whenever(mockAuth.currentUser).thenReturn(null)

        // Setup: save a session
        sessionManager.saveSession("user-1", "user@example.com", true)
        var sessionData = sessionManager.sessionFlow.first()
        assertTrue(sessionData.isSessionActive)

        viewModel = ProfileViewModel(application, authRepository)

        // Execute: sign out
        viewModel.signOut()

        // Verify: session is cleared
        sessionData = sessionManager.sessionFlow.first()
        assertFalse(sessionData.isSessionActive)
    }

    // Happy Path: Sign Out Calls Firebase Auth Sign Out
    @Test
    fun testSignOut_CallsAuthSignOut() = runTest {
        whenever(mockAuth.currentUser).thenReturn(null)
        viewModel = ProfileViewModel(application, authRepository)

        viewModel.signOut()

        verify(mockAuth).signOut()
    }

    // Happy Path: Display Name Can Be Updated
    @Test
    fun testOnDisplayNameChange_UpdatesDisplayName() = runTest {
        whenever(mockAuth.currentUser).thenReturn(null)
        viewModel = ProfileViewModel(application, authRepository)

        viewModel.onDisplayNameChange("John Doe")

        val displayName = viewModel.displayName.first()
        assertEquals("John Doe", displayName)
    }

    // Happy Path: Email Change Handler
    @Test
    fun testOnNewEmailChange_UpdatesNewEmail() = runTest {
        whenever(mockAuth.currentUser).thenReturn(null)
        viewModel = ProfileViewModel(application, authRepository)

        viewModel.onNewEmailChange("newemail@example.com")

        val newEmail = viewModel.newEmail.first()
        assertEquals("newemail@example.com", newEmail)
    }

    // Happy Path: Password Change Handler
    @Test
    fun testOnNewPasswordChange_UpdatesNewPassword() = runTest {
        whenever(mockAuth.currentUser).thenReturn(null)
        viewModel = ProfileViewModel(application, authRepository)

        viewModel.onNewPasswordChange("newPassword123!")

        val newPassword = viewModel.newPassword.first()
        assertEquals("newPassword123!", newPassword)
    }

    // Error Case: Sign Out Handles Errors Gracefully
    @Test
    fun testSignOut_HandlesErrorsWithoutCrashing() = runTest {
        whenever(mockAuth.currentUser).thenReturn(null)
        viewModel = ProfileViewModel(application, authRepository)

        // Should not throw even if something fails
        try {
            viewModel.signOut()
        } catch (e: Exception) {
            // Error should be logged, not propagated
        }

        // ViewModel should still be functional
        viewModel.onDisplayNameChange("Test")
        val displayName = viewModel.displayName.first()
        assertEquals("Test", displayName)
    }

    // Happy Path: Auth State Reflects Sign Out
    @Test
    fun testAuthState_ReflectsSignOutState() = runTest {
        whenever(mockAuth.currentUser).thenReturn(null)
        viewModel = ProfileViewModel(application, authRepository)

        // Initial state should reflect auth state
        val initialAuthState = viewModel.authState.first()
        // When currentUser is null, authState should be true (signed out)
        assertTrue(initialAuthState)
    }

    // Happy Path: Multiple Field Updates
    @Test
    fun testMultipleFieldUpdates_MaintainsState() = runTest {
        whenever(mockAuth.currentUser).thenReturn(null)
        viewModel = ProfileViewModel(application, authRepository)

        viewModel.onDisplayNameChange("Alice Smith")
        viewModel.onNewEmailChange("alice@example.com")
        viewModel.onCurrentPasswordChange("currentPassword123")

        assertEquals("Alice Smith", viewModel.displayName.first())
        assertEquals("alice@example.com", viewModel.newEmail.first())
        assertEquals("currentPassword123", viewModel.currentPassword.first())
    }

    // Happy Path: Session Persists Across ViewModel Lifecycle
    @Test
    fun testSessionPersistence_AcrossViewModelLifecycle() = runTest {
        whenever(mockAuth.currentUser).thenReturn(null)

        // Save session before ViewModel creation
        sessionManager.saveSession("user-123", "user@example.com", true)
        var sessionData = sessionManager.sessionFlow.first()
        assertTrue(sessionData.isSessionActive)

        // Create ViewModel
        viewModel = ProfileViewModel(application, authRepository)

        // Session should still be there
        sessionData = sessionManager.sessionFlow.first()
        assertTrue(sessionData.isSessionActive)
        assertEquals("user-123", sessionData.userId)
    }
}