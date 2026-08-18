package com.mr.claudetraining.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {
    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC")
    fun observeAll(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    fun observeById(id: String): Flow<WorkoutSessionEntity?>

    @Query("SELECT * FROM workout_sessions WHERE userId = :userId ORDER BY startTime DESC")
    fun observeByUserId(userId: String): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE syncedToCloud = 0")
    fun observeUnsyncedSessions(): Flow<List<WorkoutSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: WorkoutSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<WorkoutSessionEntity>)

    @Update
    suspend fun update(session: WorkoutSessionEntity)

    @Delete
    suspend fun delete(session: WorkoutSessionEntity)

    @Query("DELETE FROM workout_sessions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE workout_sessions SET syncedToCloud = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("UPDATE workout_sessions SET syncedToCloud = 1 WHERE syncedToCloud = 0")
    suspend fun markAllSynced()

    @Query("DELETE FROM workout_sessions")
    suspend fun deleteAll()
}
