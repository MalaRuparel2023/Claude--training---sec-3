package com.mr.claudetraining.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mr.claudetraining.domain.model.Job
import com.mr.claudetraining.domain.model.JobType

/**
 * Cached copy of a [Job] from the Firestore catalog, so the list renders instantly offline
 * or on cold start before the live stream connects. `type` is stored as the enum name
 * (matching JobDto), decoded defensively on read. `cachedAt` lets the repository evict
 * stale rows. Favorite state is NOT stored here — it lives in the live favorites stream and
 * is re-joined on read, mirroring observeEnhancedJobs.
 */
@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey val id: String,
    val title: String,
    val company: String,
    val location: String,
    val type: String,
    val remote: Boolean,
    val tags: List<String>,
    val salaryMin: Int,
    val salaryMax: Int,
    val postedAt: Long,
    val cachedAt: Long
)

fun JobEntity.toDomain() = Job(
    id = id,
    title = title,
    company = company,
    location = location,
    type = runCatching { JobType.valueOf(type) }.getOrDefault(JobType.FULL_TIME),
    remote = remote,
    tags = tags,
    salaryMin = salaryMin,
    salaryMax = salaryMax,
    postedAt = postedAt
)

fun Job.toEntity(cachedAt: Long) = JobEntity(
    id = id,
    title = title,
    company = company,
    location = location,
    type = type.name,
    remote = remote,
    tags = tags,
    salaryMin = salaryMin,
    salaryMax = salaryMax,
    postedAt = postedAt,
    cachedAt = cachedAt
)