package com.mr.claudetraining.domain.repository

import kotlinx.coroutines.flow.Flow
import com.mr.claudetraining.domain.model.Job
import com.mr.claudetraining.domain.model.JobFilter

/**
 * A read-through decorator over [JobRepository] that applies a [JobFilter] to the job
 * stream. The filter is itself a [Flow], so the result re-computes both when the catalog
 * changes and when the user adjusts criteria — without re-subscribing to the source.
 */
interface FilteredJobsRepository {
    /** Filter with a static criterion. */
    fun observeFilteredJobs(filter: JobFilter): Flow<List<Job>>

    /** Filter with a reactive criterion (e.g. backed by UI filter state). */
    fun observeFilteredJobs(filter: Flow<JobFilter>): Flow<List<Job>>
}