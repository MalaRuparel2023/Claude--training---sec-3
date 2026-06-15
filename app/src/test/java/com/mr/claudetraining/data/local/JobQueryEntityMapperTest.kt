package com.mr.claudetraining.data.local

import com.mr.claudetraining.domain.model.Job
import com.mr.claudetraining.domain.model.JobFilter
import com.mr.claudetraining.domain.model.JobSearchItem
import com.mr.claudetraining.domain.model.JobSearchResult
import com.mr.claudetraining.domain.model.JobType
import org.junit.Assert.assertEquals
import org.junit.Test

class JobQueryEntityMapperTest {

    private fun job(id: String) = Job(id = id, title = "title-$id")

    @Test
    fun `toDomain resolves jobs joins saved state and preserves order`() {
        val entity = JobQueryEntity(
            queryKey = "k",
            queryText = "android",
            items = listOf(CachedSearchItem("a", 10), CachedSearchItem("b", 5)),
            cachedAt = 0L
        )
        val jobsById = mapOf("a" to job("a"), "b" to job("b"))

        val result = entity.toDomain(jobsById, savedIds = setOf("b"))

        assertEquals("android", result.query)
        assertEquals(2, result.items.size)
        assertEquals(job("a"), result.items[0].job)
        assertEquals(10, result.items[0].score)
        assertEquals(false, result.items[0].saved)
        assertEquals(job("b"), result.items[1].job)
        assertEquals(5, result.items[1].score)
        assertEquals(true, result.items[1].saved)
    }

    @Test
    fun `toDomain skips items whose job was evicted`() {
        val entity = JobQueryEntity(
            queryKey = "k",
            queryText = "q",
            items = listOf(CachedSearchItem("present", 1), CachedSearchItem("gone", 2)),
            cachedAt = 0L
        )

        val result = entity.toDomain(mapOf("present" to job("present")), emptySet())

        assertEquals(1, result.items.size)
        assertEquals("present", result.items[0].job.id)
    }

    @Test
    fun `toDomain on empty items yields empty result`() {
        val entity = JobQueryEntity("k", "q", emptyList(), 0L)
        assertEquals(0, entity.toDomain(emptyMap(), emptySet()).items.size)
    }

    @Test
    fun `keyOf trims and lowercases query`() {
        val a = JobQueryEntity.keyOf("  Android  ", JobFilter.None)
        val b = JobQueryEntity.keyOf("android", JobFilter.None)
        assertEquals(b, a)
    }

    @Test
    fun `keyOf is order-independent for types and tags`() {
        val f1 = JobFilter(
            types = setOf(JobType.FULL_TIME, JobType.CONTRACT),
            tags = setOf("kotlin", "android")
        )
        val f2 = JobFilter(
            types = setOf(JobType.CONTRACT, JobType.FULL_TIME),
            tags = setOf("android", "kotlin")
        )
        assertEquals(JobQueryEntity.keyOf("q", f1), JobQueryEntity.keyOf("q", f2))
    }

    @Test
    fun `keyOf encodes the full filter shape`() {
        val key = JobQueryEntity.keyOf(
            "Dev",
            JobFilter(
                types = setOf(JobType.PART_TIME),
                remoteOnly = true,
                location = "  Berlin ",
                minSalary = 50000,
                tags = setOf("Kotlin")
            )
        )
        assertEquals("dev|t=PART_TIME|r=true|l=berlin|s=50000|g=kotlin", key)
    }

    @Test
    fun `keyOf uses sentinels for none filter`() {
        assertEquals("q|t=|r=false|l=|s=-1|g=", JobQueryEntity.keyOf("q", JobFilter.None))
    }

    @Test
    fun `keyOf differs when filters differ`() {
        val withRemote = JobQueryEntity.keyOf("q", JobFilter(remoteOnly = true))
        val without = JobQueryEntity.keyOf("q", JobFilter(remoteOnly = false))
        org.junit.Assert.assertNotEquals(withRemote, without)
    }

    @Test
    fun `toEntity builds key from query and filter and flattens items`() {
        val result = JobSearchResult(
            query = "android",
            items = listOf(
                JobSearchItem(job = job("a"), score = 9, saved = true),
                JobSearchItem(job = job("b"), score = 4, saved = false)
            )
        )
        val filter = JobFilter(remoteOnly = true)

        val entity = result.toEntity(filter, cachedAt = 42L)

        assertEquals(JobQueryEntity.keyOf("android", filter), entity.queryKey)
        assertEquals("android", entity.queryText)
        assertEquals(42L, entity.cachedAt)
        assertEquals(
            listOf(CachedSearchItem("a", 9), CachedSearchItem("b", 4)),
            entity.items
        )
    }

    @Test
    fun `result round-trips through entity when all jobs resolve`() {
        val result = JobSearchResult(
            query = "kotlin",
            items = listOf(
                JobSearchItem(job = job("a"), score = 7, saved = true),
                JobSearchItem(job = job("b"), score = 3, saved = false)
            )
        )
        val filter = JobFilter(tags = setOf("kotlin"))
        val jobsById = mapOf("a" to job("a"), "b" to job("b"))

        val rebuilt = result.toEntity(filter, 0L).toDomain(jobsById, savedIds = setOf("a"))

        assertEquals(result, rebuilt)
    }
}
