package com.mr.claudetraining.domain.repository

import kotlinx.coroutines.flow.Flow
import com.mr.claudetraining.domain.model.EnhancedJob
import com.mr.claudetraining.domain.model.Job

/**
 * Source of truth for the job catalog. Emits the full list as a cold [Flow] that re-emits
 * whenever the backing store changes. Downstream repositories
 * ([FilteredJobsRepository], [SearchRepository]) decorate this single stream rather than
 * re-querying, so there is one observation point per collector.
 */
interface JobRepository {
    fun observeJobs(): Flow<List<Job>>

    /**
     * The job catalog joined with the viewer's favorites into one stream. Re-emits when
     * either source changes — see [JobRepositoryImpl] for the `combine`.
     */
    fun observeEnhancedJobs(): Flow<List<EnhancedJob>>

    fun observeJob(id: String): Flow<Job?>

    /** One-shot pull of the full catalog, used by the sync worker to refresh the Room cache. */
    suspend fun fetchJobs(): List<Job>

    /** Ids of jobs the current user has bookmarked. */
    fun observeSavedJobIds(): Flow<Set<String>>

    suspend fun setSaved(jobId: String, saved: Boolean)
}