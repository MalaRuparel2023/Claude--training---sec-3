package com.mr.claudetraining.domain.model

/** Employment type of a posting. */
enum class JobType { FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP }

/** A job posting in the shared catalog. Framework-free; the data layer maps DTOs onto this type. */
data class Job(
    val id: String = "",
    val title: String = "",
    val company: String = "",
    val location: String = "",
    val type: JobType = JobType.FULL_TIME,
    val remote: Boolean = false,
    val tags: List<String> = emptyList(),
    val salaryMin: Int = 0,
    val salaryMax: Int = 0,
    val postedAt: Long = 0L
)

/**
 * Declarative filter criteria over [Job]. An empty field means "no constraint", so the
 * default instance ([None]) matches every job. Filtering lives in the model — both
 * [com.mr.claudetraining.domain.repository.FilteredJobsRepository]
 * and [com.mr.claudetraining.domain.repository.SearchRepository]
 * apply it through [matches], so the rule is defined exactly once.
 */
data class JobFilter(
    val types: Set<JobType> = emptySet(),
    val remoteOnly: Boolean = false,
    val location: String? = null,
    val minSalary: Int? = null,
    val tags: Set<String> = emptySet()
) {
    fun matches(job: Job): Boolean {
        if (types.isNotEmpty() && job.type !in types) return false
        if (remoteOnly && !job.remote) return false
        if (!location.isNullOrBlank() && !job.location.contains(location, ignoreCase = true)) return false
        if (minSalary != null && job.salaryMax < minSalary) return false
        if (tags.isNotEmpty() && tags.none { tag -> job.tags.any { it.equals(tag, ignoreCase = true) } }) return false
        return true
    }

    val isEmpty: Boolean
        get() = this == None

    companion object {
        val None = JobFilter()
    }
}

/**
 * A [Job] decorated with the viewer's favorite state. Produced by combining the job catalog
 * with the saved-id stream, so the UI binds one list instead of cross-referencing two.
 */
data class EnhancedJob(
    val job: Job,
    val isFavorite: Boolean = false
)

/** A single hit produced by [com.mr.claudetraining.domain.repository.SearchRepository]. */
data class JobSearchItem(
    val job: Job,
    val score: Int,
    val saved: Boolean
)

/** Outcome of combining the query, filter, job, and saved-id streams. */
data class JobSearchResult(
    val query: String = "",
    val items: List<JobSearchItem> = emptyList()
) {
    val totalCount: Int get() = items.size
    val isEmpty: Boolean get() = items.isEmpty()
}