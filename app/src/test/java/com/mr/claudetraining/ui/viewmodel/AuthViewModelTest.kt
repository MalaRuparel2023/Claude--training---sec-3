package com.mr.claudetraining.ui.viewmodel

import com.google.android.gms.auth.api.signin.GoogleSignInClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.mr.claudetraining.domain.model.Response
import com.mr.claudetraining.domain.repository.AnalyticsLogger
import com.mr.claudetraining.domain.repository.AuthRepository

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val authRepository: AuthRepository = mock()
    private val googleSignInClient: GoogleSignInClient = mock()
    private val analytics: AnalyticsLogger = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = AuthViewModel(authRepository, googleSignInClient, analytics)

    // Most tests don't care about the auth-state collection started in init, but the
    // init block always subscribes to authState(); give it a default so construction
    // never NPEs on a null Flow.
    private fun stubAuthState(signedIn: Boolean = false) {
        whenever(authRepository.authState()).thenReturn(flowOf(signedIn))
    }

    // --- Gate state (init / authState observation) ---

    @Test
    fun `initial gate state is Loading before splash delay elapses`() = runTest(dispatcher) {
        stubAuthState(signedIn = false)

        val vm = viewModel()

        // No advance yet: still inside the SPLASH_DELAY_MS window.
        assertEquals(AuthGateState.Loading, vm.gateState.value)
    }

    @Test
    fun `gate state becomes SignedIn when authState emits true`() = runTest(dispatcher) {
        stubAuthState(signedIn = true)

        val vm = viewModel()
        advanceUntilIdle()

        assertEquals(AuthGateState.SignedIn, vm.gateState.value)
    }

    @Test
    fun `gate state becomes SignedOut when authState emits false`() = runTest(dispatcher) {
        stubAuthState(signedIn = false)

        val vm = viewModel()
        advanceUntilIdle()

        assertEquals(AuthGateState.SignedOut, vm.gateState.value)
    }

    @Test
    fun `gate state tracks every authState emission`() = runTest(dispatcher) {
        val upstream = MutableStateFlow(false)
        whenever(authRepository.authState()).thenReturn(upstream)

        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(AuthGateState.SignedOut, vm.gateState.value)

        upstream.value = true
        advanceUntilIdle()
        assertEquals(AuthGateState.SignedIn, vm.gateState.value)

        upstream.value = false
        advanceUntilIdle()
        assertEquals(AuthGateState.SignedOut, vm.gateState.value)
    }

    // --- Initial form state ---

    @Test
    fun `initial form values are empty and response is Idle`() = runTest(dispatcher) {
        stubAuthState()

        val vm = viewModel()

        assertEquals("", vm.email)
        assertEquals("", vm.password)
        assertEquals(Response.Idle, vm.formResponse.value)
    }

    // --- Input handling ---

    @Test
    fun `onEmailChange updates email`() = runTest(dispatcher) {
        stubAuthState()

        val vm = viewModel()
        vm.onEmailChange("user@example.com")

        assertEquals("user@example.com", vm.email)
    }

    @Test
    fun `onPasswordChange updates password`() = runTest(dispatcher) {
        stubAuthState()

        val vm = viewModel()
        vm.onPasswordChange("secret")

        assertEquals("secret", vm.password)
    }

    @Test
    fun `onEmailChange clears a previous failure response`() = runTest(dispatcher) {
        stubAuthState()

        val vm = viewModel()
        // signIn with blank fields produces a Failure.
        vm.signIn()
        advanceUntilIdle()
        assertTrue(vm.formResponse.value is Response.Failure)

        vm.onEmailChange("user@example.com")

        assertEquals(Response.Idle, vm.formResponse.value)
    }

    @Test
    fun `onPasswordChange clears a previous failure response`() = runTest(dispatcher) {
        stubAuthState()

        val vm = viewModel()
        vm.signIn()
        advanceUntilIdle()
        assertTrue(vm.formResponse.value is Response.Failure)

        vm.onPasswordChange("secret")

        assertEquals(Response.Idle, vm.formResponse.value)
    }

    @Test
    fun `onEmailChange does not disturb a non-failure response`() = runTest(dispatcher) {
        stubAuthState()
        whenever(authRepository.signInWithEmailAndPassword(any(), any())).then { }

        val vm = viewModel()
        vm.onEmailChange("user@example.com")
        vm.onPasswordChange("secret")
        vm.signIn()
        advanceUntilIdle()
        assertTrue(vm.formResponse.value is Response.Success)

        vm.onEmailChange("changed@example.com")

        // clearError only resets Failure; a Success must be left untouched.
        assertTrue(vm.formResponse.value is Response.Success)
    }

    // --- signIn ---

    @Test
    fun `signIn with blank email fails with validation error`() = runTest(dispatcher) {
        stubAuthState()

        val vm = viewModel()
        vm.onPasswordChange("secret")
        vm.signIn()
        advanceUntilIdle()

        val response = vm.formResponse.value
        assertTrue(response is Response.Failure)
        assertEquals(
            "Enter your email and password",
            (response as Response.Failure).e?.message
        )
        verify(authRepository, never()).signInWithEmailAndPassword(any(), any())
    }

    @Test
    fun `signIn with blank password fails with validation error`() = runTest(dispatcher) {
        stubAuthState()

        val vm = viewModel()
        vm.onEmailChange("user@example.com")
        vm.signIn()
        advanceUntilIdle()

        val response = vm.formResponse.value
        assertTrue(response is Response.Failure)
        assertEquals(
            "Enter your email and password",
            (response as Response.Failure).e?.message
        )
        verify(authRepository, never()).signInWithEmailAndPassword(any(), any())
    }

    @Test
    fun `signIn success sets Success and trims the email`() = runTest(dispatcher) {
        stubAuthState()
        whenever(authRepository.signInWithEmailAndPassword(any(), any())).then { }

        val vm = viewModel()
        vm.onEmailChange("  user@example.com  ")
        vm.onPasswordChange("secret")
        vm.signIn()
        advanceUntilIdle()

        assertTrue(vm.formResponse.value is Response.Success)
        verify(authRepository).signInWithEmailAndPassword(eq("user@example.com"), eq("secret"))
    }

    @Test
    fun `signIn failure sets Failure with the thrown exception`() = runTest(dispatcher) {
        stubAuthState()
        authRepository.stub {
            onBlocking { signInWithEmailAndPassword(any(), any()) } doThrow
                RuntimeException("bad credentials")
        }

        val vm = viewModel()
        vm.onEmailChange("user@example.com")
        vm.onPasswordChange("secret")
        vm.signIn()
        advanceUntilIdle()

        val response = vm.formResponse.value
        assertTrue(response is Response.Failure)
        assertEquals("bad credentials", (response as Response.Failure).e?.message)
    }

    // --- signUp ---

    @Test
    fun `signUp with blank fields fails with validation error`() = runTest(dispatcher) {
        stubAuthState()

        val vm = viewModel()
        vm.signUp()
        advanceUntilIdle()

        val response = vm.formResponse.value
        assertTrue(response is Response.Failure)
        assertEquals(
            "Enter your email and password",
            (response as Response.Failure).e?.message
        )
        verify(authRepository, never()).signUpWithEmailAndPassword(any(), any())
    }

    @Test
    fun `signUp success sets Success and trims the email`() = runTest(dispatcher) {
        stubAuthState()
        whenever(authRepository.signUpWithEmailAndPassword(any(), any())).then { }

        val vm = viewModel()
        vm.onEmailChange("  new@example.com ")
        vm.onPasswordChange("secret")
        vm.signUp()
        advanceUntilIdle()

        assertTrue(vm.formResponse.value is Response.Success)
        verify(authRepository).signUpWithEmailAndPassword(eq("new@example.com"), eq("secret"))
    }

    @Test
    fun `signUp failure sets Failure with the thrown exception`() = runTest(dispatcher) {
        stubAuthState()
        authRepository.stub {
            onBlocking { signUpWithEmailAndPassword(any(), any()) } doThrow
                RuntimeException("email already in use")
        }

        val vm = viewModel()
        vm.onEmailChange("new@example.com")
        vm.onPasswordChange("secret")
        vm.signUp()
        advanceUntilIdle()

        val response = vm.formResponse.value
        assertTrue(response is Response.Failure)
        assertEquals("email already in use", (response as Response.Failure).e?.message)
    }

    // --- signInWithGoogle (runAuth, bypasses blank validation) ---

    @Test
    fun `signInWithGoogle success sets Success`() = runTest(dispatcher) {
        stubAuthState()
        whenever(authRepository.signInWithGoogle(any())).then { }

        val vm = viewModel()
        vm.signInWithGoogle("id-token")
        advanceUntilIdle()

        assertTrue(vm.formResponse.value is Response.Success)
        verify(authRepository).signInWithGoogle(eq("id-token"))
    }

    @Test
    fun `signInWithGoogle sets Loading before completing`() = runTest(dispatcher) {
        stubAuthState()
        whenever(authRepository.signInWithGoogle(any())).then { }

        val vm = viewModel()
        vm.signInWithGoogle("id-token")
        // runAuth launches a coroutine that first sets Loading; it has not run yet.
        // Advancing the dispatcher lets the coroutine reach the Loading assignment and
        // then suspend on the (instant) repository call before resolving.

        advanceUntilIdle()
        assertTrue(vm.formResponse.value is Response.Success)
    }

    @Test
    fun `signInWithGoogle failure sets Failure with the thrown exception`() = runTest(dispatcher) {
        stubAuthState()
        authRepository.stub {
            onBlocking { signInWithGoogle(any()) } doThrow RuntimeException("token rejected")
        }

        val vm = viewModel()
        vm.signInWithGoogle("id-token")
        advanceUntilIdle()

        val response = vm.formResponse.value
        assertTrue(response is Response.Failure)
        assertEquals("token rejected", (response as Response.Failure).e?.message)
    }

    @Test
    fun `signInWithGoogle failure without a message falls back to a generic exception`() =
        runTest(dispatcher) {
            stubAuthState()
            authRepository.stub {
                onBlocking { signInWithGoogle(any()) } doThrow RuntimeException()
            }

            val vm = viewModel()
            vm.signInWithGoogle("id-token")
            advanceUntilIdle()

            val response = vm.formResponse.value
            assertTrue(response is Response.Failure)
            // RuntimeException() carries a non-null Exception; message is null.
            assertNull((response as Response.Failure).e?.message)
        }

    // --- onGoogleSignInFailed ---

    @Test
    fun `onGoogleSignInFailed sets Failure with the given message`() = runTest(dispatcher) {
        stubAuthState()

        val vm = viewModel()
        vm.onGoogleSignInFailed("user cancelled")

        val response = vm.formResponse.value
        assertTrue(response is Response.Failure)
        assertEquals("user cancelled", (response as Response.Failure).e?.message)
    }

    // --- signOut ---

    @Test
    fun `signOut signs out of repository and google client`() = runTest(dispatcher) {
        stubAuthState()
        // GoogleSignInClient.signOut() returns a Task; the VM ignores it, and Mockito's
        // default stub returns null which is fine since the result is never touched.

        val vm = viewModel()
        vm.signOut()

        verify(authRepository).signOut()
        verify(googleSignInClient).signOut()
    }

    // NOTE: googleSignInIntent() is not unit-tested. It returns the concrete
    // android.content.Intent obtained from GoogleSignInClient.signInIntent. Intent is an
    // Android-framework type with no JVM implementation, so stubbing the getter and
    // asserting on a real Intent would require Robolectric, which is intentionally
    // excluded from this module's test dependencies.
}
