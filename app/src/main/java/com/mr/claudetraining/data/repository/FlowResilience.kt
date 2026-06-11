package com.mr.claudetraining.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retryWhen
import java.io.IOException
import kotlin.math.pow

private const val TAG = "JobFlows"

/**
 * Resilience operators for Firestore-backed streams. A snapshot listener surfaces failures
 * by completing the flow with an exception; left unhandled it crashes the collector. These
 * extensions add the three standard recovery stages:
 *
 *  1. [retryTransient] — re-subscribe on recoverable errors with exponential backoff.
 *  2. [fallbackTo] — after retries are exhausted, swallow the error and emit a safe default.
 *
 * Compose them source-first so every downstream operator inherits the behaviour:
 * `source.retryTransient().fallbackTo(emptyList())`.
 */

/** Transient errors worth retrying — network blips and Firestore's recoverable codes. */
fun Throwable.isTransient(): Boolean = when (this) {
    is IOException -> true
    is FirebaseFirestoreException -> code in TRANSIENT_CODES
    else -> false
}

private val TRANSIENT_CODES = setOf(
    Code.UNAVAILABLE,
    Code.DEADLINE_EXCEEDED,
    Code.ABORTED,
    Code.INTERNAL,
    Code.RESOURCE_EXHAUSTED
)

/**
 * Re-subscribes to the upstream when it fails with a [transient][isTransient] error, backing
 * off exponentially (capped) between attempts. Non-transient errors (e.g. PERMISSION_DENIED)
 * propagate immediately — retrying them is pointless.
 */
fun <T> Flow<T>.retryTransient(
    maxAttempts: Long = 3,
    initialDelayMs: Long = 500,
    maxDelayMs: Long = 5_000,
    factor: Double = 2.0
): Flow<T> = retryWhen { cause, attempt ->
    val shouldRetry = attempt < maxAttempts && cause.isTransient()
    if (shouldRetry) {
        val backoff = (initialDelayMs * factor.pow(attempt.toInt())).toLong().coerceAtMost(maxDelayMs)
        Log.w(TAG, "transient stream failure (attempt ${attempt + 1}/$maxAttempts), retrying in ${backoff}ms", cause)
        delay(backoff)
    }
    shouldRetry
}

/** Terminal stage: log whatever escaped the retries and emit [fallback] so collectors survive. */
fun <T> Flow<T>.fallbackTo(fallback: T): Flow<T> = catch { cause ->
    Log.e(TAG, "stream failed after retries, emitting fallback", cause)
    emit(fallback)
}