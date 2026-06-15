package com.mr.claudetraining.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** A reaction as stored on a cached message — flattened from the domain MessageReaction. */
@Serializable
data class ReactionData(val emoji: String, val count: Int)

/** One relevance-ranked hit persisted inside a [JobQueryEntity]. Viewer-specific
 *  `saved` state is intentionally NOT cached — it is re-joined from the live favorites
 *  stream on read, so a stale cache can never show the wrong bookmark. */
@Serializable
data class CachedSearchItem(val jobId: String, val score: Int)

/**
 * Room can only persist primitives, so list/object columns round-trip through JSON.
 * One [Json] instance is shared; `ignoreUnknownKeys` keeps old caches readable after a
 * model field is added (Room migrations handle column changes, this handles payload shape).
 */
class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringList(value: List<String>): String = json.encodeToString(value)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        if (value.isBlank()) emptyList() else json.decodeFromString(value)

    @TypeConverter
    fun fromReactions(value: List<ReactionData>): String = json.encodeToString(value)

    @TypeConverter
    fun toReactions(value: String): List<ReactionData> =
        if (value.isBlank()) emptyList() else json.decodeFromString(value)

    @TypeConverter
    fun fromSearchItems(value: List<CachedSearchItem>): String = json.encodeToString(value)

    @TypeConverter
    fun toSearchItems(value: String): List<CachedSearchItem> =
        if (value.isBlank()) emptyList() else json.decodeFromString(value)

    @TypeConverter
    fun fromDraftStatus(value: DraftStatus): String = value.name

    @TypeConverter
    fun toDraftStatus(value: String): DraftStatus =
        runCatching { DraftStatus.valueOf(value) }.getOrDefault(DraftStatus.PENDING)
}
