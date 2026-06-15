package com.mr.claudetraining.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardMapperTest {

    @Test
    fun `UserProfileDto toDomain maps every field`() {
        val domain = UserProfileDto(
            name = "Mala",
            memberSince = "2024",
            sessions = 42,
            dayStreak = 7,
            badges = 3
        ).toDomain()

        assertEquals("Mala", domain.name)
        assertEquals("2024", domain.memberSince)
        assertEquals(42, domain.sessions)
        assertEquals(7, domain.dayStreak)
        assertEquals(3, domain.badges)
    }

    @Test
    fun `UserProfileDto toDomain uses defaults`() {
        val domain = UserProfileDto().toDomain()
        assertEquals("", domain.name)
        assertEquals(0, domain.sessions)
        assertEquals(0, domain.dayStreak)
        assertEquals(0, domain.badges)
    }

    @Test
    fun `DailyGoalDto toDomain maps every field`() {
        val domain = DailyGoalDto(completed = 3, total = 5).toDomain()
        assertEquals(3, domain.completed)
        assertEquals(5, domain.total)
    }

    @Test
    fun `DailyGoalDto toDomain uses defaults`() {
        val domain = DailyGoalDto().toDomain()
        assertEquals(0, domain.completed)
        assertEquals(0, domain.total)
    }
}
