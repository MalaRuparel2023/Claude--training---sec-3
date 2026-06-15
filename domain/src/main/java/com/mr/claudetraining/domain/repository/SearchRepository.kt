package com.mr.claudetraining.domain.repository

import kotlinx.coroutines.flow.Flow
import com.mr.claudetraining.domain.model.JobFilter
import com.mr.claudetraining.domain.model.JobSearchResult

/**
 * Combines several independent streams into one search result:
 *  - the live query text,
 *  - the active [JobFilter],
 *  - the filtered job catalog (via [FilteredJobsRepository]),
 *  - the user's saved-job ids (via [JobRepository]).
 *
 * Any upstream change re-emits a fresh [JobSearchResult] with relevance-ranked,
 * saved-flagged items.
 */
interface SearchRepository {
    fun search(query: Flow<String>, filter: Flow<JobFilter>): Flow<JobSearchResult>
}