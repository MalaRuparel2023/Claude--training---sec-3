package com.mr.claudetraining.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
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
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.mr.claudetraining.domain.model.ChannelSnapshot
import com.mr.claudetraining.domain.model.ChatMessage
import com.mr.claudetraining.domain.model.ChatUser
import com.mr.claudetraining.domain.repository.ChatRepository

@OptIn(ExperimentalCoroutinesApi::class)
class ChatDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val chatRepository: ChatRepository = mock()

    private val author = ChatUser(id = "u1", name = "Alice")
    private val messages = listOf(
        ChatMessage(id = "m1", text = "hello", author = author, createdAt = 1),
        ChatMessage(id = "m2", text = "world", author = author, createdAt = 2)
    )
    private val typingUsers = listOf(ChatUser(id = "u2", name = "Bob"))
    private val snapshot = ChannelSnapshot(
        channelName = "General",
        messages = messages,
        typingUsers = typingUsers
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(channelId: String? = "general"): ChatDetailViewModel {
        val handle = SavedStateHandle().apply {
            if (channelId != null) set("channelId", channelId)
        }
        return ChatDetailViewModel(chatRepository, handle)
    }

    // --- Initial state ---

    @Test
    fun `init seeds channelId and loading from saved state`() = runTest(dispatcher) {
        whenever(chatRepository.watchChannel("general")).thenReturn(flowOf(snapshot))

        val vm = viewModel()

        assertEquals("general", vm.uiState.value.channelId)
        assertTrue(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `init with missing channelId does not load or watch`() = runTest(dispatcher) {
        val vm = viewModel(channelId = null)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("", state.channelId)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertTrue(state.messages.isEmpty())
        verify(chatRepository, never()).watchChannel(any())
    }

    // --- Initial load: loading -> data ---

    @Test
    fun `initial load populates state from snapshot and clears loading`() = runTest(dispatcher) {
        whenever(chatRepository.watchChannel("general")).thenReturn(flowOf(snapshot))

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("General", state.channelName)
        assertEquals(messages, state.messages)
        assertEquals(typingUsers, state.typingUsers)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `deleted channel snapshot surfaces deleted error`() = runTest(dispatcher) {
        whenever(chatRepository.watchChannel("general"))
            .thenReturn(flowOf(snapshot.copy(isDeleted = true)))

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("This channel has been deleted", state.error)
        assertFalse(state.isLoading)
    }

    // --- Message observation flow ---

    @Test
    fun `state updates on every new emission from watchChannel`() = runTest(dispatcher) {
        val upstream = MutableSharedFlow<ChannelSnapshot>()
        whenever(chatRepository.watchChannel("general")).thenReturn(upstream)

        val vm = viewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isLoading)

        upstream.emit(snapshot.copy(messages = listOf(messages[0])))
        advanceUntilIdle()
        assertEquals(listOf(messages[0]), vm.uiState.value.messages)
        assertFalse(vm.uiState.value.isLoading)

        upstream.emit(snapshot)
        advanceUntilIdle()
        assertEquals(messages, vm.uiState.value.messages)
        assertEquals(typingUsers, vm.uiState.value.typingUsers)
    }

    @Test
    fun `emission after deleted error clears the error`() = runTest(dispatcher) {
        val upstream = MutableSharedFlow<ChannelSnapshot>()
        whenever(chatRepository.watchChannel("general")).thenReturn(upstream)

        val vm = viewModel()
        advanceUntilIdle()

        upstream.emit(snapshot.copy(isDeleted = true))
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.error)

        upstream.emit(snapshot)
        advanceUntilIdle()
        assertNull(vm.uiState.value.error)
    }

    // --- Watch error handling ---

    @Test
    fun `watch failure sets error message and stops loading`() = runTest(dispatcher) {
        whenever(chatRepository.watchChannel("general"))
            .thenReturn(flow { throw RuntimeException("socket closed") })

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("socket closed", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `watch failure without message falls back to default error`() = runTest(dispatcher) {
        whenever(chatRepository.watchChannel("general"))
            .thenReturn(flow { throw RuntimeException() })

        val vm = viewModel()
        advanceUntilIdle()

        assertEquals("Failed to load messages", vm.uiState.value.error)
        assertFalse(vm.uiState.value.isLoading)
    }

    // --- retry ---

    @Test
    fun `retry after failure reloads messages`() = runTest(dispatcher) {
        whenever(chatRepository.watchChannel("general"))
            .thenReturn(flow { throw RuntimeException("boom") }, flowOf(snapshot))

        val vm = viewModel()
        advanceUntilIdle()
        assertEquals("boom", vm.uiState.value.error)

        vm.retry()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(messages, state.messages)
        assertNull(state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `retry cancels previous watch and resubscribes`() = runTest(dispatcher) {
        whenever(chatRepository.watchChannel("general"))
            .thenReturn(flowOf(snapshot), flowOf(snapshot))

        val vm = viewModel()
        advanceUntilIdle()

        vm.retry()
        advanceUntilIdle()

        verify(chatRepository, org.mockito.kotlin.times(2)).watchChannel("general")
    }

    // --- sendMessage ---

    @Test
    fun `sendMessage success forwards text to repository`() = runTest(dispatcher) {
        whenever(chatRepository.watchChannel("general")).thenReturn(flowOf(snapshot))

        val vm = viewModel()
        advanceUntilIdle()

        vm.sendMessage("hi there")
        advanceUntilIdle()

        verify(chatRepository).sendMessage("general", "hi there")
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `sendMessage blank text is ignored`() = runTest(dispatcher) {
        whenever(chatRepository.watchChannel("general")).thenReturn(flowOf(snapshot))

        val vm = viewModel()
        advanceUntilIdle()

        vm.sendMessage("   ")
        advanceUntilIdle()

        verify(chatRepository, never()).sendMessage(any(), any())
    }

    @Test
    fun `sendMessage ignored when channelId is empty`() = runTest(dispatcher) {
        val vm = viewModel(channelId = null)
        advanceUntilIdle()

        vm.sendMessage("hi")
        advanceUntilIdle()

        verify(chatRepository, never()).sendMessage(any(), any())
    }

    @Test
    fun `sendMessage failure with message sets error`() = runTest(dispatcher) {
        whenever(chatRepository.watchChannel("general")).thenReturn(flowOf(snapshot))
        chatRepository.stub {
            onBlocking { sendMessage(any(), any()) } doThrow RuntimeException("send failed")
        }

        val vm = viewModel()
        advanceUntilIdle()

        vm.sendMessage("hi")
        advanceUntilIdle()

        assertEquals("Failed to send message: send failed", vm.uiState.value.error)
    }

    @Test
    fun `sendMessage failure without message sets error with null suffix`() = runTest(dispatcher) {
        whenever(chatRepository.watchChannel("general")).thenReturn(flowOf(snapshot))
        chatRepository.stub {
            onBlocking { sendMessage(any(), any()) } doThrow RuntimeException()
        }

        val vm = viewModel()
        advanceUntilIdle()

        vm.sendMessage("hi")
        advanceUntilIdle()

        assertEquals("Failed to send message: null", vm.uiState.value.error)
    }

    // --- startTyping / stopTyping ---

    @Test
    fun `startTyping delegates to repository with channelId`() = runTest(dispatcher) {
        whenever(chatRepository.watchChannel("general")).thenReturn(flowOf(snapshot))

        val vm = viewModel()
        advanceUntilIdle()

        vm.startTyping()

        verify(chatRepository).startTyping("general")
    }

    @Test
    fun `stopTyping delegates to repository with channelId`() = runTest(dispatcher) {
        whenever(chatRepository.watchChannel("general")).thenReturn(flowOf(snapshot))

        val vm = viewModel()
        advanceUntilIdle()

        vm.stopTyping()

        verify(chatRepository).stopTyping("general")
    }

    // --- addReaction ---

    @Test
    fun `addReaction success forwards to repository`() = runTest(dispatcher) {
        whenever(chatRepository.watchChannel("general")).thenReturn(flowOf(snapshot))

        val vm = viewModel()
        advanceUntilIdle()

        vm.addReaction("m1", "thumbsup")
        advanceUntilIdle()

        verify(chatRepository).addReaction("m1", "thumbsup")
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `addReaction failure with message sets error`() = runTest(dispatcher) {
        whenever(chatRepository.watchChannel("general")).thenReturn(flowOf(snapshot))
        chatRepository.stub {
            onBlocking { addReaction(any(), any()) } doThrow RuntimeException("reaction failed")
        }

        val vm = viewModel()
        advanceUntilIdle()

        vm.addReaction("m1", "thumbsup")
        advanceUntilIdle()

        assertEquals("Failed to add reaction: reaction failed", vm.uiState.value.error)
    }

    @Test
    fun `addReaction failure without message sets error with null suffix`() = runTest(dispatcher) {
        whenever(chatRepository.watchChannel("general")).thenReturn(flowOf(snapshot))
        chatRepository.stub {
            onBlocking { addReaction(any(), any()) } doThrow RuntimeException()
        }

        val vm = viewModel()
        advanceUntilIdle()

        vm.addReaction("m1", "thumbsup")
        advanceUntilIdle()

        assertEquals("Failed to add reaction: null", vm.uiState.value.error)
    }
}
