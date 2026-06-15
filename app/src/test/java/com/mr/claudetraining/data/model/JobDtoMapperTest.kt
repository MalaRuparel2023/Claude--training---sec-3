package com.mr.claudetraining.data.model

import com.mr.claudetraining.domain.model.Job
import com.mr.claudetraining.domain.model.JobType
import org.junit.Assert.assertEquals
import org.junit.Test

class JobDtoMapperTest {

    @Test
    fun `toDomain maps every field`() {
        val dto = JobDto(
            id = "j1",
            title = "Engineer",
            company = "Acme",
            location = "Berlin",
            type = JobType.CONTRACT.name,
            remote = true,
            tags = listOf("kotlin", "android"),
            salaryMin = 50000,
            salaryMax = 90000,
            postedAt = 1234L
        )

        val domain = dto.toDomain()

        assertEquals("j1", domain.id)
        assertEquals("Engineer", domain.title)
        assertEquals("Acme", domain.company)
        assertEquals("Berlin", domain.location)
        assertEquals(JobType.CONTRACT, domain.type)
        assertEquals(true, domain.remote)
        assertEquals(listOf("kotlin", "android"), domain.tags)
        assertEquals(50000, domain.salaryMin)
        assertEquals(90000, domain.salaryMax)
        assertEquals(1234L, domain.postedAt)
    }

    @Test
    fun `toDomain decodes all valid enum values`() {
        JobType.values().forEach { type ->
            assertEquals(type, JobDto(type = type.name).toDomain().type)
        }
    }

    @Test
    fun `toDomain falls back to FULL_TIME for unknown type`() {
        assertEquals(JobType.FULL_TIME, JobDto(type = "BOGUS").toDomain().type)
    }

    @Test
    fun `toDomain falls back to FULL_TIME for empty type`() {
        assertEquals(JobType.FULL_TIME, JobDto(type = "").toDomain().type)
    }

    @Test
    fun `toDomain on default dto yields defaults`() {
        val domain = JobDto().toDomain()
        assertEquals("", domain.id)
        assertEquals(JobType.FULL_TIME, domain.type)
        assertEquals(false, domain.remote)
        assertEquals(emptyList<String>(), domain.tags)
        assertEquals(0, domain.salaryMin)
        assertEquals(0L, domain.postedAt)
    }

    @Test
    fun `toDto maps every field`() {
        val job = Job(
            id = "j2",
            title = "Designer",
            company = "Beta",
            location = "Remote",
            type = JobType.INTERNSHIP,
            remote = true,
            tags = listOf("figma"),
            salaryMin = 10,
            salaryMax = 20,
            postedAt = 99L
        )

        val dto = job.toDto()

        assertEquals("j2", dto.id)
        assertEquals("Designer", dto.title)
        assertEquals("Beta", dto.company)
        assertEquals("Remote", dto.location)
        assertEquals(JobType.INTERNSHIP.name, dto.type)
        assertEquals(true, dto.remote)
        assertEquals(listOf("figma"), dto.tags)
        assertEquals(10, dto.salaryMin)
        assertEquals(20, dto.salaryMax)
        assertEquals(99L, dto.postedAt)
    }

    @Test
    fun `dto round-trips through domain`() {
        val dto = JobDto(
            id = "r",
            title = "T",
            company = "C",
            location = "L",
            type = JobType.PART_TIME.name,
            remote = true,
            tags = listOf("a", "b"),
            salaryMin = 1,
            salaryMax = 2,
            postedAt = 3L
        )
        assertEquals(dto, dto.toDomain().toDto())
    }
}
