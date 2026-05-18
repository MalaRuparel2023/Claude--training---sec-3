package ro.alexmamo.firebasesigninwithemailandpassword.presentation.sign_in

import android.app.Application
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ro.alexmamo.firebasesigninwithemailandpassword.core.EMPTY_STRING
import ro.alexmamo.firebasesigninwithemailandpassword.core.AuthErrorHandler
import ro.alexmamo.firebasesigninwithemailandpassword.domain.model.Response
import ro.alexmamo.firebasesigninwithemailandpassword.domain.repository.AuthRepository
import javax.inject.Inject

typealias SignInResponse = Response<Unit>

@HiltViewModel
class SignInViewModel @Inject constructor(
    application: Application,
    private val repo: AuthRepository,
    private val googleSignInClient: GoogleSignInClient
): AndroidViewModel(application) {
    private val context = application
    private val _email = MutableStateFlow(TextFieldValue(EMPTY_STRING))
    val email: StateFlow<TextFieldValue> = _email.asStateFlow()

    private val _password = MutableStateFlow(TextFieldValue(EMPTY_STRING))
    val password: StateFlow<TextFieldValue> = _password.asStateFlow()

    private val _signInState = MutableStateFlow<SignInResponse>(Response.Idle)
    val signInState: StateFlow<SignInResponse> = _signInState.asStateFlow()

    fun onEmailChange(newEmail: TextFieldValue) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: TextFieldValue) {
        _password.value = newPassword
    }

    fun signInWithEmailAndPassword(email: String, password: String) = viewModelScope.launch {
        try {
            _signInState.value = Response.Loading
            _signInState.value = Response.Success(repo.signInWithEmailAndPassword(email, password))
        } catch (e: Exception) {
            val errorMessage = AuthErrorHandler.handleAuthException(e, context)
            _signInState.value = Response.Failure(Exception(errorMessage))
        }
    }

    fun getGoogleSignInIntent() = googleSignInClient.signInIntent

    fun signInWithGoogle(idToken: String) = viewModelScope.launch {
        try {
            _signInState.value = Response.Loading
            _signInState.value = Response.Success(repo.signInWithGoogle(idToken))
        } catch (e: Exception) {
            val errorMessage = AuthErrorHandler.handleAuthException(e, context)
            _signInState.value = Response.Failure(Exception(errorMessage))
        }
    }

    val isEmailVerified get() = repo.currentUser?.isEmailVerified == true
}