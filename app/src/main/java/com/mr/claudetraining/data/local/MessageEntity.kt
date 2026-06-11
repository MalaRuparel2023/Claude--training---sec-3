package com.mr.claudetraining.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mr.claudetraining.domain.model.ChatMessage
import com.mr.claudetraining.domain.model.ChatUser
import com.mr.claudetraining.domain.model.MessageReaction

/**
 * Cached chat message (maps to [ChatMessage]). `channelId` scopes a message to its
 * conversation — the domain model omits it because it is always read in a per-channel
 * context, but the cache must store it to query `WHERE channelId = ? ORDER BY createdAt`.
 *
 * `authorId` references [UserEntity] and is indexed for the join, but there is deliberately
 * NO foreign-key constraint: this is a cache, and an author being evicted must not cascade
 * and delete its messages. Author display fields are resolved on read.
 */
@Entity(
    tableName = "messages",
    indices = [Index("channelId", "createdAt"), Index("authorId")]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val channelId: String,
    val authorId: String,
    val text: String,
    val createdAt: Long,
    val isMine: Boolean,
    val reactions: List<ReactionData>
)

fun MessageEntity.toDomain(author: ChatUser) = ChatMessage(
    id = id,
    text = text,
    author = author,
    createdAt = createdAt,
    isMine = isMine,
    reactions = reactions.map { MessageReaction(it.emoji, it.count) }
)

fun ChatMessage.toEntity(channelId: String) = MessageEntity(
    id = id,
    channelId = channelId,
    authorId = author.id,
    text = text,
    createdAt = createdAt,
    isMine = isMine,
    reactions = reactions.map { ReactionData(it.emoji, it.count) }
)
