package com.mr.claudetraining.data.model

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import com.mr.claudetraining.domain.model.ChatChannel
import com.mr.claudetraining.domain.model.ChatMessage
import com.mr.claudetraining.domain.model.ChatUser
import com.mr.claudetraining.domain.model.MessageReaction

fun Channel.toDomain(): ChatChannel {
    val lastMessage = messages.lastOrNull()
    return ChatChannel(
        id = id,
        name = name.ifEmpty { id },
        image = image,
        lastMessage = lastMessage?.text ?: "",
        lastMessageTime = lastMessage?.createdAt?.time ?: 0,
        unreadCount = unreadCount
    )
}

fun User.toDomain(): ChatUser = ChatUser(
    id = id,
    name = name.ifEmpty { id },
    image = image,
    isOnline = online
)

fun Message.toDomain(currentUserId: String): ChatMessage = ChatMessage(
    id = id,
    text = text,
    author = (user ?: User()).toDomain(),
    createdAt = createdAt?.time ?: 0,
    isMine = user?.id == currentUserId,
    reactions = reactionCounts.map { (emoji, count) -> MessageReaction(emoji, count) }
)