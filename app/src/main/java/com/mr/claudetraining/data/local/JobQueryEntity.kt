package com.mr.claudetraining.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mr.claudetraining.domain.model.Job
import com.mr.claudetraining.domain.model.JobFilter
import com.mr.claudetraining.domain.model.JobSearchItem
import com.mr.claudetraining.domain.model.JobSearchResult

/**
 * Caches a ranked [JobSearchResult] keyed by the (query + filter) that produced it, so
 * repeating a recent search returns instantly without re-running relevance ranking.
 *
 * Only the ordered (jobId, score) list is stored — the full [Job] rows are re-read from
 * [JobEntity] and the `saved` flag re-joined from the live favorites stream on read. That
 * keeps this table small and prevents a stale cache from ever showing a wrong bookmark.
 * `cachedAt` drives TTL eviction.
 */
@Entity(tableName = "job_queries")
data class JobQueryEntity(
    @PrimaryKey val queryKey: String,
    val queryText: String,
    val items: List<CachedSearchItem>,
    val cachedAt: Long
) {
    /**
     * Rebuilds the domain result. `jobsById` supplies the resolved catalog rows and
     * `savedIds` the current favorites; jobs missing from the cache (evicted) are skipped
     * so the result never contains a half-populated item.
     */
    fun toDomain(jobsById: Map<String, Job>, savedIds: Set<String>) = JobSearchResult(
        query = queryText,
        items = items.mapNotNull { item ->
            jobsById[item.jobId]?.let { job ->
                JobSearchItem(job = job, score = item.score, saved = job.id in savedIds)
            }
        }
    )

    companion object {
        /** Stable cache key — same query text and filter always map to the same row.
         *  Query is trimmed and lower-cased; filter fields are emitted in a fixed order. */
        fun keyOf(query: String, filter: JobFilter): String = buildString {
            append(query.trim().lowercase())
            append("|t=").append(filter.types.map { it.name }.sorted().joinToString(","))
            append("|r=").append(filter.remoteOnly)
            append("|l=").append(filter.location?.trim()?.lowercase().orEmpty())
            append("|s=").append(filter.minSalary ?: -1)
            append("|g=").append(filter.tags.map { it.lowercase() }.sorted().joinToString(","))
        }
    }
}

fun JobSearchResult.toEntity(filter: JobFilter, cachedAt: Long) = JobQueryEntity(
    queryKey = JobQueryEntity.keyOf(query, filter),
    queryText = query,
    items = items.map { CachedSearchItem(jobId = it.job.id, score = it.score) },
    cachedAt = cachedAt
)