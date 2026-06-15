package com.mr.claudetraining.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import com.mr.claudetraining.domain.model.ChatMessage
import com.mr.claudetraining.domain.model.ChatUser
import com.mr.claudetraining.domain.repository.ChatRepository
import javax.inject.Inject

data class ChatDetailUiState(
    val channelId: String = "",
    val channelName: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val typingUsers: List<ChatUser> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val channelId: String = savedStateHandle["channelId"] ?: ""

    private val _uiState = MutableStateFlow(
        ChatDetailUiState(channelId = channelId, isLoading = channelId.isNotEmpty())
    )
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

    private var watchJob: Job? = null

    init {
        if (channelId.isNotEmpty()) watch()
    }

    private fun watch() {
        watchJob?.cancel()
        watchJob = viewModelScope.launch {
            chatRepository.watchChannel(channelId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load messages"
                    )
                }
                .collect { snapshot ->
                    _uiState.value = _uiState.value.copy(
                        channelName = snapshot.channelName,
                        messages = snapshot.messages,
                        typingUsers = snapshot.typingUsers,
                        isLoading = false,
                        error = if (snapshot.isDeleted) "This channel has been deleted" else null
                    )
                }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || channelId.isEmpty()) return
        viewModelScope.launch {
            runCatching { chatRepository.sendMessage(channelId, text) }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to send message: ${e.message}"
                    )
                }
        }
    }

    fun startTyping() = chatRepository.startTyping(channelId)

    fun stopTyping() = chatRepository.stopTyping(channelId)

    fun addReaction(messageId: String, emoji: String) {
        viewModelScope.launch {
            runCatching { chatRepository.addReaction(messageId, emoji) }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to add reaction: ${e.message}"
                    )
                }
        }
    }

    fun retry() = watch()
}