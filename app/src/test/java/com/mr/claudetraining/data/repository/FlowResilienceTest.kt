package com.mr.claudetraining.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

// android.util.Log calls inside the resilience operators are no-ops here:
// testOptions { unitTests.returnDefaultValues = true } in app/build.gradle.
@OptIn(ExperimentalCoroutinesApi::class)
class FlowResilienceTest {

    // --- isTransient ---

    @Test
    fun `IOException is transient`() {
        assertTrue(IOException("boom").isTransient())
    }

    @Test
    fun `generic exceptions are not transient`() {
        assertFalse(RuntimeException("nope").isTransient())
        assertFalse(IllegalStateException().isTransient())
    }

    // --- retryTransient ---

    @Test
    fun `successful flow passes through without retrying`() = runTest {
        val result = flowOf(1, 2, 3).retryTransient().toList()
        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun `transient failure is retried then succeeds`() = runTest {
        val attempts = AtomicInteger(0)
        val flow = flow {
            val n = attempts.incrementAndGet()
            if (n < 3) throw IOException("transient")
            emit("ok")
        }

        val result = flow.retryTransient(maxAttempts = 5).toList()

        assertEquals(listOf("ok"), result)
        assertEquals(3, attempts.get())
    }

    @Test
    fun `retry stops after maxAttempts and rethrows transient error`() = runTest {
        val attempts = AtomicInteger(0)
        val flow = flow<Int> {
            attempts.incrementAndGet()
            throw IOException("always")
        }

        var caught: Throwable? = null
        try {
            flow.retryTransient(maxAttempts = 3).toList()
        } catch (e: IOException) {
            caught = e
        }

        assertTrue(caught is IOException)
        // initial attempt + 3 retries = 4 subscriptions
        assertEquals(4, attempts.get())
    }

    @Test
    fun `non-transient error propagates immediately without retrying`() = runTest {
        val attempts = AtomicInteger(0)
        val flow = flow<Int> {
            attempts.incrementAndGet()
            throw IllegalStateException("fatal")
        }

        var caught: Throwable? = null
        try {
            flow.retryTransient(maxAttempts = 3).toList()
        } catch (e: IllegalStateException) {
            caught = e
        }

        assertTrue(caught is IllegalStateException)
        assertEquals(1, attempts.get())
    }

    @Test
    fun `backoff grows exponentially and is capped at maxDelayMs`() = runTest {
        val flow = flow<Int> { throw IOException("transient") }

        flow.retryTransient(
            maxAttempts = 4,
            initialDelayMs = 500,
            maxDelayMs = 1_000,
            factor = 2.0
        ).catchAll().toList()

        // delays: attempt0 -> 500, attempt1 -> 1000, attempt2 -> capped 1000, attempt3 -> capped 1000
        // total virtual time = 500 + 1000 + 1000 + 1000 = 3500
        assertEquals(3_500, currentTime)
    }

    // --- fallbackTo ---

    @Test
    fun `fallbackTo passes successful values through`() = runTest {
        val result = flowOf(listOf("a"), listOf("b")).fallbackTo(emptyList()).toList()
        assertEquals(listOf(listOf("a"), listOf("b")), result)
    }

    @Test
    fun `fallbackTo emits fallback after upstream failure`() = runTest {
        val flow = flow<List<String>> { throw RuntimeException("down") }
        val result = flow.fallbackTo(listOf("default")).toList()
        assertEquals(listOf(listOf("default")), result)
    }

    @Test
    fun `fallbackTo emits prior values then fallback on later failure`() = runTest {
        val flow = flow<Int> {
            emit(1)
            emit(2)
            throw IOException("boom")
        }
        val result = flow.fallbackTo(-1).toList()
        assertEquals(listOf(1, 2, -1), result)
    }

    @Test
    fun `retryTransient composed with fallbackTo survives exhausted transient errors`() = runTest {
        val flow = flow<Set<String>> { throw IOException("transient") }

        val result = flow.retryTransient(maxAttempts = 2).fallbackTo(emptySet()).toList()

        assertEquals(listOf(emptySet<String>()), result)
    }

    private fun <T> Flow<T>.catchAll(): Flow<T> =
        catch { /* swallow to let backoff timing assertions run */ }
}
