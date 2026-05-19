package ro.alexmamo.firebasesigninwithemailandpassword.data.session

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val SESSION_STORE = "session_store"
private val Context.sessionDataStore by preferencesDataStore(SESSION_STORE)

private object SessionKeys {
    val USER_ID = stringPreferencesKey("user_id")
    val USER_EMAIL = stringPreferencesKey("user_email")
    val EMAIL_VERIFIED = booleanPreferencesKey("email_verified")
    val LAST_SIGN_IN_TIME = stringPreferencesKey("last_sign_in_time")
    val IS_SESSION_ACTIVE = booleanPreferencesKey("is_session_active")
}

data class SessionData(
    val userId: String = "",
    val userEmail: String = "",
    val emailVerified: Boolean = false,
    val lastSignInTime: String = "",
    val isSessionActive: Boolean = false
)

class SessionManager constructor(
    private val context: Context,
    private val auth: FirebaseAuth
) {
    val sessionFlow: Flow<SessionData> = context.sessionDataStore.data.map { prefs ->
        SessionData(
            userId = prefs[SessionKeys.USER_ID] ?: "",
            userEmail = prefs[SessionKeys.USER_EMAIL] ?: "",
            emailVerified = prefs[SessionKeys.EMAIL_VERIFIED] ?: false,
            lastSignInTime = prefs[SessionKeys.LAST_SIGN_IN_TIME] ?: "",
            isSessionActive = prefs[SessionKeys.IS_SESSION_ACTIVE] ?: false
        )
    }

    suspend fun saveSession(
        userId: String,
        userEmail: String,
        emailVerified: Boolean
    ) {
        try {
            context.sessionDataStore.edit { prefs ->
                prefs[SessionKeys.USER_ID] = userId
                prefs[SessionKeys.USER_EMAIL] = userEmail
                prefs[SessionKeys.EMAIL_VERIFIED] = emailVerified
                prefs[SessionKeys.LAST_SIGN_IN_TIME] = System.currentTimeMillis().toString()
                prefs[SessionKeys.IS_SESSION_ACTIVE] = true
            }
        } catch (e: Exception) {
            throw SessionPersistenceException("Failed to save session", e)
        }
    }

    suspend fun updateEmailVerificationStatus(emailVerified: Boolean) {
        try {
            context.sessionDataStore.edit { prefs ->
                prefs[SessionKeys.EMAIL_VERIFIED] = emailVerified
            }
        } catch (e: Exception) {
            throw SessionPersistenceException("Failed to update email verification", e)
        }
    }

    suspend fun clearSession() {
        try {
            context.sessionDataStore.edit { prefs ->
                prefs[SessionKeys.USER_ID] = ""
                prefs[SessionKeys.USER_EMAIL] = ""
                prefs[SessionKeys.EMAIL_VERIFIED] = false
                prefs[SessionKeys.LAST_SIGN_IN_TIME] = ""
                prefs[SessionKeys.IS_SESSION_ACTIVE] = false
            }
        } catch (e: Exception) {
            throw SessionPersistenceException("Failed to clear session", e)
        }
    }

    suspend fun isSessionValid(): Boolean {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                clearSession()
                return false
            }

            currentUser.reload().let {
                true
            }
        } catch (e: Exception) {
            clearSession()
            false
        }
    }
}

class SessionPersistenceException(message: String, cause: Throwable? = null) :
    Exception(message, cause)