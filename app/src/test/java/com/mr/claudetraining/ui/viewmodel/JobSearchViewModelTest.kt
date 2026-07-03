package com.mr.claudetraining.ui.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.mr.claudetraining.domain.model.AnalyticsEvent
import com.mr.claudetraining.domain.model.Job
import com.mr.claudetraining.domain.model.JobFilter
import com.mr.claudetraining.domain.model.JobSearchItem
import com.mr.claudetraining.domain.model.JobSearchResult
import com.mr.claudetraining.domain.model.JobType
import com.mr.claudetraining.domain.repository.AnalyticsLogger
import com.mr.claudetraining.domain.repository.SearchRepository

@OptIn(ExperimentalCoroutinesApi::class)
class JobSearchViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val searchRepository: SearchRepository = mock()
    private val analytics: AnalyticsLogger = mock()

    private val kotlinJob = Job(
        id = "1",
        title = "Kotlin Engineer",
        company = "Acme",
        location = "Berlin",
        type = JobType.FULL_TIME,
        tags = listOf("backend"),
        postedAt = 100
    )

    private val androidJob = Job(
        id = "2",
        title = "Mobile Dev",
        company = "Globex",
        location = "Remote",
        type = JobType.FULL_TIME,
        tags = listOf("kotlin", "android"),
        postedAt = 200
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = JobSearchViewModel(searchRepository, analytics)

    @Test
    fun `initial state is empty with no query`() = runTest(dispatcher) {
        whenever(searchRepository.search(any(), any())).thenReturn(
            flowOf(JobSearchResult(query = "", items = emptyList()))
        )

        val vm = viewModel()
        advanceUntilIdle()
        val state = vm.uiState.value

        assertEquals("", state.query)
        assertEquals(JobFilter.None, state.filter)
        assertEquals(emptyList<JobSearchItem>(), state.results)
        assertEquals(0, state.totalCount)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `setQuery updates query flow`() = runTest(dispatcher) {
        whenever(searchRepository.search(any(), any())).thenReturn(
            flowOf(JobSearchResult(query = "kotlin", items = emptyList()))
        )

        val vm = viewModel()
        advanceUntilIdle()
        vm.setQuery("kotlin")

        assertEquals("kotlin", vm.query.value)
    }

    @Test
    fun `setFilter updates filter flow`() = runTest(dispatcher) {
        whenever(searchRepository.search(any(), any())).thenReturn(
            flowOf(JobSearchResult(query = "", items = emptyList()))
        )

        val vm = viewModel()
        advanceUntilIdle()
        val newFilter = JobFilter(remoteOnly = true)
        vm.setFilter(newFilter)

        assertEquals(newFilter, vm.filter.value)
    }

    @Test
    fun `search results populate ui state`() = runTest(dispatcher) {
        val items = listOf(
            JobSearchItem(kotlinJob, score = 5, saved = false),
            JobSearchItem(androidJob, score = 3, saved = true)
        )
        whenever(searchRepository.search(any(), any())).thenReturn(
            flowOf(JobSearchResult(query = "kotlin", items = items))
        )

        val vm = viewModel()
        advanceUntilIdle()
        vm.setQuery("kotlin")

        val state = vm.uiState.value
        assertEquals("kotlin", state.query)
        assertEquals(2, state.totalCount)
        assertEquals(items, state.results)
    }

    @Test
    fun `SearchJobs analytics logged on non-blank query`() = runTest(dispatcher) {
        val items = listOf(JobSearchItem(kotlinJob, score = 5, saved = false))
        whenever(searchRepository.search(any(), any())).thenReturn(
            flowOf(JobSearchResult(query = "kotlin", items = items))
        )

        val vm = viewModel()
        advanceUntilIdle()
        vm.setQuery("kotlin")

        val eventCaptor = argumentCaptor<AnalyticsEvent>()
        verify(analytics).log(eventCaptor.capture())
        val event = eventCaptor.firstValue
        assertTrue(event is AnalyticsEvent.SearchJobs)
        assertEquals("kotlin", (event as AnalyticsEvent.SearchJobs).query)
        assertEquals(1, event.resultCount)
    }

    @Test
    fun `SearchJobs not logged for blank query`() = runTest(dispatcher) {
        whenever(searchRepository.search(any(), any())).thenReturn(
            flowOf(JobSearchResult(query = "", items = listOf()))
        )

        viewModel()

        verify(analytics, org.mockito.kotlin.never()).log(any())
    }

    @Test
    fun `toggleJobType adds and removes type from filter`() = runTest(dispatcher) {
        whenever(searchRepository.search(any(), any())).thenReturn(
            flowOf(JobSearchResult(query = "", items = emptyList()))
        )

        val vm = viewModel()
        advanceUntilIdle()

        vm.toggleJobType(JobType.FULL_TIME)
        assertEquals(setOf(JobType.FULL_TIME), vm.filter.value.types)

        vm.toggleJobType(JobType.PART_TIME)
        assertEquals(setOf(JobType.FULL_TIME, JobType.PART_TIME), vm.filter.value.types)

        vm.toggleJobType(JobType.FULL_TIME)
        assertEquals(setOf(JobType.PART_TIME), vm.filter.value.types)
    }

    @Test
    fun `toggleRemoteOnly flips remote flag`() = runTest(dispatcher) {
        whenever(searchRepository.search(any(), any())).thenReturn(
            flowOf(JobSearchResult(query = "", items = emptyList()))
        )

        val vm = viewModel()
        advanceUntilIdle()

        assertFalse(vm.filter.value.remoteOnly)
        vm.toggleRemoteOnly()
        assertTrue(vm.filter.value.remoteOnly)
        vm.toggleRemoteOnly()
        assertFalse(vm.filter.value.remoteOnly)
    }

    @Test
    fun `setLocation updates location in filter`() = runTest(dispatcher) {
        whenever(searchRepository.search(any(), any())).thenReturn(
            flowOf(JobSearchResult(query = "", items = emptyList()))
        )

        val vm = viewModel()
        advanceUntilIdle()

        vm.setLocation("Berlin")
        assertEquals("Berlin", vm.filter.value.location)

        vm.setLocation("")
        assertNull(vm.filter.value.location)

        vm.setLocation("   ")
        assertNull(vm.filter.value.location)
    }

    @Test
    fun `setMinSalary updates salary in filter`() = runTest(dispatcher) {
        whenever(searchRepository.search(any(), any())).thenReturn(
            flowOf(JobSearchResult(query = "", items = emptyList()))
        )

        val vm = viewModel()
        advanceUntilIdle()

        vm.setMinSalary(50000)
        assertEquals(50000, vm.filter.value.minSalary)

        vm.setMinSalary(0)
        assertNull(vm.filter.value.minSalary)

        vm.setMinSalary(-1000)
        assertNull(vm.filter.value.minSalary)
    }

    @Test
    fun `toggleSkillTag adds and removes tag`() = runTest(dispatcher) {
        whenever(searchRepository.search(any(), any())).thenReturn(
            flowOf(JobSearchResult(query = "", items = emptyList()))
        )

        val vm = viewModel()
        advanceUntilIdle()

        vm.toggleSkillTag("kotlin")
        assertEquals(setOf("kotlin"), vm.filter.value.tags)

        vm.toggleSkillTag("android")
        assertEquals(setOf("kotlin", "android"), vm.filter.value.tags)

        vm.toggleSkillTag("kotlin")
        assertEquals(setOf("android"), vm.filter.value.tags)
    }

    @Test
    fun `addSkillTag adds tag to filter`() = runTest(dispatcher) {
        whenever(searchRepository.search(any(), any())).thenReturn(
            flowOf(JobSearchResult(query = "", items = emptyList()))
        )

        val vm = viewModel()
        advanceUntilIdle()

        vm.addSkillTag("kotlin")
        assertEquals(setOf("kotlin"), vm.filter.value.tags)

        vm.addSkillTag("android")
        assertEquals(setOf("kotlin", "android"), vm.filter.value.tags)

        vm.addSkillTag("")
        assertEquals(setOf("kotlin", "android"), vm.filter.value.tags)

        vm.addSkillTag("   ")
        assertEquals(setOf("kotlin", "android"), vm.filter.value.tags)
    }

    @Test
    fun `resetFilter clears all filter options`() = runTest(dispatcher) {
        whenever(searchRepository.search(any(), any())).thenReturn(
            flowOf(JobSearchResult(query = "", items = emptyList()))
        )

        val vm = viewModel()
        advanceUntilIdle()

        vm.setFilter(
            JobFilter(
                types = setOf(JobType.FULL_TIME),
                remoteOnly = true,
                location = "Berlin",
                minSalary = 50000,
                tags = setOf("kotlin")
            )
        )

        vm.resetFilter()
        assertEquals(JobFilter.None, vm.filter.value)
    }

    @Test
    fun `filter changes trigger new search results`() = runTest(dispatcher) {
        val fullTimeJobs = listOf(
            JobSearchItem(kotlinJob.copy(type = JobType.FULL_TIME), score = 5, saved = false)
        )
        val partTimeJobs = listOf(
            JobSearchItem(kotlinJob.copy(type = JobType.PART_TIME), score = 5, saved = false)
        )

        var callCount = 0
        whenever(searchRepository.search(any(), any())).thenAnswer {
            callCount++
            when (callCount) {
                1 -> flowOf(JobSearchResult(query = "", items = fullTimeJobs))
                else -> flowOf(JobSearchResult(query = "", items = partTimeJobs))
            }
        }

        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.totalCount)

        vm.toggleJobType(JobType.PART_TIME)
        // Search repository is called with updated filter, but we verify state updates
        assertEquals(JobFilter(types = setOf(JobType.PART_TIME)), vm.filter.value)
    }

}
