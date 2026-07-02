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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.mr.claudetraining.domain.model.AnalyticsEvent
import com.mr.claudetraining.domain.model.EnhancedJob
import com.mr.claudetraining.domain.model.Job
import com.mr.claudetraining.domain.repository.AnalyticsLogger
import com.mr.claudetraining.domain.repository.JobRepository

@OptIn(ExperimentalCoroutinesApi::class)
class JobListViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val jobRepository: JobRepository = mock()
    private val analytics: AnalyticsLogger = mock()

    private val jobs = listOf(
        EnhancedJob(Job(id = "j1", title = "Android Eng"), isFavorite = false),
        EnhancedJob(Job(id = "j2", title = "Backend Eng"), isFavorite = true)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = JobListViewModel(jobRepository, analytics)

    @Test
    fun `emits enhanced jobs and logs view_job_list once on load`() = runTest(dispatcher) {
        whenever(jobRepository.observeEnhancedJobs()).thenReturn(flowOf(jobs))

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(jobs, state.jobs)
        assertFalse(state.isLoading)
        verify(analytics).log(AnalyticsEvent.ViewJobList(2))
    }

    @Test
    fun `toggleSaved delegates to repository and logs save_job`() = runTest(dispatcher) {
        whenever(jobRepository.observeEnhancedJobs()).thenReturn(flowOf(jobs))
        val vm = viewModel()
        advanceUntilIdle()

        vm.toggleSaved("j1", saved = true)
        advanceUntilIdle()

        verify(jobRepository).setSaved("j1", true)
        verify(analytics).log(AnalyticsEvent.SaveJob("j1", true))
    }

    @Test
    fun `blank job id is ignored`() = runTest(dispatcher) {
        whenever(jobRepository.observeEnhancedJobs()).thenReturn(flowOf(jobs))
        val vm = viewModel()
        advanceUntilIdle()

        vm.toggleSaved("", saved = true)
        advanceUntilIdle()

        verify(jobRepository, never()).setSaved(any(), any())
    }
}
