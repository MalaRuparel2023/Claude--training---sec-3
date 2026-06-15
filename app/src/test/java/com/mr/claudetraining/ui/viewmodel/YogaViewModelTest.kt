package com.mr.claudetraining.ui.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.mr.claudetraining.domain.model.DailyGoal
import com.mr.claudetraining.domain.model.UserProfile
import com.mr.claudetraining.domain.model.YogaProgram
import com.mr.claudetraining.domain.repository.YogaRepository

@OptIn(ExperimentalCoroutinesApi::class)
class YogaViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository: YogaRepository = mock()

    private val programs = listOf(
        YogaProgram(id = "1", title = "Morning Flow", category = "Vinyasa", durationMinutes = 20),
        YogaProgram(id = "2", title = "Evening Calm", category = "Restorative", durationMinutes = 30)
    )
    private val profile = UserProfile(name = "Mala", sessions = 5, dayStreak = 3, badges = 2)
    private val dailyGoal = DailyGoal(completed = 1, total = 3)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = YogaViewModel(repository)

    /** Stubs every observe* flow so the init block can collect without NPEs. */
    private fun stubAllEmpty() {
        whenever(repository.observePrograms()).thenReturn(flowOf(emptyList()))
        whenever(repository.observeProfile()).thenReturn(flowOf(null))
        whenever(repository.observeDailyGoal()).thenReturn(flowOf(null))
    }

    // --- Initial load ---

    @Test
    fun `initial state is loading with no data or error`() = runTest(dispatcher) {
        stubAllEmpty()

        val vm = viewModel()

        val state = vm.uiState.value
        assertTrue(state.isLoading)
        assertTrue(state.programs.isEmpty())
        assertNull(state.profile)
        assertNull(state.dailyGoal)
        assertNull(state.error)
    }

    @Test
    fun `init seeds sample data before observing`() = runTest(dispatcher) {
        stubAllEmpty()

        viewModel()
        advanceUntilIdle()

        verify(repository).seedSampleDataIfEmpty()
    }

    // --- Success path ---

    @Test
    fun `programs emission populates state and clears loading`() = runTest(dispatcher) {
        whenever(repository.observePrograms()).thenReturn(flowOf(programs))
        whenever(repository.observeProfile()).thenReturn(flowOf(null))
        whenever(repository.observeDailyGoal()).thenReturn(flowOf(null))

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(programs, state.programs)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `profile emission updates profile field`() = runTest(dispatcher) {
        whenever(repository.observePrograms()).thenReturn(flowOf(emptyList()))
        whenever(repository.observeProfile()).thenReturn(flowOf(profile))
        whenever(repository.observeDailyGoal()).thenReturn(flowOf(null))

        val vm = viewModel()
        advanceUntilIdle()

        assertEquals(profile, vm.uiState.value.profile)
    }

    @Test
    fun `daily goal emission updates dailyGoal field`() = runTest(dispatcher) {
        whenever(repository.observePrograms()).thenReturn(flowOf(emptyList()))
        whenever(repository.observeProfile()).thenReturn(flowOf(null))
        whenever(repository.observeDailyGoal()).thenReturn(flowOf(dailyGoal))

        val vm = viewModel()
        advanceUntilIdle()

        assertEquals(dailyGoal, vm.uiState.value.dailyGoal)
    }

    @Test
    fun `all three flows combine into a single state`() = runTest(dispatcher) {
        whenever(repository.observePrograms()).thenReturn(flowOf(programs))
        whenever(repository.observeProfile()).thenReturn(flowOf(profile))
        whenever(repository.observeDailyGoal()).thenReturn(flowOf(dailyGoal))

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(programs, state.programs)
        assertEquals(profile, state.profile)
        assertEquals(dailyGoal, state.dailyGoal)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    // --- Empty ---

    @Test
    fun `empty programs list clears loading without error`() = runTest(dispatcher) {
        stubAllEmpty()

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.programs.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    // --- Re-emission ---

    @Test
    fun `state updates on every new programs emission`() = runTest(dispatcher) {
        val upstream = MutableSharedFlow<List<YogaProgram>>()
        whenever(repository.observePrograms()).thenReturn(upstream)
        whenever(repository.observeProfile()).thenReturn(flowOf(null))
        whenever(repository.observeDailyGoal()).thenReturn(flowOf(null))

        val vm = viewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isLoading)

        upstream.emit(listOf(programs[0]))
        advanceUntilIdle()
        assertEquals(listOf(programs[0]), vm.uiState.value.programs)
        assertFalse(vm.uiState.value.isLoading)

        upstream.emit(programs)
        advanceUntilIdle()
        assertEquals(programs, vm.uiState.value.programs)
    }

    // --- Error handling ---

    @Test
    fun `programs flow failure sets error message and stops loading`() = runTest(dispatcher) {
        whenever(repository.observePrograms())
            .thenReturn(flow { throw RuntimeException("db error") })
        whenever(repository.observeProfile()).thenReturn(flowOf(null))
        whenever(repository.observeDailyGoal()).thenReturn(flowOf(null))

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("db error", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `programs flow failure without message falls back to default error`() = runTest(dispatcher) {
        whenever(repository.observePrograms())
            .thenReturn(flow { throw RuntimeException() })
        whenever(repository.observeProfile()).thenReturn(flowOf(null))
        whenever(repository.observeDailyGoal()).thenReturn(flowOf(null))

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("Failed to load data", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `profile flow failure is swallowed and does not set error`() = runTest(dispatcher) {
        whenever(repository.observePrograms()).thenReturn(flowOf(programs))
        whenever(repository.observeProfile())
            .thenReturn(flow { throw RuntimeException("profile boom") })
        whenever(repository.observeDailyGoal()).thenReturn(flowOf(null))

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertNull(state.error)
        assertNull(state.profile)
        assertEquals(programs, state.programs)
    }

    @Test
    fun `daily goal flow failure is swallowed and does not set error`() = runTest(dispatcher) {
        whenever(repository.observePrograms()).thenReturn(flowOf(programs))
        whenever(repository.observeProfile()).thenReturn(flowOf(null))
        whenever(repository.observeDailyGoal())
            .thenReturn(flow { throw RuntimeException("goal boom") })

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertNull(state.error)
        assertNull(state.dailyGoal)
        assertEquals(programs, state.programs)
    }

    @Test
    fun `seed failure does not crash and observation still proceeds`() = runTest(dispatcher) {
        repository.stub {
            onBlocking { seedSampleDataIfEmpty() } doThrow RuntimeException("seed failed")
        }
        whenever(repository.observePrograms()).thenReturn(flowOf(programs))
        whenever(repository.observeProfile()).thenReturn(flowOf(null))
        whenever(repository.observeDailyGoal()).thenReturn(flowOf(null))

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(programs, state.programs)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }
}
