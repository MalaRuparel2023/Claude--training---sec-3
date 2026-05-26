package ro.alexmamo.firebasesigninwithemailandpassword.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import ro.alexmamo.firebasesigninwithemailandpassword.SrteamChatApp
import ro.alexmamo.firebasesigninwithemailandpassword.data.model.ChatDetailUiState
import ro.alexmamo.firebasesigninwithemailandpassword.data.model.MessageUi
import ro.alexmamo.firebasesigninwithemailandpassword.data.model.UserUi
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatDetailViewModel(
    private val chatClient: ChatClient = SrteamChatApp.streamChatClient!!,
    savedStateHandle: SavedStateHandle? = null,
    val channelId: String = ""
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ChatDetailUiState(
            channelId = channelId,
            isLoading = true
        )
    )
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

    private val channelClient = chatClient.channel("messaging", channelId)

    init {
        if (channelId.isNotEmpty()) {
            loadMessages()
        }
    }

    private fun loadMessages() {
        channelClient.watch().enqueue { result ->
            when (result) {
                is Result.Success -> {
                    val currentUserId = chatClient.getCurrentUser()?.id ?: ""
                    val channel = result.value
                    val messages = channel.messages.map { MessageUi.fromMessage(it, currentUserId) }
                    _uiState.value = _uiState.value.copy(
                        channelName = channel.name.ifEmpty { channelId },
                        messages = messages,
                        isLoading = false
                    )
                    subscribeToChannelEvents()
                }
                is Result.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.value.message ?: "Failed to load messages"
                    )
                }
            }
        }
    }

    private fun subscribeToChannelEvents() {
        channelClient.subscribeFor(
            NewMessageEvent::class.java,
            TypingStartEvent::class.java,
            TypingStopEvent::class.java,
            UserPresenceChangedEvent::class.java,
            ChannelDeletedEvent::class.java
        ) { event ->
            handleChannelEvent(event)
        }
    }

    private fun handleChannelEvent(event: Any) {
        val currentUserId = chatClient.getCurrentUser()?.id ?: ""
        when (event) {
            is NewMessageEvent -> {
                val newMsg = MessageUi.fromMessage(event.message, currentUserId)
                val currentMessages = _uiState.value.messages
                if (currentMessages.none { it.id == newMsg.id }) {
                    _uiState.value = _uiState.value.copy(
                        messages = currentMessages + newMsg
                    )
                }
            }
            is TypingStartEvent -> {
                val typingUser = UserUi.fromStreamUser(event.user)
                val currentTypingUsers = _uiState.value.typingUsers
                if (currentTypingUsers.none { it.id == typingUser.id }) {
                    _uiState.value = _uiState.value.copy(
                        typingUsers = currentTypingUsers + typingUser
                    )
                }
            }
            is TypingStopEvent -> {
                _uiState.value = _uiState.value.copy(
                    typingUsers = _uiState.value.typingUsers.filter { it.id != event.user.id }
                )
            }
            is UserPresenceChangedEvent -> {
                // User presence changed - no immediate UI change required
            }
            is ChannelDeletedEvent -> {
                _uiState.value = _uiState.value.copy(
                    error = "This channel has been deleted"
                )
            }
            else -> Unit
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || channelId.isEmpty()) return

        val message = Message(text = text)
        channelClient.sendMessage(message).enqueue { result ->
            if (result is Result.Failure) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to send message: ${result.value.message}"
                )
            }
        }
    }

    fun startTyping() {
        channelClient.keystroke(parentId = null).enqueue()
    }

    fun stopTyping() {
        channelClient.stopTyping(parentId = null).enqueue()
    }

    fun addReaction(messageId: String, emoji: String) {
        val reaction = Reaction(messageId = messageId, type = emoji)
        chatClient.sendReaction(reaction, enforceUnique = false).enqueue { result ->
            if (result is Result.Failure) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add reaction: ${result.value.message}"
                )
            }
        }
    }

    fun retry() {
        loadMessages()
    }

    override fun onCleared() {
        super.onCleared()
        stopTyping()
        channelClient.stopWatching().enqueue()
    }
}