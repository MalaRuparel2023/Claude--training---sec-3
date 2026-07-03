package com.mr.claudetraining.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.mr.claudetraining.domain.model.AnalyticsEvent
import com.mr.claudetraining.domain.model.Job
import com.mr.claudetraining.domain.repository.AnalyticsLogger
import com.mr.claudetraining.domain.repository.JobRepository

@OptIn(ExperimentalCoroutinesApi::class)
class JobDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val jobRepository: JobRepository = mock()
    private val analytics: AnalyticsLogger = mock()

    private val job = Job(id = "j1", title = "Android Eng", company = "Acme")

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(saved: Set<String> = emptySet()): JobDetailViewModel {
        whenever(jobRepository.observeJob("j1")).thenReturn(flowOf(job))
        whenever(jobRepository.observeSavedJobIds()).thenReturn(flowOf(saved))
        return JobDetailViewModel(jobRepository, analytics, SavedStateHandle(mapOf("jobId" to "j1")))
    }

    @Test
    fun `resolves job with saved state and logs view_job once`() = runTest(dispatcher) {
        val vm = viewModel(saved = setOf("j1"))
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(job, state.job)
        assertTrue(state.isSaved)
        verify(analytics).log(AnalyticsEvent.ViewJob("j1", "Android Eng"))
    }

    @Test
    fun `toggleSaved saves an unsaved job and logs save_job`() = runTest(dispatcher) {
        val vm = viewModel(saved = emptySet())
        advanceUntilIdle()

        vm.toggleSaved()
        advanceUntilIdle()

        verify(jobRepository).setSaved("j1", true)
        verify(analytics).log(AnalyticsEvent.SaveJob("j1", true))
    }
}
