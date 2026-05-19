package ro.alexmamo.firebasesigninwithemailandpassword.presentation.email_link_signin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ro.alexmamo.firebasesigninwithemailandpassword.core.AuthErrorHandler
import ro.alexmamo.firebasesigninwithemailandpassword.core.isValidEmail
import ro.alexmamo.firebasesigninwithemailandpassword.domain.model.Response
import ro.alexmamo.firebasesigninwithemailandpassword.domain.repository.AuthRepository
import javax.inject.Inject

typealias SendLinkResponse = Response<Unit>
typealias SignInWithLinkResponse = Response<Unit>

@HiltViewModel
class EmailLinkSignInViewModel @Inject constructor(
    application: Application,
    private val repo: AuthRepository
): AndroidViewModel(application) {
    private val context = application

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _sendLinkState = MutableStateFlow<SendLinkResponse>(Response.Idle)
    val sendLinkState: StateFlow<SendLinkResponse> = _sendLinkState.asStateFlow()

    private val _signInWithLinkState = MutableStateFlow<SignInWithLinkResponse>(Response.Idle)
    val signInWithLinkState: StateFlow<SignInWithLinkResponse> = _signInWithLinkState.asStateFlow()

    fun onEmailChange(email: String) {
        _email.value = email
    }

    fun sendSignInLink() = viewModelScope.launch {
        try {
            val email = _email.value
            if (!isValidEmail(email)) {
                _sendLinkState.value = Response.Failure(Exception("Please enter a valid email address"))
                return@launch
            }
            _sendLinkState.value = Response.Loading
            repo.sendSignInLinkToEmail(email)
            _sendLinkState.value = Response.Success(Unit)
        } catch (e: Exception) {
            val errorMessage = AuthErrorHandler.handleAuthException(e, context)
            _sendLinkState.value = Response.Failure(Exception(errorMessage))
        }
    }

    fun signInWithEmailLink(emailLink: String) = viewModelScope.launch {
        try {
            val email = _email.value
            if (!isValidEmail(email)) {
                _signInWithLinkState.value = Response.Failure(Exception("Please enter a valid email address"))
                return@launch
            }
            _signInWithLinkState.value = Response.Loading
            repo.signInWithEmailLink(email, emailLink)
            _signInWithLinkState.value = Response.Success(Unit)
        } catch (e: Exception) {
            val errorMessage = AuthErrorHandler.handleAuthException(e, context)
            _signInWithLinkState.value = Response.Failure(Exception(errorMessage))
        }
    }

    fun isSignInWithEmailLink(link: String): Boolean = repo.isSignInWithEmailLink(link)
}