package ro.alexmamo.firebasesigninwithemailandpassword.ui.viewmodel

import androidx.lifecycle.ViewModel
import ro.alexmamo.firebasesigninwithemailandpassword.SrteamChatApp
import ro.alexmamo.firebasesigninwithemailandpassword.data.model.ChatListUiState
import ro.alexmamo.firebasesigninwithemailandpassword.data.model.ChannelUi
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatListViewModel(
    private val chatClient: ChatClient = SrteamChatApp.streamChatClient!!
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatListUiState>(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    init {
        loadChannels()
    }

    fun loadChannels() {
        val currentUserId = chatClient.getCurrentUser()?.id
        if (currentUserId == null) {
            _uiState.value = _uiState.value.copy(isLoading = false, error = "Not connected")
            android.util.Log.d("ChatListViewModel", "Not connected - getCurrentUser() returned null")
            return
        }

        android.util.Log.d("ChatListViewModel", "Loading channels for user: $currentUserId")
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        val request = QueryChannelsRequest(
            filter = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.`in`("members", listOf(currentUserId))
            ),
            querySort = QuerySortByField.descByName("last_message_at"),
            limit = 30,
            offset = 0,
            messageLimit = 1
        )

        chatClient.queryChannels(request).enqueue { result ->
            when (result) {
                is Result.Success -> {
                    val channels = result.value
                    android.util.Log.d("ChatListViewModel", "Got ${channels.size} channels")

                    val channelUis = channels.map { ChannelUi.fromChannel(it) }
                    _uiState.value = ChatListUiState(
                        channels = channelUis,
                        isLoading = false,
                        error = if (channels.isEmpty()) "No channels. Create channels in Stream Dashboard and add tutorial-demi as member." else null
                    )
                }
                is Result.Failure -> {
                    val errorMsg = result.value.message ?: "Failed to load channels"
                    android.util.Log.e("ChatListViewModel", "Error loading channels: $errorMsg")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMsg
                    )
                }
            }
        }

        subscribeToChannelEvents()
    }


    private fun subscribeToChannelEvents() {
        chatClient.subscribeFor(
            NewMessageEvent::class.java,
            ChannelDeletedEvent::class.java
        ) { loadChannels() }
    }

    fun retryLoadChannels() {
        loadChannels()
    }


    fun createDemoChannel() {
        val currentUserId = chatClient.getCurrentUser()?.id ?: "tutorial-demi"
        val channelClient = chatClient.channel("messaging", "general")
        channelClient.create(
            memberIds = listOf(currentUserId),
            extraData = mapOf("name" to "General Chat")
        ).enqueue { result ->
            when (result) {
                is Result.Success -> {
                    android.util.Log.d("ChatListViewModel", "Demo channel created")
                    loadChannels()
                }
                is Result.Failure -> {
                    android.util.Log.e("ChatListViewModel", "Failed to create channel: ${result.value.message}")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}
