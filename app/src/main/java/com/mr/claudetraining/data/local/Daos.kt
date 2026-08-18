package com.mr.claudetraining.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: String): UserEntity?

    @Query("SELECT * FROM users")
    fun observeAll(): Flow<List<UserEntity>>

    @Upsert
    suspend fun upsertAll(users: List<UserEntity>)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE channelId = :channelId ORDER BY createdAt ASC")
    fun observeByChannel(channelId: String): Flow<List<MessageEntity>>

    @Upsert
    suspend fun upsertAll(messages: List<MessageEntity>)

    @Query("DELETE FROM messages WHERE channelId = :channelId")
    suspend fun clearChannel(channelId: String)
}

@Dao
interface MessageDraftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(draft: MessageDraftEntity)

    @Query("SELECT * FROM message_drafts WHERE channelId = :channelId ORDER BY createdAt ASC")
    fun observeByChannel(channelId: String): Flow<List<MessageDraftEntity>>

    /** Drafts the sync worker should attempt — newly composed or previously failed. */
    @Query("SELECT * FROM message_drafts WHERE status IN ('PENDING', 'FAILED') ORDER BY createdAt ASC")
    suspend fun pending(): List<MessageDraftEntity>

    @Query("UPDATE message_drafts SET status = :status WHERE localId = :id")
    suspend fun updateStatus(id: String, status: DraftStatus)

    @Query("UPDATE message_drafts SET status = 'FAILED', attemptCount = attemptCount + 1, lastError = :error WHERE localId = :id")
    suspend fun markFailed(id: String, error: String?)

    @Query("DELETE FROM message_drafts WHERE localId = :id")
    suspend fun delete(id: String)
}