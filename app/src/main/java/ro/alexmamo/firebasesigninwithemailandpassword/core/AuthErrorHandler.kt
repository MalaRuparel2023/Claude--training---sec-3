package ro.alexmamo.firebasesigninwithemailandpassword.core

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthException
import android.content.Context
import ro.alexmamo.firebasesigninwithemailandpassword.R

object AuthErrorHandler {
    fun handleAuthException(exception: Exception, context: Context): String {
        return when (exception) {
            is FirebaseAuthWeakPasswordException -> context.getString(R.string.weak_password_message)
            is FirebaseAuthUserCollisionException -> context.getString(R.string.email_already_in_use_message)
            is FirebaseAuthInvalidCredentialsException -> {
                when {
                    exception.errorCode == "ERROR_INVALID_EMAIL" ->
                        context.getString(R.string.invalid_email_format_message)
                    exception.errorCode == "ERROR_USER_DISABLED" ->
                        "This account has been disabled."
                    exception.errorCode == "ERROR_WRONG_PASSWORD" ->
                        context.getString(R.string.wrong_password_message)
                    else -> context.getString(R.string.invalid_email_format_message)
                }
            }
            is FirebaseAuthException -> {
                when (exception.errorCode) {
                    "ERROR_USER_NOT_FOUND" -> context.getString(R.string.user_not_found_message)
                    "ERROR_WRONG_PASSWORD" -> context.getString(R.string.wrong_password_message)
                    "ERROR_NETWORK_REQUEST_FAILED" -> context.getString(R.string.network_error_message)
                    "ERROR_TOO_MANY_REQUESTS" -> context.getString(R.string.too_many_requests_message)
                    "ERROR_INVALID_EMAIL" -> context.getString(R.string.invalid_email_format_message)
                    "ERROR_EMAIL_ALREADY_IN_USE" -> context.getString(R.string.email_already_in_use_message)
                    "ERROR_WEAK_PASSWORD" -> context.getString(R.string.weak_password_message)
                    else -> exception.message ?: context.getString(R.string.generic_auth_error_message)
                }
            }
            else -> exception.message ?: context.getString(R.string.generic_auth_error_message)
        }
    }
}
