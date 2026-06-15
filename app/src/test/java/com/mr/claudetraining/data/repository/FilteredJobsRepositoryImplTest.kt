package com.mr.claudetraining.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import com.mr.claudetraining.domain.model.Job
import com.mr.claudetraining.domain.model.JobFilter
import com.mr.claudetraining.domain.model.JobType
import com.mr.claudetraining.domain.repository.JobRepository

@OptIn(ExperimentalCoroutinesApi::class)
class FilteredJobsRepositoryImplTest {

    private val jobRepository: JobRepository = mock()

    private val remoteJob = Job(id = "1", title = "Remote Eng", remote = true, type = JobType.FULL_TIME)
    private val onsiteJob = Job(id = "2", title = "Onsite Eng", remote = false, type = JobType.CONTRACT)
    private val catalog = listOf(remoteJob, onsiteJob)

    private fun repo() = FilteredJobsRepositoryImpl(jobRepository)

    @Test
    fun `empty filter passes the raw catalog through unchanged`() = runTest {
        whenever(jobRepository.observeJobs()).thenReturn(flowOf(catalog))

        val result = repo().observeFilteredJobs(JobFilter.None).first()

        assertEquals(catalog, result)
    }

    @Test
    fun `static filter retains only matching jobs`() = runTest {
        whenever(jobRepository.observeJobs()).thenReturn(flowOf(catalog))

        val result = repo().observeFilteredJobs(JobFilter(remoteOnly = true)).first()

        assertEquals(listOf(remoteJob), result)
    }

    @Test
    fun `filter that matches nothing yields empty list`() = runTest {
        whenever(jobRepository.observeJobs()).thenReturn(flowOf(catalog))

        val result = repo().observeFilteredJobs(JobFilter(types = setOf(JobType.INTERNSHIP))).first()

        assertEquals(emptyList<Job>(), result)
    }

    @Test
    fun `empty catalog yields empty list`() = runTest {
        whenever(jobRepository.observeJobs()).thenReturn(flowOf(emptyList()))

        val result = repo().observeFilteredJobs(JobFilter(remoteOnly = true)).first()

        assertEquals(emptyList<Job>(), result)
    }

    @Test
    fun `reactive filter re-applies when criteria change`() = runTest {
        whenever(jobRepository.observeJobs()).thenReturn(flowOf(catalog))
        val filter = MutableStateFlow(JobFilter.None)

        val emissions = mutableListOf<List<Job>>()
        val collector = launchCollect(repo().observeFilteredJobs(filter), emissions)
        advanceUntilIdle()

        filter.value = JobFilter(remoteOnly = true)
        advanceUntilIdle()

        collector.cancel()
        assertEquals(catalog, emissions.first())
        assertEquals(listOf(remoteJob), emissions.last())
    }

    @Test
    fun `identical consecutive results are de-duplicated`() = runTest {
        // Two emissions of the same catalog with a no-op filter should collapse to one result.
        whenever(jobRepository.observeJobs()).thenReturn(flowOf(catalog, catalog))

        val results = repo().observeFilteredJobs(JobFilter.None).take(1).toList()

        assertEquals(1, results.size)
        assertEquals(catalog, results.first())
    }

    private fun <T> CoroutineScope.launchCollect(
        flow: Flow<T>,
        into: MutableList<T>
    ) = launch { flow.collect { into.add(it) } }
}
