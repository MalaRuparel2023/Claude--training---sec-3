package com.mr.claudetraining.data.local

import com.mr.claudetraining.domain.model.ChatUser
import org.junit.Assert.assertEquals
import org.junit.Test

class UserEntityMapperTest {

    @Test
    fun `toDomain maps fields and drops cachedAt`() {
        val entity = UserEntity(
            id = "u1",
            name = "Mala",
            image = "img",
            isOnline = true,
            cachedAt = 999L
        )

        val domain = entity.toDomain()

        assertEquals("u1", domain.id)
        assertEquals("Mala", domain.name)
        assertEquals("img", domain.image)
        assertEquals(true, domain.isOnline)
    }

    @Test
    fun `toEntity maps fields and stamps cachedAt`() {
        val user = ChatUser(id = "u2", name = "Bob", image = "av", isOnline = false)

        val entity = user.toEntity(cachedAt = 1234L)

        assertEquals("u2", entity.id)
        assertEquals("Bob", entity.name)
        assertEquals("av", entity.image)
        assertEquals(false, entity.isOnline)
        assertEquals(1234L, entity.cachedAt)
    }

    @Test
    fun `user round-trips through entity`() {
        val user = ChatUser(id = "rt", name = "n", image = "i", isOnline = true)
        assertEquals(user, user.toEntity(cachedAt = 0L).toDomain())
    }
}
