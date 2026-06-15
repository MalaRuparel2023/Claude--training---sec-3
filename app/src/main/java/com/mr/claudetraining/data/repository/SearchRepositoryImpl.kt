package com.mr.claudetraining.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import com.mr.claudetraining.domain.model.Job
import com.mr.claudetraining.domain.model.JobFilter
import com.mr.claudetraining.domain.model.JobSearchItem
import com.mr.claudetraining.domain.model.JobSearchResult
import com.mr.claudetraining.domain.repository.FilteredJobsRepository
import com.mr.claudetraining.domain.repository.JobRepository
import com.mr.claudetraining.domain.repository.SearchRepository
import javax.inject.Inject

/**
 * Fan-in of four streams: the debounced query, the active filter, the filtered catalog, and
 * the saved-id set. The filter feeds [FilteredJobsRepository] (so filtering happens before
 * scoring), while the query drives relevance ranking here. Every upstream change re-emits a
 * ranked [JobSearchResult]; identical results are de-duplicated.
 */
class SearchRepositoryImpl @Inject constructor(
    private val jobRepository: JobRepository,
    private val filteredJobsRepository: FilteredJobsRepository
) : SearchRepository {

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun search(query: Flow<String>, filter: Flow<JobFilter>): Flow<JobSearchResult> {
        val debouncedQuery = query
            .debounce(QUERY_DEBOUNCE_MS)
            .map { it.trim() }
            .distinctUntilChanged()

        return combine(
            debouncedQuery,
            filteredJobsRepository.observeFilteredJobs(filter),
            jobRepository.observeSavedJobIds()
        ) { q, jobs, savedIds ->
            val items = jobs
                .map { job -> JobSearchItem(job, score(job, q), job.id in savedIds) }
                .filter { q.isBlank() || it.score > 0 }
                .sortedWith(compareByDescending<JobSearchItem> { it.score }.thenByDescending { it.job.postedAt })
            JobSearchResult(query = q, items = items)
        }.distinctUntilChanged()
    }

    /** Weighted keyword match across the fields a candidate is most likely to search by. */
    private fun score(job: Job, query: String): Int {
        if (query.isBlank()) return 1
        val terms = query.split(WHITESPACE).filter { it.isNotBlank() }
        return terms.sumOf { term: String ->
            val termScore: Int = when {
                job.title.contains(term, ignoreCase = true) -> 5
                job.company.contains(term, ignoreCase = true) -> 3
                job.tags.any { it.contains(term, ignoreCase = true) } -> 2
                job.location.contains(term, ignoreCase = true) -> 1
                else -> 0
            }
            termScore
        }
    }

    private companion object {
        const val QUERY_DEBOUNCE_MS = 300L
        val WHITESPACE = "\\s+".toRegex()
    }
}