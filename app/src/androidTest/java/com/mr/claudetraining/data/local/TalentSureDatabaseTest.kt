package com.mr.claudetraining.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TalentSureDatabaseTest {

    private lateinit var db: TalentSureDatabase
    private lateinit var jobDao: JobDao
    private lateinit var userDao: UserDao
    private lateinit var messageDao: MessageDao
    private lateinit var jobQueryDao: JobQueryDao
    private lateinit var messageDraftDao: MessageDraftDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, TalentSureDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        jobDao = db.jobDao()
        userDao = db.userDao()
        messageDao = db.messageDao()
        jobQueryDao = db.jobQueryDao()
        messageDraftDao = db.messageDraftDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun job(
        id: String,
        title: String = "Engineer",
        postedAt: Long = 0L,
        tags: List<String> = listOf("kotlin", "android")
    ) = JobEntity(
        id = id,
        title = title,
        company = "Acme",
        location = "Berlin",
        type = "FULL_TIME",
        remote = true,
        tags = tags,
        salaryMin = 50_000,
        salaryMax = 90_000,
        postedAt = postedAt,
        cachedAt = 1_000L
    )

    private fun user(id: String, name: String = "Ada") = UserEntity(
        id = id,
        name = name,
        image = "https://example.com/$id.png",
        isOnline = true,
        cachedAt = 1_000L
    )

    private fun message(
        id: String,
        channelId: String,
        createdAt: Long = 0L,
        reactions: List<ReactionData> = emptyList()
    ) = MessageEntity(
        id = id,
        channelId = channelId,
        authorId = "u1",
        text = "hello",
        createdAt = createdAt,
        isMine = false,
        reactions = reactions
    )

    private fun draft(
        localId: String,
        channelId: String = "c1",
        createdAt: Long = 0L,
        status: DraftStatus = DraftStatus.PENDING
    ) = MessageDraftEntity(
        localId = localId,
        channelId = channelId,
        text = "draft",
        createdAt = createdAt,
        status = status
    )

    // --- JobDao ---

    @Test
    fun jobInsertAndObserveRoundTrip() = runTest {
        val jobs = listOf(job("j1", postedAt = 10), job("j2", postedAt = 20))
        jobDao.upsertAll(jobs)

        // observeAll orders by postedAt DESC
        val observed = jobDao.observeAll().first()
        assertEquals(listOf("j2", "j1"), observed.map { it.id })
        // List<String> TypeConverter round-trip
        assertEquals(listOf("kotlin", "android"), observed.first { it.id == "j1" }.tags)
    }

    @Test
    fun jobObserveSingleEmitsNullThenEntity() = runTest {
        assertNull(jobDao.observe("missing").first())
        jobDao.upsertAll(listOf(job("j1", title = "Lead")))
        assertEquals("Lead", jobDao.observe("j1").first()?.title)
    }

    @Test
    fun jobUpsertReplacesOnConflict() = runTest {
        jobDao.upsertAll(listOf(job("j1", title = "Old")))
        jobDao.upsertAll(listOf(job("j1", title = "New")))

        val all = jobDao.observeAll().first()
        assertEquals(1, all.size)
        assertEquals("New", all.single().title)
    }

    @Test
    fun jobReplaceAllUpsertsAndDropsMissing() = runTest {
        jobDao.upsertAll(listOf(job("j1"), job("j2"), job("j3")))

        // replaceAll keeps only the supplied rows
        jobDao.replaceAll(listOf(job("j2", title = "Kept"), job("j4")))

        val ids = jobDao.observeAll().first().map { it.id }.toSet()
        assertEquals(setOf("j2", "j4"), ids)
        assertEquals("Kept", jobDao.observe("j2").first()?.title)
    }

    @Test
    fun jobDeleteNotInKeepsOnlyListed() = runTest {
        jobDao.upsertAll(listOf(job("j1"), job("j2"), job("j3")))
        jobDao.deleteNotIn(listOf("j2"))

        assertEquals(listOf("j2"), jobDao.observeAll().first().map { it.id })
    }

    // --- UserDao ---

    @Test
    fun userInsertGetByIdAndObserve() = runTest {
        userDao.upsertAll(listOf(user("u1", "Ada"), user("u2", "Grace")))

        assertEquals("Ada", userDao.getById("u1")?.name)
        assertNull(userDao.getById("nope"))
        assertEquals(2, userDao.observeAll().first().size)
    }

    @Test
    fun userUpsertReplacesOnConflict() = runTest {
        userDao.upsertAll(listOf(user("u1", "Ada")))
        userDao.upsertAll(listOf(user("u1", "Ada Lovelace")))

        assertEquals("Ada Lovelace", userDao.getById("u1")?.name)
        assertEquals(1, userDao.observeAll().first().size)
    }

    // --- MessageDao ---

    @Test
    fun messageObserveByChannelOrdersByCreatedAtAndIsScoped() = runTest {
        messageDao.upsertAll(
            listOf(
                message("m2", "c1", createdAt = 20),
                message("m1", "c1", createdAt = 10),
                message("mX", "c2", createdAt = 5)
            )
        )

        val c1 = messageDao.observeByChannel("c1").first()
        assertEquals(listOf("m1", "m2"), c1.map { it.id })
        assertEquals(listOf("mX"), messageDao.observeByChannel("c2").first().map { it.id })
    }

    @Test
    fun messageReactionsConverterRoundTrip() = runTest {
        val reactions = listOf(ReactionData("👍", 3), ReactionData("🎉", 1))
        messageDao.upsertAll(listOf(message("m1", "c1", reactions = reactions)))

        assertEquals(reactions, messageDao.observeByChannel("c1").first().single().reactions)
    }

    @Test
    fun messageClearChannelRemovesOnlyThatChannel() = runTest {
        messageDao.upsertAll(listOf(message("m1", "c1"), message("mX", "c2")))
        messageDao.clearChannel("c1")

        assertTrue(messageDao.observeByChannel("c1").first().isEmpty())
        assertEquals(1, messageDao.observeByChannel("c2").first().size)
    }

    // --- JobQueryDao ---

    @Test
    fun jobQueryUpsertGetByKeyAndSearchItemsConverter() = runTest {
        val items = listOf(CachedSearchItem("j1", 90), CachedSearchItem("j2", 40))
        val entity = JobQueryEntity(
            queryKey = "k1",
            queryText = "android dev",
            items = items,
            cachedAt = 500L
        )
        jobQueryDao.upsert(entity)

        val loaded = jobQueryDao.getByKey("k1")
        assertEquals("android dev", loaded?.queryText)
        assertEquals(items, loaded?.items)
        assertNull(jobQueryDao.getByKey("absent"))
    }

    @Test
    fun jobQueryUpsertReplacesOnConflict() = runTest {
        jobQueryDao.upsert(
            JobQueryEntity("k1", "old", listOf(CachedSearchItem("j1", 10)), cachedAt = 100L)
        )
        jobQueryDao.upsert(
            JobQueryEntity("k1", "new", listOf(CachedSearchItem("j2", 20)), cachedAt = 200L)
        )

        val loaded = jobQueryDao.getByKey("k1")
        assertEquals("new", loaded?.queryText)
        assertEquals(listOf(CachedSearchItem("j2", 20)), loaded?.items)
    }

    @Test
    fun jobQueryDeleteStaleEvictsByCachedAt() = runTest {
        jobQueryDao.upsert(JobQueryEntity("fresh", "f", emptyList(), cachedAt = 1_000L))
        jobQueryDao.upsert(JobQueryEntity("stale", "s", emptyList(), cachedAt = 100L))

        jobQueryDao.deleteStale(staleBefore = 500L)

        assertNull(jobQueryDao.getByKey("stale"))
        assertEquals("f", jobQueryDao.getByKey("fresh")?.queryText)
    }

    // --- MessageDraftDao ---

    @Test
    fun draftInsertReplaceAndObserveByChannel() = runTest {
        messageDraftDao.upsert(draft("d2", channelId = "c1", createdAt = 20))
        messageDraftDao.upsert(draft("d1", channelId = "c1", createdAt = 10))
        messageDraftDao.upsert(draft("dX", channelId = "c2", createdAt = 5))

        val c1 = messageDraftDao.observeByChannel("c1").first()
        assertEquals(listOf("d1", "d2"), c1.map { it.localId })
        assertEquals(1, messageDraftDao.observeByChannel("c2").first().size)
    }

    @Test
    fun draftUpsertReplacesOnConflict() = runTest {
        messageDraftDao.upsert(draft("d1", status = DraftStatus.PENDING))
        messageDraftDao.upsert(draft("d1", status = DraftStatus.SENT))

        val drafts = messageDraftDao.observeByChannel("c1").first()
        assertEquals(1, drafts.size)
        // DraftStatus TypeConverter round-trip
        assertEquals(DraftStatus.SENT, drafts.single().status)
    }

    @Test
    fun draftPendingReturnsPendingAndFailedOldestFirst() = runTest {
        messageDraftDao.upsert(draft("d1", createdAt = 30, status = DraftStatus.PENDING))
        messageDraftDao.upsert(draft("d2", createdAt = 10, status = DraftStatus.FAILED))
        messageDraftDao.upsert(draft("d3", createdAt = 20, status = DraftStatus.SENT))
        messageDraftDao.upsert(draft("d4", createdAt = 5, status = DraftStatus.SENDING))

        val pending = messageDraftDao.pending()
        assertEquals(listOf("d2", "d1"), pending.map { it.localId })
    }

    @Test
    fun draftUpdateStatusChangesStatus() = runTest {
        messageDraftDao.upsert(draft("d1", status = DraftStatus.PENDING))
        messageDraftDao.updateStatus("d1", DraftStatus.SENT)

        assertEquals(DraftStatus.SENT, messageDraftDao.observeByChannel("c1").first().single().status)
        assertTrue(messageDraftDao.pending().isEmpty())
    }

    @Test
    fun draftMarkFailedSetsStatusBumpsAttemptAndStoresError() = runTest {
        messageDraftDao.upsert(draft("d1", status = DraftStatus.SENDING))

        messageDraftDao.markFailed("d1", "network down")

        val updated = messageDraftDao.observeByChannel("c1").first().single()
        assertEquals(DraftStatus.FAILED, updated.status)
        assertEquals(1, updated.attemptCount)
        assertEquals("network down", updated.lastError)
        // FAILED rows are picked up again by the sync worker
        assertEquals(listOf("d1"), messageDraftDao.pending().map { it.localId })
    }

    @Test
    fun draftDeleteRemovesRow() = runTest {
        messageDraftDao.upsert(draft("d1"))
        messageDraftDao.delete("d1")

        assertTrue(messageDraftDao.observeByChannel("c1").first().isEmpty())
        assertFalse(messageDraftDao.pending().any { it.localId == "d1" })
    }
}
