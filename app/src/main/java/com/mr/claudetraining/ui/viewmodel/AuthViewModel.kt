package com.mr.claudetraining.ui.viewmodel

import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.mr.claudetraining.domain.model.AnalyticsEvent
import com.mr.claudetraining.domain.model.Response
import com.mr.claudetraining.domain.repository.AnalyticsLogger
import com.mr.claudetraining.domain.repository.AuthRepository
import javax.inject.Inject

sealed interface AuthGateState {
    data object Loading : AuthGateState
    data object SignedOut : AuthGateState
    data object SignedIn : AuthGateState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val googleSignInClient: GoogleSignInClient,
    private val analytics: AnalyticsLogger
) : ViewModel() {

    private val _gateState = MutableStateFlow<AuthGateState>(AuthGateState.Loading)
    val gateState: StateFlow<AuthGateState> = _gateState.asStateFlow()

    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    private val _formResponse = MutableStateFlow<Response<Unit>>(Response.Idle)
    val formResponse: StateFlow<Response<Unit>> = _formResponse.asStateFlow()

    init {
        viewModelScope.launch {
            // Hold the splash briefly, then follow the live auth state.
            delay(SPLASH_DELAY_MS)
            authRepository.authState().collect { signedIn ->
                _gateState.value = if (signedIn) AuthGateState.SignedIn else AuthGateState.SignedOut
            }
        }
    }

    fun onEmailChange(value: String) {
        email = value
        clearError()
    }

    fun onPasswordChange(value: String) {
        password = value
        clearError()
    }

    fun signIn() = submit(AnalyticsEvent.Login("email")) {
        authRepository.signInWithEmailAndPassword(email.trim(), password)
    }

    fun signUp() = submit(AnalyticsEvent.SignUp("email")) {
        authRepository.signUpWithEmailAndPassword(email.trim(), password)
    }

    fun googleSignInIntent(): Intent = googleSignInClient.signInIntent

    fun signInWithGoogle(idToken: String) =
        runAuth(AnalyticsEvent.Login("google")) { authRepository.signInWithGoogle(idToken) }

    fun onGoogleSignInFailed(message: String) {
        _formResponse.value = Response.Failure(Exception(message))
    }

    fun signOut() {
        authRepository.signOut()
        googleSignInClient.signOut()
        analytics.log(AnalyticsEvent.SignOut)
        analytics.setUserId(null)
    }

    private fun submit(successEvent: AnalyticsEvent, action: suspend () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _formResponse.value = Response.Failure(IllegalArgumentException("Enter your email and password"))
            return
        }
        runAuth(successEvent, action)
    }

    private fun runAuth(successEvent: AnalyticsEvent, action: suspend () -> Unit) {
        viewModelScope.launch {
            _formResponse.value = Response.Loading
            runCatching { action() }
                .onSuccess {
                    analytics.log(successEvent)
                    _formResponse.value = Response.Success(Unit)
                }
                .onFailure { e -> _formResponse.value = Response.Failure(e as? Exception ?: Exception(e.message)) }
        }
    }

    private fun clearError() {
        if (_formResponse.value is Response.Failure) _formResponse.value = Response.Idle
    }

    private companion object {
        const val SPLASH_DELAY_MS = 1300L
    }
}