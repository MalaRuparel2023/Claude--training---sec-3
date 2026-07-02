package com.mr.claudetraining.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.mr.claudetraining.data.local.JobDao
import com.mr.claudetraining.data.local.toDomain as entityToDomain
import com.mr.claudetraining.data.local.toEntity
import com.mr.claudetraining.data.model.JobDto
import com.mr.claudetraining.data.model.toDomain
import com.mr.claudetraining.domain.model.EnhancedJob
import com.mr.claudetraining.domain.model.Job
import com.mr.claudetraining.domain.repository.JobRepository
import com.mr.claudetraining.domain.repository.PerfTrace
import com.mr.claudetraining.domain.repository.PerformanceTracer
import javax.inject.Inject

private const val TAG = "JobRepository"

class JobRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val jobDao: JobDao,
    private val performance: PerformanceTracer
) : JobRepository {

    private val jobsRef = firestore.collection("jobs")
    private val savedJobsRef = firestore.collection("users")
        .document(DEMO_USER_ID)
        .collection("savedJobs")

    // Offline-first: Room is the single source the UI binds to, so the stream emits cached
    // rows instantly and re-emits on every DB change. In parallel a Firebase listener mirrors
    // each snapshot into Room — the two concerns are merged in one channelFlow, with the DB
    // as the only output. A fatal Firebase error stops the mirror but leaves the cache intact
    // (we never overwrite it with an empty fallback), so the UI keeps serving last-known data.
    override fun observeJobs(): Flow<List<Job>> = channelFlow {
        launch {
            rawJobs().retryTransient()
                .catch { Log.e(TAG, "jobs mirror stopped; serving cache", it) }
                .collect { jobs -> jobDao.replaceAll(jobs.map { it.toEntity(System.currentTimeMillis()) }) }
        }
        jobDao.observeAll().collect { rows -> send(rows.map { it.entityToDomain() }) }
    }.distinctUntilChanged().traceLoad(PerformanceTracer.JOB_LIST_LOAD)

    // Measures subscription → first emitted list (cache or network, whichever lands first).
    private fun <T> Flow<T>.traceLoad(name: String): Flow<T> {
        var trace: PerfTrace? = null
        return onStart { trace = performance.newTrace(name).also { it.start() } }
            .onEach {
                trace?.let { it.stop(); trace = null }
            }
            // Safety net: stop the trace if the flow completes, errors, or is cancelled
            // before any emission — otherwise the span leaks and Firebase drops it.
            .onCompletion { trace?.stop() }
    }

    override fun observeJob(id: String): Flow<Job?> = channelFlow {
        launch {
            rawJob(id).retryTransient()
                .catch { Log.e(TAG, "job $id mirror stopped; serving cache", it) }
                .collect { job -> job?.let { jobDao.upsertAll(listOf(it.toEntity(System.currentTimeMillis()))) } }
        }
        jobDao.observe(id).collect { row -> send(row?.entityToDomain()) }
    }.distinctUntilChanged()

    override suspend fun fetchJobs(): List<Job> =
        jobsRef.orderBy("postedAt", Query.Direction.DESCENDING).get().await()
            .documents.mapNotNull { it.toObject(JobDto::class.java)?.toDomain() }

    override fun observeSavedJobIds(): Flow<Set<String>> =
        rawSavedJobIds().retryTransient().fallbackTo(emptySet())

    // Both inputs are already resilient, so a favorites outage degrades to "nothing
    // favorited" while the catalog keeps streaming — the enhanced list never collapses.
    override fun observeEnhancedJobs(): Flow<List<EnhancedJob>> =
        combine(observeJobs(), observeSavedJobIds()) { jobs, favoriteIds ->
            jobs.map { job -> EnhancedJob(job, isFavorite = job.id in favoriteIds) }
        }.distinctUntilChanged()

    private fun rawJobs(): Flow<List<Job>> = callbackFlow {
        val reg = jobsRef.orderBy("postedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val jobs = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(JobDto::class.java)?.toDomain()
                } ?: emptyList()
                trySend(jobs)
            }
        awaitClose { reg.remove() }
    }

    private fun rawJob(id: String): Flow<Job?> = callbackFlow {
        val reg = jobsRef.document(id).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(JobDto::class.java)?.toDomain())
        }
        awaitClose { reg.remove() }
    }

    private fun rawSavedJobIds(): Flow<Set<String>> = callbackFlow {
        val reg = savedJobsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.documents?.map { it.id }?.toSet() ?: emptySet())
        }
        awaitClose { reg.remove() }
    }

    override suspend fun setSaved(jobId: String, saved: Boolean) {
        val doc = savedJobsRef.document(jobId)
        if (saved) doc.set(mapOf("savedAt" to System.currentTimeMillis())).await()
        else doc.delete().await()
    }

    private companion object {
        const val DEMO_USER_ID = "tutorial-demi"
    }
}