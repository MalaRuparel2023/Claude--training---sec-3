package com.mr.claudetraining.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    // ---- String list ----

    @Test
    fun `string list round-trips`() {
        val list = listOf("kotlin", "android", "compose")
        assertEquals(list, converters.toStringList(converters.fromStringList(list)))
    }

    @Test
    fun `empty string list round-trips`() {
        assertEquals(emptyList<String>(), converters.toStringList(converters.fromStringList(emptyList())))
    }

    @Test
    fun `toStringList returns empty for blank input`() {
        assertEquals(emptyList<String>(), converters.toStringList(""))
        assertEquals(emptyList<String>(), converters.toStringList("   "))
    }

    // ---- Reactions ----

    @Test
    fun `reactions round-trip`() {
        val list = listOf(ReactionData("👍", 2), ReactionData("🔥", 1))
        assertEquals(list, converters.toReactions(converters.fromReactions(list)))
    }

    @Test
    fun `empty reactions round-trip`() {
        assertEquals(emptyList<ReactionData>(), converters.toReactions(converters.fromReactions(emptyList())))
    }

    @Test
    fun `toReactions returns empty for blank input`() {
        assertEquals(emptyList<ReactionData>(), converters.toReactions(""))
        assertEquals(emptyList<ReactionData>(), converters.toReactions("  "))
    }

    // ---- Search items ----

    @Test
    fun `search items round-trip`() {
        val list = listOf(CachedSearchItem("a", 10), CachedSearchItem("b", 5))
        assertEquals(list, converters.toSearchItems(converters.fromSearchItems(list)))
    }

    @Test
    fun `empty search items round-trip`() {
        assertEquals(emptyList<CachedSearchItem>(), converters.toSearchItems(converters.fromSearchItems(emptyList())))
    }

    @Test
    fun `toSearchItems returns empty for blank input`() {
        assertEquals(emptyList<CachedSearchItem>(), converters.toSearchItems(""))
    }

    @Test
    fun `toSearchItems ignores unknown keys`() {
        val json = """[{"jobId":"x","score":3,"extra":"ignored"}]"""
        assertEquals(listOf(CachedSearchItem("x", 3)), converters.toSearchItems(json))
    }

    // ---- DraftStatus ----

    @Test
    fun `draft status round-trips for every value`() {
        DraftStatus.values().forEach { status ->
            assertEquals(status, converters.toDraftStatus(converters.fromDraftStatus(status)))
        }
    }

    @Test
    fun `toDraftStatus falls back to PENDING for unknown value`() {
        assertEquals(DraftStatus.PENDING, converters.toDraftStatus("BOGUS"))
    }

    @Test
    fun `toDraftStatus falls back to PENDING for empty value`() {
        assertEquals(DraftStatus.PENDING, converters.toDraftStatus(""))
    }
}
