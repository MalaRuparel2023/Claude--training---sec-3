package com.mr.claudetraining.domain.model

/** A chat conversation. Framework-free; the data layer maps SDK channels onto this type. */
data class ChatChannel(
    val id: String,
    val name: String,
    val image: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = 0,
    val unreadCount: Int = 0
)

data class ChatUser(
    val id: String,
    val name: String = "",
    val image: String = "",
    val isOnline: Boolean = false
)

data class MessageReaction(
    val emoji: String,
    val count: Int
)

data class ChatMessage(
    val id: String,
    val text: String,
    val author: ChatUser,
    val createdAt: Long,
    val isMine: Boolean = false,
    val reactions: List<MessageReaction> = emptyList()
)

/** Snapshot of a watched channel, re-emitted by the data layer as events arrive. */
data class ChannelSnapshot(
    val channelName: String,
    val messages: List<ChatMessage> = emptyList(),
    val typingUsers: List<ChatUser> = emptyList(),
    val isDeleted: Boolean = false
)

sealed interface ChatConnectionState {
    data object Idle : ChatConnectionState
    data object Connecting : ChatConnectionState
    data class Connected(val userId: String) : ChatConnectionState
    data class Error(val message: String) : ChatConnectionState
    data object Disconnected : ChatConnectionState
    data object SignedOut : ChatConnectionState
}