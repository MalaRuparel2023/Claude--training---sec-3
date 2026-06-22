package com.mr.claudetraining.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import com.mr.claudetraining.domain.model.ChannelSnapshot
import com.mr.claudetraining.domain.model.ChatChannel
import com.mr.claudetraining.domain.model.ChatConnectionState
import com.mr.claudetraining.domain.model.ChatMessage

interface ChatRepository {
    val connectionState: StateFlow<ChatConnectionState>
    fun connect()
    fun disconnect(clearData: Boolean)
    fun currentUserId(): String?

    /** Emits the channel list, re-emitting whenever a relevant channel event arrives. */
    fun observeChannels(): Flow<List<ChatChannel>>
    suspend fun createDemoChannel()

    /** Emits a fresh [ChannelSnapshot] on watch and on every subsequent channel event. */
    fun watchChannel(channelId: String): Flow<ChannelSnapshot>
    suspend fun sendMessage(channelId: String, text: String)
    fun startTyping(channelId: String)
    fun stopTyping(channelId: String)
    suspend fun addReaction(messageId: String, emoji: String)

    /** Marks the channel read up to its latest message for the current user. */
    fun markRead(channelId: String)

    /** Full-text search over a channel's message history. */
    suspend fun searchMessages(channelId: String, query: String): List<ChatMessage>
}