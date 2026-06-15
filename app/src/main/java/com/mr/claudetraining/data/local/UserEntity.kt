package com.mr.claudetraining.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mr.claudetraining.domain.model.ChatUser

/**
 * Cached chat participant (maps to [ChatUser]). Lets the UI show names/avatars for message
 * authors while offline. `isOnline` is presence — only meaningful while connected, so it is
 * snapshotted here and treated as stale once a fresh stream arrives.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val image: String,
    val isOnline: Boolean,
    val cachedAt: Long
)

fun UserEntity.toDomain() = ChatUser(
    id = id,
    name = name,
    image = image,
    isOnline = isOnline
)

fun ChatUser.toEntity(cachedAt: Long) = UserEntity(
    id = id,
    name = name,
    image = image,
    isOnline = isOnline,
    cachedAt = cachedAt
)