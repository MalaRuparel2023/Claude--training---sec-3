package com.mr.claudetraining.data.local

import com.mr.claudetraining.domain.model.Job
import com.mr.claudetraining.domain.model.JobType
import org.junit.Assert.assertEquals
import org.junit.Test

class JobEntityMapperTest {

    @Test
    fun `toDomain maps every field`() {
        val entity = JobEntity(
            id = "j1",
            title = "Engineer",
            company = "Acme",
            location = "Berlin",
            type = JobType.CONTRACT.name,
            remote = true,
            tags = listOf("kotlin"),
            salaryMin = 50000,
            salaryMax = 90000,
            postedAt = 100L,
            cachedAt = 200L
        )

        val domain = entity.toDomain()

        assertEquals("j1", domain.id)
        assertEquals("Engineer", domain.title)
        assertEquals("Acme", domain.company)
        assertEquals("Berlin", domain.location)
        assertEquals(JobType.CONTRACT, domain.type)
        assertEquals(true, domain.remote)
        assertEquals(listOf("kotlin"), domain.tags)
        assertEquals(50000, domain.salaryMin)
        assertEquals(90000, domain.salaryMax)
        assertEquals(100L, domain.postedAt)
    }

    @Test
    fun `toDomain decodes all valid enum values`() {
        JobType.values().forEach { type ->
            val entity = sampleEntity().copy(type = type.name)
            assertEquals(type, entity.toDomain().type)
        }
    }

    @Test
    fun `toDomain falls back to FULL_TIME for unknown type`() {
        assertEquals(JobType.FULL_TIME, sampleEntity().copy(type = "WEEKEND").toDomain().type)
    }

    @Test
    fun `toDomain falls back to FULL_TIME for empty type`() {
        assertEquals(JobType.FULL_TIME, sampleEntity().copy(type = "").toDomain().type)
    }

    @Test
    fun `toEntity maps every field and stamps cachedAt`() {
        val job = Job(
            id = "j2",
            title = "T",
            company = "C",
            location = "L",
            type = JobType.PART_TIME,
            remote = false,
            tags = listOf("a", "b"),
            salaryMin = 1,
            salaryMax = 2,
            postedAt = 9L
        )

        val entity = job.toEntity(cachedAt = 555L)

        assertEquals("j2", entity.id)
        assertEquals(JobType.PART_TIME.name, entity.type)
        assertEquals(listOf("a", "b"), entity.tags)
        assertEquals(9L, entity.postedAt)
        assertEquals(555L, entity.cachedAt)
    }

    @Test
    fun `entity round-trips through domain preserving non-cache fields`() {
        val entity = sampleEntity()
        val rebuilt = entity.toDomain().toEntity(cachedAt = entity.cachedAt)
        assertEquals(entity, rebuilt)
    }

    private fun sampleEntity() = JobEntity(
        id = "s",
        title = "t",
        company = "c",
        location = "l",
        type = JobType.FULL_TIME.name,
        remote = true,
        tags = listOf("x"),
        salaryMin = 10,
        salaryMax = 20,
        postedAt = 1L,
        cachedAt = 2L
    )
}
