package com.mr.claudetraining.domain.repository

/**
 * Handles the FCM device registration token: fetching the current token and
 * registering it with the backend so the server can target this device.
 * Implementations are backed by Firebase Cloud Messaging + Firestore.
 */
interface PushTokenRepository {

    /** The current FCM registration token, or null if it can't be retrieved. */
    suspend fun currentToken(): String?

    /**
     * Persist [token] for the signed-in user so the backend can push to this
     * device. No-op when no user is signed in.
     */
    suspend fun registerToken(token: String)

    /** Drop the local FCM token (call on sign-out so a signed-out device stops receiving pushes). */
    suspend fun deleteToken()
}
