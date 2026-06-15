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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.mr.claudetraining.domain.model.ChatChannel
import com.mr.claudetraining.domain.repository.ChatRepository

@OptIn(ExperimentalCoroutinesApi::class)
class ChatListViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val chatRepository: ChatRepository = mock()

    private val channels = listOf(
        ChatChannel(id = "general", name = "General", lastMessage = "hi", unreadCount = 2),
        ChatChannel(id = "random", name = "Random")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = ChatListViewModel(chatRepository)

    // --- Initial load ---

    @Test
    fun `init shows loading until first emission`() = runTest(dispatcher) {
        whenever(chatRepository.observeChannels()).thenReturn(flowOf(channels))

        val vm = viewModel()

        assertTrue(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `initial load emits channels and clears loading`() = runTest(dispatcher) {
        whenever(chatRepository.observeChannels()).thenReturn(flowOf(channels))

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(channels, state.channels)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `empty channel list surfaces guidance error`() = runTest(dispatcher) {
        whenever(chatRepository.observeChannels()).thenReturn(flowOf(emptyList()))

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.channels.isEmpty())
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun `state updates on every new emission from repository`() = runTest(dispatcher) {
        val upstream = MutableSharedFlow<List<ChatChannel>>()
        whenever(chatRepository.observeChannels()).thenReturn(upstream)

        val vm = viewModel()
        advanceUntilIdle()

        upstream.emit(listOf(channels[0]))
        advanceUntilIdle()
        assertEquals(listOf(channels[0]), vm.uiState.value.channels)

        upstream.emit(channels)
        advanceUntilIdle()
        assertEquals(channels, vm.uiState.value.channels)
    }

    // --- Error handling ---

    @Test
    fun `flow failure sets error message and stops loading`() = runTest(dispatcher) {
        whenever(chatRepository.observeChannels())
            .thenReturn(flow { throw RuntimeException("network down") })

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("network down", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `flow failure without message falls back to default error`() = runTest(dispatcher) {
        whenever(chatRepository.observeChannels())
            .thenReturn(flow { throw RuntimeException() })

        val vm = viewModel()
        advanceUntilIdle()

        assertEquals("Failed to load channels", vm.uiState.value.error)
    }

    @Test
    fun `retry after failure clears error and loads channels`() = runTest(dispatcher) {
        whenever(chatRepository.observeChannels())
            .thenReturn(flow { throw RuntimeException("boom") }, flowOf(channels))

        val vm = viewModel()
        advanceUntilIdle()
        assertEquals("boom", vm.uiState.value.error)

        vm.retryLoadChannels()
        assertTrue(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.error)

        advanceUntilIdle()
        assertEquals(channels, vm.uiState.value.channels)
        assertNull(vm.uiState.value.error)
    }

    // --- createDemoChannel ---

    @Test
    fun `createDemoChannel success reloads channels`() = runTest(dispatcher) {
        whenever(chatRepository.observeChannels())
            .thenReturn(flowOf(emptyList()), flowOf(channels))

        val vm = viewModel()
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.error)

        vm.createDemoChannel()
        advanceUntilIdle()

        verify(chatRepository).createDemoChannel()
        assertEquals(channels, vm.uiState.value.channels)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `createDemoChannel failure sets error and keeps existing channels`() = runTest(dispatcher) {
        whenever(chatRepository.observeChannels()).thenReturn(flowOf(channels))
        chatRepository.stub {
            onBlocking { createDemoChannel() } doThrow RuntimeException("create failed")
        }

        val vm = viewModel()
        advanceUntilIdle()

        vm.createDemoChannel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("create failed", state.error)
        assertEquals(channels, state.channels)
    }
}