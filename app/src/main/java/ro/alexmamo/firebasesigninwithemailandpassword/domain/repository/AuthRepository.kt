package ro.alexmamo.firebasesigninwithemailandpassword.domain.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: FirebaseUser?

    suspend fun signUpWithEmailAndPassword(email: String, password: String)

    suspend fun sendEmailVerification()

    suspend fun signInWithEmailAndPassword(email: String, password: String)

    suspend fun signInWithGoogle(idToken: String)

    suspend fun sendSignInLinkToEmail(email: String)

    suspend fun signInWithEmailLink(email: String, emailLink: String)

    fun isSignInWithEmailLink(link: String): Boolean

    suspend fun deleteUser()

    suspend fun reloadUser()

    suspend fun sendPasswordResetEmail(email: String)

    suspend fun updateUserProfile(displayName: String?, photoUrl: String?)

    suspend fun updateUserEmail(newEmail: String)

    suspend fun updateUserPassword(newPassword: String)

    suspend fun reauthenticateUser(email: String, password: String)

    fun signOut()

    suspend fun clearPersistedSession()

    fun getAuthState(): Flow<Boolean>
}