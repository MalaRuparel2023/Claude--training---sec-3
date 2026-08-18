package com.mr.claudetraining.domain.repository

import kotlinx.coroutines.flow.Flow
import com.mr.claudetraining.domain.model.WorkoutSession
import com.mr.claudetraining.domain.model.WorkoutSessionFilter

interface WorkoutSessionRepository {
    fun observeSessions(): Flow<List<WorkoutSession>>

    fun observeSession(id: String): Flow<WorkoutSession?>

    suspend fun createSession(session: WorkoutSession): Result<String>

    suspend fun updateSession(session: WorkoutSession): Result<Unit>

    suspend fun deleteSession(id: String): Result<Unit>

    fun observeFilteredSessions(filter: WorkoutSessionFilter): Flow<List<WorkoutSession>>

    suspend fun syncToCloud(): Result<Unit>

    fun observeSyncStatus(): Flow<Boolean>
}
