package com.mr.claudetraining.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import com.mr.claudetraining.domain.model.ChatChannel
import com.mr.claudetraining.domain.repository.ChatRepository
import javax.inject.Inject

data class ChatListUiState(
    val channels: List<ChatChannel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        loadChannels()
    }

    fun loadChannels() {
        observeJob?.cancel()
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        observeJob = viewModelScope.launch {
            chatRepository.observeChannels()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load channels"
                    )
                }
                .collect { channels ->
                    _uiState.value = ChatListUiState(
                        channels = channels,
                        isLoading = false,
                        error = if (channels.isEmpty())
                            "No channels. Create channels in Stream Dashboard and add tutorial-demi as member."
                        else null
                    )
                }
        }
    }

    fun retryLoadChannels() = loadChannels()

    fun createDemoChannel() {
        viewModelScope.launch {
            runCatching { chatRepository.createDemoChannel() }
                .onSuccess { loadChannels() }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to create channel"
                    )
                }
        }
    }
}