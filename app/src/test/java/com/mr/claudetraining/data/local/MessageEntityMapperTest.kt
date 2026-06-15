package com.mr.claudetraining.data.local

import com.mr.claudetraining.domain.model.ChatMessage
import com.mr.claudetraining.domain.model.ChatUser
import com.mr.claudetraining.domain.model.MessageReaction
import org.junit.Assert.assertEquals
import org.junit.Test

class MessageEntityMapperTest {

    private val author = ChatUser(id = "u1", name = "Mala", image = "img", isOnline = true)

    @Test
    fun `toDomain maps fields and injects supplied author`() {
        val entity = MessageEntity(
            id = "m1",
            channelId = "c1",
            authorId = "u1",
            text = "hello",
            createdAt = 1000L,
            isMine = true,
            reactions = listOf(ReactionData("👍", 2), ReactionData("🔥", 1))
        )

        val domain = entity.toDomain(author)

        assertEquals("m1", domain.id)
        assertEquals("hello", domain.text)
        assertEquals(author, domain.author)
        assertEquals(1000L, domain.createdAt)
        assertEquals(true, domain.isMine)
        assertEquals(
            listOf(MessageReaction("👍", 2), MessageReaction("🔥", 1)),
            domain.reactions
        )
    }

    @Test
    fun `toDomain maps empty reactions`() {
        val entity = MessageEntity("m2", "c", "u1", "hi", 1L, false, emptyList())
        assertEquals(emptyList<MessageReaction>(), entity.toDomain(author).reactions)
    }

    @Test
    fun `toEntity maps fields scopes channel and flattens reactions`() {
        val message = ChatMessage(
            id = "m3",
            text = "yo",
            author = author,
            createdAt = 50L,
            isMine = false,
            reactions = listOf(MessageReaction("❤️", 5))
        )

        val entity = message.toEntity(channelId = "chan9")

        assertEquals("m3", entity.id)
        assertEquals("chan9", entity.channelId)
        assertEquals("u1", entity.authorId)
        assertEquals("yo", entity.text)
        assertEquals(50L, entity.createdAt)
        assertEquals(false, entity.isMine)
        assertEquals(listOf(ReactionData("❤️", 5)), entity.reactions)
    }

    @Test
    fun `message round-trips through entity preserving author and reactions`() {
        val message = ChatMessage(
            id = "rt",
            text = "round",
            author = author,
            createdAt = 7L,
            isMine = true,
            reactions = listOf(MessageReaction("😀", 3))
        )

        val rebuilt = message.toEntity("chan").toDomain(author)
        assertEquals(message, rebuilt)
    }
}
