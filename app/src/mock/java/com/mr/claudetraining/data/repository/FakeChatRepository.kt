package com.mr.claudetraining.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import com.mr.claudetraining.domain.model.ChannelSnapshot
import com.mr.claudetraining.domain.model.ChatChannel
import com.mr.claudetraining.domain.model.ChatConnectionState
import com.mr.claudetraining.domain.model.ChatMessage
import com.mr.claudetraining.domain.model.ChatUser
import com.mr.claudetraining.domain.model.MessageReaction
import com.mr.claudetraining.domain.repository.ChatRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * mock flavor: a fully in-memory [ChatRepository] with seeded channels and messages.
 * No Stream SDK, no network — lets the app run, preview and be tested without credentials.
 */
@Singleton
class FakeChatRepository @Inject constructor() : ChatRepository {

    private val me = ChatUser(id = "mock-me", name = "You", isOnline = true)
    private val alice = ChatUser(id = "alice", name = "Alice", isOnline = true)
    private val bob = ChatUser(id = "bob", name = "Bob", isOnline = false)

    private val now = System.currentTimeMillis()

    private val _connectionState = MutableStateFlow<ChatConnectionState>(ChatConnectionState.Idle)
    override val connectionState: StateFlow<ChatConnectionState> = _connectionState.asStateFlow()

    private val channels = MutableStateFlow(
        listOf(
            ChatChannel("general", "General", lastMessage = "Welcome to the mock build!", lastMessageTime = now, unreadCount = 2),
            ChatChannel("random", "Random", lastMessage = "Anyone up for coffee?", lastMessageTime = now - 3_600_000, unreadCount = 0),
            ChatChannel("design", "Design Team", lastMessage = "New mockups are ready 🎨", lastMessageTime = now - 86_400_000, unreadCount = 5)
        )
    )

    private val messagesByChannel = mutableMapOf<String, MutableStateFlow<List<ChatMessage>>>()

    override fun currentUserId(): String = me.id

    override fun connect() {
        _connectionState.value = ChatConnectionState.Connected(me.id)
    }

    override fun disconnect(clearData: Boolean) {
        _connectionState.value =
            if (clearData) ChatConnectionState.SignedOut else ChatConnectionState.Disconnected
    }

    override fun observeChannels(): Flow<List<ChatChannel>> = channels.asStateFlow()

    override suspend fun createDemoChannel() {
        val id = "demo-${System.currentTimeMillis()}"
        channels.value = channels.value + ChatChannel(
            id = id,
            name = "Demo Channel",
            lastMessage = "Say hi 👋",
            lastMessageTime = System.currentTimeMillis()
        )
    }

    override fun watchChannel(channelId: String): Flow<ChannelSnapshot> {
        val name = channels.value.firstOrNull { it.id == channelId }?.name ?: channelId
        return messagesFlow(channelId).map { messages ->
            ChannelSnapshot(
                channelName = name,
                messages = messages,
                members = listOf(me, alice, bob),
                // Everything sent so far has already been read by the others.
                lastReadByOthers = System.currentTimeMillis()
            )
        }
    }

    override suspend fun sendMessage(channelId: String, text: String) {
        val flow = messagesFlow(channelId)
        val message = ChatMessage(
            id = "m-${System.currentTimeMillis()}",
            text = text,
            author = me,
            createdAt = System.currentTimeMillis(),
            isMine = true
        )
        flow.value = flow.value + message
        channels.value = channels.value.map {
            if (it.id == channelId) it.copy(lastMessage = text, lastMessageTime = message.createdAt) else it
        }
    }

    override fun startTyping(channelId: String) = Unit

    override fun stopTyping(channelId: String) = Unit

    override suspend fun addReaction(messageId: String, emoji: String) {
        messagesByChannel.values.forEach { flow ->
            flow.value = flow.value.map { message ->
                if (message.id == messageId) {
                    message.copy(reactions = message.reactions + MessageReaction(emoji, 1))
                } else {
                    message
                }
            }
        }
    }

    override fun markRead(channelId: String) = Unit

    override suspend fun searchMessages(channelId: String, query: String): List<ChatMessage> {
        if (query.isBlank()) return emptyList()
        return messagesFlow(channelId).value.filter {
            it.text.contains(query, ignoreCase = true)
        }
    }

    private fun messagesFlow(channelId: String): MutableStateFlow<List<ChatMessage>> =
        messagesByChannel.getOrPut(channelId) { MutableStateFlow(seedMessages()) }

    private fun seedMessages(): List<ChatMessage> = listOf(
        ChatMessage("s1", "Hey there! 👋", alice, now - 600_000, isMine = false),
        ChatMessage("s2", "Hi Alice, how's it going?", me, now - 540_000, isMine = true),
        ChatMessage("s3", "All good — this is the mock chat backend.", bob, now - 120_000, isMine = false,
            reactions = listOf(MessageReaction("👍", 2)))
    )
}