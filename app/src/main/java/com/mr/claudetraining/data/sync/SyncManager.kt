package com.mr.claudetraining.data.sync

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.mr.claudetraining.data.local.DraftStatus
import com.mr.claudetraining.data.local.MessageDraftDao
import com.mr.claudetraining.data.local.MessageDraftEntity
import com.mr.claudetraining.data.local.toEntity
import com.mr.claudetraining.domain.repository.ChatRepository
import com.mr.claudetraining.domain.repository.WorkoutSessionRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object Syncing : SyncStatus
    data class Error(val message: String) : SyncStatus
}

/**
 * Offline-first coordinator. Room is the source the UI reads from; this keeps it fresh:
 *
 *  - **detect** — watches [NetworkMonitor]; every transition back to online triggers a sync.
 *  - **push** — drains the message-draft outbox first, so locally composed messages land on
 *    the server before the pull, and our own writes come back in the same refresh.
 *  - **pull** — fetches the catalog from Firebase and mirrors it into Room.
 *
 * A [Mutex] serialises runs so an overlapping network event or manual [sync] can't double-pull.
 */
@Singleton
class SyncManager @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val chatRepository: ChatRepository,
    private val draftDao: MessageDraftDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val status: StateFlow<SyncStatus> = _status.asStateFlow()

    /** Call once at app start. Re-syncs whenever connectivity is regained. */
    fun start() {
        scope.launch {
            networkMonitor.isOnline
                .filter { online -> online }
                .collect { sync() }
        }
    }

    /** Enqueue an offline-composed message. Persisted immediately; sent on the next sync. */
    suspend fun enqueueDraft(channelId: String, text: String) {
        draftDao.upsert(
            MessageDraftEntity(
                localId = UUID.randomUUID().toString(),
                channelId = channelId,
                text = text,
                createdAt = System.currentTimeMillis()
            )
        )
        scope.launch { sync() }
    }

    /** Run a full sync now. Safe to call concurrently — runs are serialised. */
    suspend fun sync() = mutex.withLock {
        _status.value = SyncStatus.Syncing
        _status.value = runCatching {
            pushDrafts()
            // Job sync removed - WorkoutSessions handled by WorkoutSessionRepository
        }.fold(
            onSuccess = { SyncStatus.Idle },
            onFailure = { error ->
                Log.e(TAG, "sync failed", error)
                SyncStatus.Error(error.message ?: "Sync failed")
            }
        )
    }

    private suspend fun pushDrafts() {
        draftDao.pending().forEach { draft ->
            draftDao.updateStatus(draft.localId, DraftStatus.SENDING)
            runCatching { chatRepository.sendMessage(draft.channelId, draft.text) }
                .onSuccess {
                    // The real message now arrives via the live channel stream; the
                    // outbox row has served its purpose.
                    draftDao.delete(draft.localId)
                }
                .onFailure { error ->
                    Log.w(TAG, "draft ${draft.localId} failed to send, will retry", error)
                    draftDao.markFailed(draft.localId, error.message)
                }
        }
    }

    private companion object {
        const val TAG = "SyncManager"
    }
}