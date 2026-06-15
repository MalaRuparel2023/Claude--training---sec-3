package com.mr.claudetraining.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JobFilterTest {

    private fun job(
        type: JobType = JobType.FULL_TIME,
        remote: Boolean = false,
        location: String = "Berlin",
        salaryMax: Int = 100,
        tags: List<String> = listOf("kotlin", "android")
    ) = Job(
        id = "1",
        title = "Engineer",
        company = "Acme",
        location = location,
        type = type,
        remote = remote,
        tags = tags,
        salaryMin = 0,
        salaryMax = salaryMax
    )

    // --- isEmpty ---

    @Test
    fun `default filter is empty`() {
        assertTrue(JobFilter().isEmpty)
        assertTrue(JobFilter.None.isEmpty)
    }

    @Test
    fun `any populated field makes filter non-empty`() {
        assertFalse(JobFilter(remoteOnly = true).isEmpty)
        assertFalse(JobFilter(types = setOf(JobType.CONTRACT)).isEmpty)
        assertFalse(JobFilter(location = "Berlin").isEmpty)
        assertFalse(JobFilter(minSalary = 10).isEmpty)
        assertFalse(JobFilter(tags = setOf("go")).isEmpty)
    }

    // --- matches: empty filter ---

    @Test
    fun `empty filter matches every job`() {
        assertTrue(JobFilter().matches(job()))
        assertTrue(JobFilter().matches(job(type = JobType.INTERNSHIP, remote = true)))
    }

    // --- matches: type ---

    @Test
    fun `type constraint matches when job type is in set`() {
        val filter = JobFilter(types = setOf(JobType.FULL_TIME, JobType.CONTRACT))
        assertTrue(filter.matches(job(type = JobType.FULL_TIME)))
        assertTrue(filter.matches(job(type = JobType.CONTRACT)))
    }

    @Test
    fun `type constraint rejects when job type not in set`() {
        val filter = JobFilter(types = setOf(JobType.CONTRACT))
        assertFalse(filter.matches(job(type = JobType.FULL_TIME)))
    }

    // --- matches: remote ---

    @Test
    fun `remoteOnly rejects on-site jobs and accepts remote ones`() {
        val filter = JobFilter(remoteOnly = true)
        assertFalse(filter.matches(job(remote = false)))
        assertTrue(filter.matches(job(remote = true)))
    }

    // --- matches: location ---

    @Test
    fun `location match is case-insensitive substring`() {
        val filter = JobFilter(location = "berl")
        assertTrue(filter.matches(job(location = "Berlin, DE")))
    }

    @Test
    fun `location mismatch is rejected`() {
        assertFalse(JobFilter(location = "Munich").matches(job(location = "Berlin")))
    }

    @Test
    fun `blank location is treated as no constraint`() {
        assertTrue(JobFilter(location = "").matches(job(location = "Berlin")))
        assertTrue(JobFilter(location = "   ").matches(job(location = "Berlin")))
    }

    // --- matches: salary ---

    @Test
    fun `minSalary compares against job salaryMax`() {
        val filter = JobFilter(minSalary = 100)
        assertTrue(filter.matches(job(salaryMax = 100)))
        assertTrue(filter.matches(job(salaryMax = 150)))
        assertFalse(filter.matches(job(salaryMax = 99)))
    }

    // --- matches: tags ---

    @Test
    fun `tag constraint matches when any tag overlaps case-insensitively`() {
        val filter = JobFilter(tags = setOf("Kotlin"))
        assertTrue(filter.matches(job(tags = listOf("kotlin", "jvm"))))
    }

    @Test
    fun `tag constraint rejects when no tag overlaps`() {
        val filter = JobFilter(tags = setOf("rust"))
        assertFalse(filter.matches(job(tags = listOf("kotlin", "android"))))
    }

    // --- matches: combined ---

    @Test
    fun `all constraints must pass`() {
        val filter = JobFilter(
            types = setOf(JobType.FULL_TIME),
            remoteOnly = true,
            location = "Berlin",
            minSalary = 50,
            tags = setOf("kotlin")
        )
        assertTrue(
            filter.matches(
                job(type = JobType.FULL_TIME, remote = true, location = "Berlin", salaryMax = 80, tags = listOf("kotlin"))
            )
        )
        // fails a single constraint (remote) -> rejected
        assertFalse(
            filter.matches(
                job(type = JobType.FULL_TIME, remote = false, location = "Berlin", salaryMax = 80, tags = listOf("kotlin"))
            )
        )
    }
}
