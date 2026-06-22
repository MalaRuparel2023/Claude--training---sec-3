package com.mr.claudetraining.data.repository

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.result.Result
import io.getstream.result.call.Call
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import com.mr.claudetraining.data.model.toDomain
import com.mr.claudetraining.domain.model.ChannelSnapshot
import com.mr.claudetraining.domain.model.ChatChannel
import com.mr.claudetraining.domain.model.ChatConnectionState
import com.mr.claudetraining.domain.model.ChatMessage
import com.mr.claudetraining.domain.model.ChatUser
import com.mr.claudetraining.domain.repository.ChatRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatClient: ChatClient
) : ChatRepository {

    private val _connectionState = MutableStateFlow<ChatConnectionState>(ChatConnectionState.Idle)
    override val connectionState: StateFlow<ChatConnectionState> = _connectionState.asStateFlow()

    override fun currentUserId(): String? = chatClient.getCurrentUser()?.id

    @Volatile
    private var connectionEventsSubscribed = false

    override fun connect() {
        // Already live (or mid-connect) — nothing to do. After a background/network
        // drop the state is Disconnected, so we fall through and re-open the socket.
        when (_connectionState.value) {
            is ChatConnectionState.Connected, ChatConnectionState.Connecting -> return
            else -> Unit
        }

        _connectionState.value = ChatConnectionState.Connecting
        subscribeToConnectionEvents()

        // TODO: Replace with real per-user credentials issued by the backend.
        // These are Stream's public quickstart demo credentials.
        val user = User(
            id = DEMO_USER_ID,
            name = "Tutorial User",
            image = "https://getstream.io/random_png/?id=$DEMO_USER_ID&size=200"
        )

        chatClient.connectUser(user = user, token = DEMO_USER_TOKEN).enqueue { result ->
            when (result) {
                is Result.Success ->
                    _connectionState.value = ChatConnectionState.Connected(user.id)
                is Result.Failure ->
                    _connectionState.value =
                        ChatConnectionState.Error(result.value.message ?: "Failed to connect")
            }
        }
    }

    // Stream auto-reconnects after a network loss; mirroring its Connected/Disconnected
    // events into our state lets the UI show/hide the "Disconnected" banner without us
    // managing the socket manually. Subscribed once for the client's lifetime.
    private fun subscribeToConnectionEvents() {
        if (connectionEventsSubscribed) return
        connectionEventsSubscribed = true
        chatClient.subscribeFor(
            ConnectedEvent::class.java,
            DisconnectedEvent::class.java
        ) { event ->
            when (event) {
                is ConnectedEvent -> _connectionState.value = ChatConnectionState.Connected(event.me.id)
                is DisconnectedEvent ->
                    if (_connectionState.value !is ChatConnectionState.SignedOut) {
                        _connectionState.value = ChatConnectionState.Disconnected
                    }
                else -> Unit
            }
        }
    }

    override fun disconnect(clearData: Boolean) {
        chatClient.disconnect(flushPersistence = clearData).enqueue {
            _connectionState.value =
                if (clearData) ChatConnectionState.SignedOut else ChatConnectionState.Disconnected
        }
    }

    override fun observeChannels(): Flow<List<ChatChannel>> = callbackFlow {
        val currentUserId = currentUserId()
            ?: throw IllegalStateException("Not connected")

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

        suspend fun emitChannels() {
            val channels = chatClient.queryChannels(request).awaitOrThrow()
            trySend(channels.map { it.toDomain() })
        }

        emitChannels()

        val subscription = chatClient.subscribeFor(
            NewMessageEvent::class.java,
            ChannelDeletedEvent::class.java
        ) {
            chatClient.queryChannels(request).enqueue { result ->
                if (result is Result.Success) trySend(result.value.map { it.toDomain() })
            }
        }

        awaitClose { subscription.dispose() }
    }

    override suspend fun createDemoChannel() {
        val currentUserId = currentUserId() ?: DEMO_USER_ID
        chatClient.channel("messaging", "general").create(
            memberIds = listOf(currentUserId),
            extraData = mapOf("name" to "General Chat")
        ).awaitOrThrow()
    }

    override fun watchChannel(channelId: String): Flow<ChannelSnapshot> = callbackFlow {
        val channelClient = chatClient.channel("messaging", channelId)

        var channelName = channelId
        val messages = mutableListOf<ChatMessage>()
        val typingUsers = mutableListOf<ChatUser>()
        val members = mutableListOf<ChatUser>()
        var lastReadByOthers = 0L

        fun emit(isDeleted: Boolean = false) {
            trySend(
                ChannelSnapshot(
                    channelName = channelName,
                    messages = messages.toList(),
                    typingUsers = typingUsers.toList(),
                    members = members.toList(),
                    lastReadByOthers = lastReadByOthers,
                    isDeleted = isDeleted
                )
            )
        }

        val channel = channelClient.watch().awaitOrThrow()
        val currentUserId = currentUserId() ?: ""
        channelName = channel.name.ifEmpty { channelId }
        messages.addAll(channel.messages.map { it.toDomain(currentUserId) })
        members.addAll(channel.members.map { it.user.toDomain() })
        lastReadByOthers = channel.read
            .filter { it.user.id != currentUserId }
            .maxOfOrNull { it.lastRead?.time ?: 0L } ?: 0L
        // Opening the channel marks it read for the current user.
        channelClient.markRead().enqueue()
        emit()

        val subscription = channelClient.subscribeFor(
            NewMessageEvent::class.java,
            TypingStartEvent::class.java,
            TypingStopEvent::class.java,
            UserPresenceChangedEvent::class.java,
            MessageReadEvent::class.java,
            ChannelDeletedEvent::class.java
        ) { event ->
            when (event) {
                is NewMessageEvent -> {
                    val message = event.message.toDomain(currentUserId)
                    if (messages.none { it.id == message.id }) {
                        messages.add(message)
                        // A message arriving while we're watching is read immediately.
                        channelClient.markRead().enqueue()
                        emit()
                    }
                }
                is TypingStartEvent -> {
                    val typingUser = event.user.toDomain()
                    if (typingUser.id != currentUserId &&
                        typingUsers.none { it.id == typingUser.id }
                    ) {
                        typingUsers.add(typingUser)
                        emit()
                    }
                }
                is TypingStopEvent -> {
                    if (typingUsers.removeAll { it.id == event.user.id }) emit()
                }
                is MessageReadEvent -> {
                    if (event.user.id != currentUserId) {
                        lastReadByOthers = maxOf(lastReadByOthers, event.createdAt.time)
                        emit()
                    }
                }
                is UserPresenceChangedEvent -> {
                    if (applyPresence(event.user.toDomain(), messages, members)) emit()
                }
                is ChannelDeletedEvent -> emit(isDeleted = true)
                else -> Unit
            }
        }

        awaitClose {
            subscription.dispose()
            channelClient.stopTyping(parentId = null).enqueue()
            channelClient.stopWatching().enqueue()
        }
    }

    /** Updates the cached online status for [user] across messages and members. */
    private fun applyPresence(
        user: ChatUser,
        messages: MutableList<ChatMessage>,
        members: MutableList<ChatUser>
    ): Boolean {
        var changed = false
        for (i in messages.indices) {
            val author = messages[i].author
            if (author.id == user.id && author.isOnline != user.isOnline) {
                messages[i] = messages[i].copy(author = author.copy(isOnline = user.isOnline))
                changed = true
            }
        }
        val mi = members.indexOfFirst { it.id == user.id }
        if (mi >= 0 && members[mi].isOnline != user.isOnline) {
            members[mi] = members[mi].copy(isOnline = user.isOnline)
            changed = true
        }
        return changed
    }

    override suspend fun sendMessage(channelId: String, text: String) {
        chatClient.channel("messaging", channelId)
            .sendMessage(Message(text = text))
            .awaitOrThrow()
    }

    override fun startTyping(channelId: String) {
        chatClient.channel("messaging", channelId).keystroke(parentId = null).enqueue()
    }

    override fun stopTyping(channelId: String) {
        chatClient.channel("messaging", channelId).stopTyping(parentId = null).enqueue()
    }

    override suspend fun addReaction(messageId: String, emoji: String) {
        chatClient.sendReaction(
            reaction = Reaction(messageId = messageId, type = emoji),
            enforceUnique = false
        ).awaitOrThrow()
    }

    override fun markRead(channelId: String) {
        chatClient.channel("messaging", channelId).markRead().enqueue()
    }

    override suspend fun searchMessages(channelId: String, query: String): List<ChatMessage> {
        if (query.isBlank()) return emptyList()
        val currentUserId = currentUserId() ?: return emptyList()
        val result = chatClient.searchMessages(
            channelFilter = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.eq("cid", "messaging:$channelId"),
                Filters.`in`("members", listOf(currentUserId))
            ),
            messageFilter = Filters.autocomplete("text", query),
            offset = 0,
            limit = 30
        ).awaitOrThrow()
        return result.messages.map { it.toDomain(currentUserId) }
    }

    private suspend fun <T : Any> Call<T>.awaitOrThrow(): T = when (val result = await()) {
        is Result.Success -> result.value
        is Result.Failure -> throw IllegalStateException(result.value.message)
    }

    private companion object {
        const val DEMO_USER_ID = "tutorial-demi"
        const val DEMO_USER_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidHV0b3JpYWwtZGVtaSJ9.cWndtYsPXVwINzDWUbYFmvUnpja73ytEorSQA-LEdPA"
    }
}
