package com.mr.claudetraining.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainComputedPropertiesTest {

    // --- JobSearchResult ---

    @Test
    fun `empty JobSearchResult reports zero count and isEmpty`() {
        val result = JobSearchResult()
        assertEquals(0, result.totalCount)
        assertTrue(result.isEmpty)
    }

    @Test
    fun `populated JobSearchResult reports item count and not empty`() {
        val item = JobSearchItem(job = Job(id = "1"), score = 5, saved = false)
        val result = JobSearchResult(query = "kotlin", items = listOf(item, item))
        assertEquals(2, result.totalCount)
        assertFalse(result.isEmpty)
    }

    // --- DailyNutrition.net ---

    @Test
    fun `net nutrition is consumed minus burned`() {
        val nutrition = DailyNutrition(caloriesConsumed = 2000, caloriesBurned = 500)
        assertEquals(1500, nutrition.net)
    }

    @Test
    fun `net nutrition can be negative when burned exceeds consumed`() {
        val nutrition = DailyNutrition(caloriesConsumed = 200, caloriesBurned = 500)
        assertEquals(-300, nutrition.net)
    }

    @Test
    fun `net nutrition defaults to zero`() {
        assertEquals(0, DailyNutrition().net)
    }

    // --- DailyGoal.progress ---

    @Test
    fun `progress is zero when total is zero`() {
        assertEquals(0f, DailyGoal(completed = 0, total = 0).progress, 0f)
        assertEquals(0f, DailyGoal(completed = 5, total = 0).progress, 0f)
    }

    @Test
    fun `progress is fraction of completed over total`() {
        assertEquals(0.5f, DailyGoal(completed = 1, total = 2).progress, 1e-6f)
        assertEquals(1f, DailyGoal(completed = 3, total = 3).progress, 1e-6f)
    }
}
