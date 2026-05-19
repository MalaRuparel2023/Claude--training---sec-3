package ro.alexmamo.firebasesigninwithemailandpassword.data.repository

import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import ro.alexmamo.firebasesigninwithemailandpassword.data.session.SessionManager
import ro.alexmamo.firebasesigninwithemailandpassword.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl constructor(
    private val auth: FirebaseAuth,
    private val sessionManager: SessionManager
) : AuthRepository {
    override val currentUser get() = auth.currentUser

    override suspend fun signUpWithEmailAndPassword(email: String, password: String) {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        result.user?.let { user ->
            sessionManager.saveSession(
                userId = user.uid,
                userEmail = user.email ?: email,
                emailVerified = user.isEmailVerified
            )
        }
    }

    override suspend fun sendEmailVerification() {
        currentUser?.sendEmailVerification()?.await()
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String) {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        result.user?.let { user ->
            sessionManager.saveSession(
                userId = user.uid,
                userEmail = user.email ?: email,
                emailVerified = user.isEmailVerified
            )
        }
    }

    override suspend fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        result.user?.let { user ->
            sessionManager.saveSession(
                userId = user.uid,
                userEmail = user.email ?: "",
                emailVerified = user.isEmailVerified
            )
        }
    }

    override suspend fun deleteUser() {
        currentUser?.delete()?.await()
    }

    override suspend fun reloadUser() {
        currentUser?.reload()?.await()
        currentUser?.let {
            sessionManager.updateEmailVerificationStatus(it.isEmailVerified)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    override fun signOut() {
        auth.signOut()
    }

    override suspend fun clearPersistedSession() {
        sessionManager.clearSession()
    }

    override fun getAuthState() = callbackFlow {
        val authStateListener = AuthStateListener { auth ->
            trySend(auth.currentUser == null)
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun updateUserProfile(displayName: String?, photoUrl: String?) {
        val profileUpdates = userProfileChangeRequest {
            if (displayName != null) this.displayName = displayName
            if (photoUrl != null) this.photoUri = android.net.Uri.parse(photoUrl)
        }
        currentUser?.updateProfile(profileUpdates)?.await()
    }

    override suspend fun updateUserEmail(newEmail: String) {
        currentUser?.updateEmail(newEmail)?.await()
    }

    override suspend fun updateUserPassword(newPassword: String) {
        currentUser?.updatePassword(newPassword)?.await()
    }

    override suspend fun reauthenticateUser(email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(email, password)
        currentUser?.reauthenticate(credential)?.await()
    }

    override suspend fun sendSignInLinkToEmail(email: String) {
        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl("https://firebasesigninwithemailandpassword.page.link")
            .setHandleCodeInApp(true)
            .setIOSBundleId("ro.alexmamo.firebasesigninwithemailandpassword")
            .setAndroidPackageName(
                "ro.alexmamo.firebasesigninwithemailandpassword",
                true,
                null
            )
            .build()
        auth.sendSignInLinkToEmail(email, actionCodeSettings).await()
    }

    override suspend fun signInWithEmailLink(email: String, emailLink: String) {
        auth.signInWithEmailLink(email, emailLink).await()
    }

    override fun isSignInWithEmailLink(link: String): Boolean {
        return auth.isSignInWithEmailLink(link)
    }
}