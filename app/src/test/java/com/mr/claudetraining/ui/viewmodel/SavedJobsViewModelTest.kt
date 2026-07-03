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
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.mr.claudetraining.domain.model.AnalyticsEvent
import com.mr.claudetraining.domain.model.EnhancedJob
import com.mr.claudetraining.domain.model.Job
import com.mr.claudetraining.domain.repository.AnalyticsLogger
import com.mr.claudetraining.domain.repository.JobRepository

@OptIn(ExperimentalCoroutinesApi::class)
class SavedJobsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val jobRepository: JobRepository = mock()
    private val analytics: AnalyticsLogger = mock()

    private val enhanced = listOf(
        EnhancedJob(Job(id = "j1", title = "Android Eng"), isFavorite = true),
        EnhancedJob(Job(id = "j2", title = "Backend Eng"), isFavorite = false),
        EnhancedJob(Job(id = "j3", title = "iOS Eng"), isFavorite = true)
    )

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `exposes only favorited jobs`() = runTest(dispatcher) {
        whenever(jobRepository.observeEnhancedJobs()).thenReturn(flowOf(enhanced))

        val vm = SavedJobsViewModel(jobRepository, analytics)
        advanceUntilIdle()

        assertEquals(listOf("j1", "j3"), vm.uiState.value.jobs.map { it.job.id })
    }

    @Test
    fun `un-saving from the saved list logs save_job false`() = runTest(dispatcher) {
        whenever(jobRepository.observeEnhancedJobs()).thenReturn(flowOf(enhanced))
        val vm = SavedJobsViewModel(jobRepository, analytics)
        advanceUntilIdle()

        vm.toggleSaved("j1", saved = false)
        advanceUntilIdle()

        verify(jobRepository).setSaved("j1", false)
        verify(analytics).log(AnalyticsEvent.SaveJob("j1", false))
    }
}
