package ro.alexmamo.firebasesigninwithemailandpassword.core

import android.content.Context
import android.util.Log
import android.util.Patterns
import android.widget.Toast

const val TAG = "AppTag"
const val EMPTY_STRING = ""
const val MIN_PASSWORD_LENGTH = 6

fun logErrorMessage(
    errorMessage: String
) = Log.e(TAG, errorMessage)

fun showToastMessage(
    context: Context,
    message: String
) = Toast.makeText(context, message, Toast.LENGTH_LONG).show()

fun isValidEmail(email: String): Boolean {
    return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun isValidPassword(password: String): Boolean {











    return password.length >= MIN_PASSWORD_LENGTH
}

fun isValidEmailLink(link: String): Boolean {
    return link.isNotBlank() &&
           (link.contains("oobCode=") || link.contains("continueUrl="))
}
