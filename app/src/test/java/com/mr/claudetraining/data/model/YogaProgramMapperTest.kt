package com.mr.claudetraining.data.model

import com.mr.claudetraining.domain.model.YogaProgram
import org.junit.Assert.assertEquals
import org.junit.Test

class YogaProgramMapperTest {

    @Test
    fun `toDomain maps every dto field`() {
        val domain = YogaProgramDto(
            id = "y1",
            title = "Morning Flow",
            subtitle = "Wake up",
            level = "Beginner",
            durationMinutes = 20,
            imageUrl = "img",
            videoUrl = "vid"
        ).toDomain()

        assertEquals("y1", domain.id)
        assertEquals("Morning Flow", domain.title)
        assertEquals("Wake up", domain.subtitle)
        assertEquals("Beginner", domain.level)
        assertEquals(20, domain.durationMinutes)
        assertEquals("img", domain.imageUrl)
        assertEquals("vid", domain.videoUrl)
    }

    @Test
    fun `toDomain leaves dto-absent fields at domain defaults`() {
        val domain = YogaProgramDto(id = "y2").toDomain()
        // category and calories have no DTO source, so they stay at domain defaults.
        assertEquals("", domain.category)
        assertEquals(0, domain.calories)
    }

    @Test
    fun `toDomain on default dto yields YogaProgram defaults`() {
        assertEquals(YogaProgram(), YogaProgramDto().toDomain())
    }
}
