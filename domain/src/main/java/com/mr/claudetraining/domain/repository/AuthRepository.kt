package com.mr.claudetraining.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserEmail: String?
    fun isSignedIn(): Boolean

    /** Emits true while a user is signed in, false otherwise. */
    fun authState(): Flow<Boolean>

    suspend fun signInWithEmailAndPassword(email: String, password: String)
    suspend fun signUpWithEmailAndPassword(email: String, password: String)
    suspend fun signInWithGoogle(idToken: String)
    fun signOut()
}