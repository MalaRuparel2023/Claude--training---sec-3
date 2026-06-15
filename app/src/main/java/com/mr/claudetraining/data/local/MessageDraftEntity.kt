package com.mr.claudetraining.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Lifecycle of an outbound draft as the send-and-sync worker advances it. */
enum class DraftStatus { PENDING, SENDING, SENT, FAILED }

/**
 * An offline-composed message awaiting delivery — the outbox half of offline send.
 *
 * `localId` is a client-generated UUID and the stable primary key, so the row is idempotent:
 * a retry re-sends the same draft instead of duplicating it, and the eventual server message
 * id is recorded in `sentMessageId` for de-dup against the live stream. The worker walks
 * PENDING/FAILED rows oldest-first (hence the `channelId, createdAt` index), flips them to
 * SENDING, and on success marks SENT (or stamps `lastError` + bumps `attemptCount` on
 * failure for backoff).
 */
@Entity(
    tableName = "message_drafts",
    indices = [Index("channelId", "createdAt")]
)
data class MessageDraftEntity(
    @PrimaryKey val localId: String,
    val channelId: String,
    val text: String,
    val createdAt: Long,
    val status: DraftStatus = DraftStatus.PENDING,
    val attemptCount: Int = 0,
    val lastError: String? = null,
    val sentMessageId: String? = null
)