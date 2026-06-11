package com.mr.claudetraining.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import com.mr.claudetraining.domain.model.Job
import com.mr.claudetraining.domain.model.JobFilter
import com.mr.claudetraining.domain.repository.FilteredJobsRepository
import com.mr.claudetraining.domain.repository.JobRepository
import javax.inject.Inject

/**
 * Decorates [JobRepository.observeJobs] with a [JobFilter]. The static overload is the
 * reactive one fed a single-value flow, so the filtering rule exists once. An empty filter
 * short-circuits to the raw stream to avoid an allocation per emission.
 */
class FilteredJobsRepositoryImpl @Inject constructor(
    private val jobRepository: JobRepository
) : FilteredJobsRepository {

    override fun observeFilteredJobs(filter: JobFilter): Flow<List<Job>> =
        observeFilteredJobs(flowOf(filter))

    override fun observeFilteredJobs(filter: Flow<JobFilter>): Flow<List<Job>> =
        combine(
            jobRepository.observeJobs(),
            filter.distinctUntilChanged()
        ) { jobs, criteria ->
            if (criteria.isEmpty) jobs else jobs.filter(criteria::matches)
        }.distinctUntilChanged()
}