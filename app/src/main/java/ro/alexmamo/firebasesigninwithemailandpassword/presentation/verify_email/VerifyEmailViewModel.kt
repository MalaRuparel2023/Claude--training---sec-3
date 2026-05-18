package ro.alexmamo.firebasesigninwithemailandpassword.presentation.verify_email

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ro.alexmamo.firebasesigninwithemailandpassword.core.AuthErrorHandler
import ro.alexmamo.firebasesigninwithemailandpassword.domain.model.Response
import ro.alexmamo.firebasesigninwithemailandpassword.domain.repository.AuthRepository
import javax.inject.Inject

typealias ReloadUserResponse = Response<Unit>

@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    application: Application,
    private val repo: AuthRepository
): AndroidViewModel(application) {
    private val context = application
    val isEmailVerified get() = repo.currentUser?.isEmailVerified == true

    private val _reloadUserState = MutableStateFlow<ReloadUserResponse>(Response.Idle)
    val reloadUserState: StateFlow<ReloadUserResponse> = _reloadUserState.asStateFlow()

    private val _isEmailVerifiedState = MutableStateFlow<Boolean>(isEmailVerified)
    val isEmailVerifiedState: StateFlow<Boolean> = _isEmailVerifiedState.asStateFlow()

    fun reloadUser() = viewModelScope.launch {
        try {
            _reloadUserState.value = Response.Loading
            _reloadUserState.value = Response.Success(repo.reloadUser())
            _isEmailVerifiedState.value = isEmailVerified
        } catch (e: Exception) {
            val errorMessage = AuthErrorHandler.handleAuthException(e, context)
            _reloadUserState.value = Response.Failure(Exception(errorMessage))
        }
    }
}