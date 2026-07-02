package com.mr.claudetraining.domain.repository

/**
 * Performance-trace abstraction (backed by Firebase Performance Monitoring). Framework-free
 * so timing can be added in ViewModels/repositories and stubbed in unit tests.
 *
 * App-start time, screen rendering, and network requests are captured automatically by the
 * Performance SDK — these custom traces cover the flows the SDK can't see on its own.
 */
interface PerformanceTracer {
    /** Creates a stopped trace; call [PerfTrace.start] to begin timing. */
    fun newTrace(name: String): PerfTrace

    /** Times [block], stopping the trace even if it throws. */
    suspend fun <T> trace(name: String, block: suspend () -> T): T {
        val trace = newTrace(name).also { it.start() }
        try {
            return block()
        } finally {
            trace.stop()
        }
    }

    companion object {
        const val JOB_LIST_LOAD = "job_list_load"
        const val CHAT_MESSAGE_SEND = "chat_message_send"
    }
}

/** A single timing span. Wall-clock duration is measured between [start] and [stop]. */
interface PerfTrace {
    fun start()
    fun putMetric(name: String, value: Long)
    fun incrementMetric(name: String, by: Long = 1)
    fun putAttribute(name: String, value: String)
    fun stop()
}
