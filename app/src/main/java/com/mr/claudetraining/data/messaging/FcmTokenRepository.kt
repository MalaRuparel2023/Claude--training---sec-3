package com.mr.claudetraining.data.messaging

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import com.mr.claudetraining.domain.repository.CrashReporter
import com.mr.claudetraining.domain.repository.PushTokenRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenRepository @Inject constructor(
    private val messaging: FirebaseMessaging,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val crashReporter: CrashReporter
) : PushTokenRepository {

    override suspend fun currentToken(): String? = try {
        messaging.token.await().also { Log.d(TAG, "FCM token: $it") }
    } catch (e: Exception) {
        crashReporter.recordNonFatal(e)
        null
    }

    override suspend fun registerToken(token: String) {
        val uid = auth.currentUser?.uid ?: return
        try {
            firestore.collection(USERS).document(uid)
                .collection(TOKENS).document(token)
                .set(
                    mapOf(
                        "token" to token,
                        "platform" to "android",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            crashReporter.log("FCM token registered for user $uid")
        } catch (e: Exception) {
            crashReporter.recordNonFatal(e)
        }
    }

    override suspend fun deleteToken() {
        try {
            messaging.deleteToken().await()
        } catch (e: Exception) {
            crashReporter.recordNonFatal(e)
        }
    }

    private companion object {
        const val TAG = "FCM_TOKEN"
        const val USERS = "users"
        const val TOKENS = "fcmTokens"
    }
}
