package com.mr.claudetraining.data.model

import com.mr.claudetraining.domain.model.Job
import com.mr.claudetraining.domain.model.JobType

/** Field defaults are required for Firestore's reflective deserialization. */
data class JobDto(
    val id: String = "",
    val title: String = "",
    val company: String = "",
    val location: String = "",
    val type: String = JobType.FULL_TIME.name,
    val remote: Boolean = false,
    val tags: List<String> = emptyList(),
    val salaryMin: Int = 0,
    val salaryMax: Int = 0,
    val postedAt: Long = 0L
)

fun JobDto.toDomain() = Job(
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

fun Job.toDto() = JobDto(
    id = id,
    title = title,
    company = company,
    location = location,
    type = type.name,
    remote = remote,
    tags = tags,
    salaryMin = salaryMin,
    salaryMax = salaryMax,
    postedAt = postedAt
)