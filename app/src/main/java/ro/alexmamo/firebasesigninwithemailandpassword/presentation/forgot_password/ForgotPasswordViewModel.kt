package ro.alexmamo.firebasesigninwithemailandpassword.presentation.forgot_password

import android.app.Application
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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

typealias PasswordResetEmailResponse = Response<Unit>

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    application: Application,
    private val repo: AuthRepository
): AndroidViewModel(application) {
    private val context = application
    private val _email = MutableStateFlow(TextFieldValue(EMPTY_STRING))
    val email: StateFlow<TextFieldValue> = _email.asStateFlow()

    private val _passwordResetEmailState = MutableStateFlow<PasswordResetEmailResponse>(Response.Idle)
    val passwordResetEmailState: StateFlow<PasswordResetEmailResponse> = _passwordResetEmailState.asStateFlow()

    fun onEmailChange(newEmail: TextFieldValue) {
        _email.value = newEmail
    }

    fun sendPasswordResetEmail(email: String) = viewModelScope.launch {
        try {
            _passwordResetEmailState.value = Response.Loading
            _passwordResetEmailState.value = Response.Success(repo.sendPasswordResetEmail(email))
        } catch (e: Exception) {
            val errorMessage = AuthErrorHandler.handleAuthException(e, context)
            _passwordResetEmailState.value = Response.Failure(Exception(errorMessage))
        }
    }
}