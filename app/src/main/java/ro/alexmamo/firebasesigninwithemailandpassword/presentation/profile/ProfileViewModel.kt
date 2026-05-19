package ro.alexmamo.firebasesigninwithemailandpassword.presentation.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ro.alexmamo.firebasesigninwithemailandpassword.core.AuthErrorHandler
import ro.alexmamo.firebasesigninwithemailandpassword.core.logErrorMessage
import ro.alexmamo.firebasesigninwithemailandpassword.domain.model.Response
import ro.alexmamo.firebasesigninwithemailandpassword.domain.repository.AuthRepository
import javax.inject.Inject

typealias DeleteUserResponse = Response<Unit>
typealias UpdateProfileResponse = Response<Unit>
typealias UpdateEmailResponse = Response<Unit>
typealias UpdatePasswordResponse = Response<Unit>

@HiltViewModel
class ProfileViewModel @Inject constructor(
    application: Application,
    val repo: AuthRepository
): AndroidViewModel(application) {
    private val context = application
    private val _authState = MutableStateFlow<Boolean>(repo.currentUser == null)
    val authState: StateFlow<Boolean> = _authState.asStateFlow()

    private val _deleteUserState = MutableStateFlow<DeleteUserResponse>(Response.Idle)
    val deleteUserState: StateFlow<DeleteUserResponse> = _deleteUserState.asStateFlow()

    private val _updateProfileState = MutableStateFlow<UpdateProfileResponse>(Response.Idle)
    val updateProfileState: StateFlow<UpdateProfileResponse> = _updateProfileState.asStateFlow()

    private val _updateEmailState = MutableStateFlow<UpdateEmailResponse>(Response.Idle)
    val updateEmailState: StateFlow<UpdateEmailResponse> = _updateEmailState.asStateFlow()

    private val _updatePasswordState = MutableStateFlow<UpdatePasswordResponse>(Response.Idle)
    val updatePasswordState: StateFlow<UpdatePasswordResponse> = _updatePasswordState.asStateFlow()

    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _currentPassword = MutableStateFlow("")
    val currentPassword: StateFlow<String> = _currentPassword.asStateFlow()

    private val _newPassword = MutableStateFlow("")
    val newPassword: StateFlow<String> = _newPassword.asStateFlow()

    private val _newEmail = MutableStateFlow("")
    val newEmail: StateFlow<String> = _newEmail.asStateFlow()

    init {
        getAuthState()
        loadUserData()
    }

    private fun getAuthState() = viewModelScope.launch {
        repo.getAuthState().collect { isUserSignedOut ->
            _authState.value = isUserSignedOut
        }
    }

    private fun loadUserData() {
        _displayName.value = repo.currentUser?.displayName ?: ""
        _email.value = repo.currentUser?.email ?: ""
    }

    fun onDisplayNameChange(name: String) {
        _displayName.value = name
    }

    fun onCurrentPasswordChange(password: String) {
        _currentPassword.value = password
    }

    fun onNewPasswordChange(password: String) {
        _newPassword.value = password
    }

    fun onNewEmailChange(email: String) {
        _newEmail.value = email
    }

    fun updateProfile() = viewModelScope.launch {
        try {
            _updateProfileState.value = Response.Loading
            val displayName = _displayName.value.ifBlank { null }
            repo.updateUserProfile(displayName = displayName, photoUrl = null)
            repo.reloadUser()
            _updateProfileState.value = Response.Success(Unit)
        } catch (e: Exception) {
            val errorMessage = AuthErrorHandler.handleAuthException(e, context)
            _updateProfileState.value = Response.Failure(Exception(errorMessage))
        }
    }

    fun updateEmail() = viewModelScope.launch {
        try {
            _updateEmailState.value = Response.Loading
            val newEmail = _newEmail.value
            if (newEmail.isBlank()) {
                _updateEmailState.value = Response.Failure(Exception("Email cannot be empty"))
                return@launch
            }
            repo.reauthenticateUser(_email.value, _currentPassword.value)
            repo.updateUserEmail(newEmail)
            repo.reloadUser()
            _email.value = newEmail
            _newEmail.value = ""
            _currentPassword.value = ""
            _updateEmailState.value = Response.Success(Unit)
        } catch (e: Exception) {
            val errorMessage = AuthErrorHandler.handleAuthException(e, context)
            _updateEmailState.value = Response.Failure(Exception(errorMessage))
        }
    }

    fun updatePassword() = viewModelScope.launch {
        try {
            _updatePasswordState.value = Response.Loading
            val newPassword = _newPassword.value
            if (newPassword.isBlank()) {
                _updatePasswordState.value = Response.Failure(Exception("Password cannot be empty"))
                return@launch
            }
            repo.reauthenticateUser(_email.value, _currentPassword.value)
            repo.updateUserPassword(newPassword)
            _currentPassword.value = ""
            _newPassword.value = ""
            _updatePasswordState.value = Response.Success(Unit)
        } catch (e: Exception) {
            val errorMessage = AuthErrorHandler.handleAuthException(e, context)
            _updatePasswordState.value = Response.Failure(Exception(errorMessage))
        }
    }

    fun signOut() = viewModelScope.launch {
        try {
            repo.signOut()
            repo.clearPersistedSession()
        } catch (e: Exception) {
            logErrorMessage("Sign out failed: ${e.message}")
        }
    }

    fun deleteUser() = viewModelScope.launch {
        try {
            _deleteUserState.value = Response.Loading
            _deleteUserState.value = Response.Success(repo.deleteUser())
        } catch (e: Exception) {
            val errorMessage = AuthErrorHandler.handleAuthException(e, context)
            _deleteUserState.value = Response.Failure(Exception(errorMessage))
        }
    }
}