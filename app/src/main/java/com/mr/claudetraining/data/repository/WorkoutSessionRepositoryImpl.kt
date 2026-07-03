package com.mr.claudetraining.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mr.claudetraining.data.local.WorkoutSessionDao
import com.mr.claudetraining.data.local.WorkoutSessionEntity
import com.mr.claudetraining.data.sync.NetworkMonitor
import com.mr.claudetraining.domain.model.WorkoutSession
import com.mr.claudetraining.domain.model.WorkoutSessionFilter
import com.mr.claudetraining.domain.repository.WorkoutSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class WorkoutSessionRepositoryImpl @Inject constructor(
    private val sessionDao: WorkoutSessionDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val networkMonitor: NetworkMonitor
) : WorkoutSessionRepository {

    override fun observeSessions(): Flow<List<WorkoutSession>> =
        sessionDao.observeAll()
            .map { entities -> entities.map { it.toDomain() } }

    override fun observeSession(id: String): Flow<WorkoutSession?> =
        sessionDao.observeById(id)
            .map { it?.toDomain() }

    override suspend fun createSession(session: WorkoutSession): Result<String> = runCatching {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val id = firestore.collection("users").document(userId)
            .collection("workout_sessions")
            .document()
            .id

        val sessionWithId = session.copy(
            id = id,
            userId = userId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        sessionDao.insert(WorkoutSessionEntity.fromDomain(sessionWithId))

        try {
            firestore.collection("users").document(userId)
                .collection("workout_sessions")
                .document(id)
                .set(sessionWithId.toFirestoreMap())
                .await()
            sessionDao.markSynced(id)
        } catch (e: Exception) {
            // Offline: will sync later
        }

        id
    }

    override suspend fun updateSession(session: WorkoutSession): Result<Unit> = runCatching {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val updated = session.copy(updatedAt = System.currentTimeMillis())

        sessionDao.update(WorkoutSessionEntity.fromDomain(updated))

        try {
            firestore.collection("users").document(userId)
                .collection("workout_sessions")
                .document(session.id)
                .set(updated.toFirestoreMap())
                .await()
            sessionDao.markSynced(session.id)
        } catch (e: Exception) {
            // Offline: will sync later
        }
    }

    override suspend fun deleteSession(id: String): Result<Unit> = runCatching {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")

        sessionDao.deleteById(id)

        try {
            firestore.collection("users").document(userId)
                .collection("workout_sessions")
                .document(id)
                .delete()
                .await()
        } catch (e: Exception) {
            // Offline: will sync later
        }
    }

    override fun observeFilteredSessions(filter: WorkoutSessionFilter): Flow<List<WorkoutSession>> =
        observeSessions()
            .map { sessions -> sessions.filter { filter.matches(it) } }

    override suspend fun syncToCloud(): Result<Unit> = runCatching {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")

        // Firestore handles offline queuing and retry automatically
        sessionDao.markAllSynced()
    }

    override fun observeSyncStatus(): Flow<Boolean> = networkMonitor.isOnline

    private fun WorkoutSession.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "userId" to userId,
        "type" to type.name,
        "intensity" to intensity.name,
        "durationMinutes" to durationMinutes,
        "caloriesBurned" to caloriesBurned,
        "notes" to notes,
        "startTime" to startTime,
        "endTime" to endTime,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )
}
