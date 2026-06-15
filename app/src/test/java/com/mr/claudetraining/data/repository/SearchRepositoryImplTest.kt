package com.mr.claudetraining.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import com.mr.claudetraining.domain.model.Job
import com.mr.claudetraining.domain.model.JobFilter
import com.mr.claudetraining.domain.model.JobSearchResult
import com.mr.claudetraining.domain.repository.FilteredJobsRepository
import com.mr.claudetraining.domain.repository.JobRepository

@OptIn(ExperimentalCoroutinesApi::class)
class SearchRepositoryImplTest {

    private val jobRepository: JobRepository = mock()
    private val filteredJobsRepository: FilteredJobsRepository = mock()

    private val kotlinJob = Job(
        id = "1", title = "Kotlin Engineer", company = "Acme",
        location = "Berlin", tags = listOf("backend"), postedAt = 100
    )
    private val androidJob = Job(
        id = "2", title = "Mobile Dev", company = "Globex",
        location = "Remote", tags = listOf("kotlin", "android"), postedAt = 200
    )
    private val designerJob = Job(
        id = "3", title = "Designer", company = "Initech",
        location = "London", tags = listOf("figma"), postedAt = 50
    )
    private val catalog = listOf(kotlinJob, androidJob, designerJob)

    private fun repo() = SearchRepositoryImpl(jobRepository, filteredJobsRepository)

    private fun CoroutineScope.collectInto(flow: Flow<JobSearchResult>, into: MutableList<JobSearchResult>) =
        launch { flow.collect { into.add(it) } }

    @Test
    fun `blank query keeps every filtered job with default score`() = runTest {
        whenever(filteredJobsRepository.observeFilteredJobs(any<Flow<JobFilter>>())).thenReturn(flowOf(catalog))
        whenever(jobRepository.observeSavedJobIds()).thenReturn(flowOf(emptySet()))

        val results = mutableListOf<JobSearchResult>()
        val c = collectInto(repo().search(flowOf(""), flowOf(JobFilter.None)), results)
        advanceUntilIdle()
        c.cancel()

        val latest = results.last()
        assertEquals("", latest.query)
        assertEquals(3, latest.totalCount)
    }

    @Test
    fun `query ranks title hits above tag and location hits`() = runTest {
        whenever(filteredJobsRepository.observeFilteredJobs(any<Flow<JobFilter>>())).thenReturn(flowOf(catalog))
        whenever(jobRepository.observeSavedJobIds()).thenReturn(flowOf(emptySet()))

        val results = mutableListOf<JobSearchResult>()
        val c = collectInto(repo().search(flowOf("kotlin"), flowOf(JobFilter.None)), results)
        advanceUntilIdle()
        c.cancel()

        val latest = results.last()
        // designerJob has no "kotlin" match -> filtered out.
        assertEquals(2, latest.totalCount)
        // kotlinJob: title match = 5; androidJob: tag match = 2 -> title hit ranks first.
        assertEquals("1", latest.items[0].job.id)
        assertEquals(5, latest.items[0].score)
        assertEquals("2", latest.items[1].job.id)
        assertEquals(2, latest.items[1].score)
    }

    @Test
    fun `non-matching query yields empty result`() = runTest {
        whenever(filteredJobsRepository.observeFilteredJobs(any<Flow<JobFilter>>())).thenReturn(flowOf(catalog))
        whenever(jobRepository.observeSavedJobIds()).thenReturn(flowOf(emptySet()))

        val results = mutableListOf<JobSearchResult>()
        val c = collectInto(repo().search(flowOf("zzz-nomatch"), flowOf(JobFilter.None)), results)
        advanceUntilIdle()
        c.cancel()

        assertTrue(results.last().isEmpty)
    }

    @Test
    fun `saved ids are reflected on matching items`() = runTest {
        whenever(filteredJobsRepository.observeFilteredJobs(any<Flow<JobFilter>>())).thenReturn(flowOf(catalog))
        whenever(jobRepository.observeSavedJobIds()).thenReturn(flowOf(setOf("1")))

        val results = mutableListOf<JobSearchResult>()
        val c = collectInto(repo().search(flowOf(""), flowOf(JobFilter.None)), results)
        advanceUntilIdle()
        c.cancel()

        val latest = results.last()
        val savedItem = latest.items.first { it.job.id == "1" }
        val unsavedItem = latest.items.first { it.job.id == "2" }
        assertTrue(savedItem.saved)
        assertFalse(unsavedItem.saved)
    }

    @Test
    fun `multi-term query sums per-term scores`() = runTest {
        whenever(filteredJobsRepository.observeFilteredJobs(any<Flow<JobFilter>>())).thenReturn(flowOf(listOf(androidJob)))
        whenever(jobRepository.observeSavedJobIds()).thenReturn(flowOf(emptySet()))

        val results = mutableListOf<JobSearchResult>()
        // "mobile" -> title match (5); "kotlin" -> tag match (2) => 7
        val c = collectInto(repo().search(flowOf("mobile kotlin"), flowOf(JobFilter.None)), results)
        advanceUntilIdle()
        c.cancel()

        assertEquals(7, results.last().items.single().score)
    }

    @Test
    fun `query is trimmed before scoring and surfaced in result`() = runTest {
        whenever(filteredJobsRepository.observeFilteredJobs(any<Flow<JobFilter>>())).thenReturn(flowOf(catalog))
        whenever(jobRepository.observeSavedJobIds()).thenReturn(flowOf(emptySet()))

        val results = mutableListOf<JobSearchResult>()
        val c = collectInto(repo().search(flowOf("  kotlin  "), flowOf(JobFilter.None)), results)
        advanceUntilIdle()
        c.cancel()

        assertEquals("kotlin", results.last().query)
    }

    @Test
    fun `debounce collapses rapid query changes to the final value`() = runTest {
        whenever(filteredJobsRepository.observeFilteredJobs(any<Flow<JobFilter>>())).thenReturn(flowOf(catalog))
        whenever(jobRepository.observeSavedJobIds()).thenReturn(flowOf(emptySet()))

        val query = MutableStateFlow("")
        val results = mutableListOf<JobSearchResult>()
        val c = collectInto(repo().search(query, flowOf(JobFilter.None)), results)
        advanceUntilIdle()

        // rapid changes within debounce window
        query.value = "k"
        query.value = "ko"
        query.value = "kotlin"
        advanceUntilIdle()
        c.cancel()

        // Only the settled query "kotlin" produces ranked results.
        assertEquals("kotlin", results.last().query)
        assertEquals(2, results.last().totalCount)
    }
}
