package com.mr.claudetraining.ui.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.mr.claudetraining.domain.model.ChatConnectionState
import com.mr.claudetraining.domain.repository.ChatRepository

@OptIn(ExperimentalCoroutinesApi::class)
class UserConnectionViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val chatRepository: ChatRepository = mock()
    private val connectionState = MutableStateFlow<ChatConnectionState>(ChatConnectionState.Idle)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        whenever(chatRepository.connectionState).thenReturn(connectionState)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = UserConnectionViewModel(chatRepository)

    // --- Initial connection ---

    @Test
    fun `init connects to the repository`() = runTest(dispatcher) {
        viewModel()

        verify(chatRepository).connect()
    }

    @Test
    fun `connectionState exposes the repository state flow directly`() = runTest(dispatcher) {
        val vm = viewModel()

        assertSame(connectionState, vm.connectionState)
        assertEquals(ChatConnectionState.Idle, vm.connectionState.value)
    }

    @Test
    fun `state changes from the repository are reflected on the view model`() = runTest(dispatcher) {
        val vm = viewModel()

        connectionState.value = ChatConnectionState.Connecting
        assertEquals(ChatConnectionState.Connecting, vm.connectionState.value)

        connectionState.value = ChatConnectionState.Connected("user-1")
        assertEquals(ChatConnectionState.Connected("user-1"), vm.connectionState.value)

        connectionState.value = ChatConnectionState.Error("boom")
        assertEquals(ChatConnectionState.Error("boom"), vm.connectionState.value)
    }

    // --- signIn ---

    @Test
    fun `signIn calls connect again`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.signIn()

        // once from init, once from signIn
        verify(chatRepository, times(2)).connect()
    }

    // --- signOut ---

    @Test
    fun `signOut disconnects and clears data`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.signOut()

        verify(chatRepository).disconnect(clearData = true)
    }
}
