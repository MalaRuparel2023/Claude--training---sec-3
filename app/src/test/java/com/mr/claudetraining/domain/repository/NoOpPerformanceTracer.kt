package com.mr.claudetraining.domain.repository

/** Test double: runs traced blocks without touching Firebase. */
object NoOpPerformanceTracer : PerformanceTracer {
    override fun newTrace(name: String): PerfTrace = object : PerfTrace {
        override fun start() {}
        override fun putMetric(name: String, value: Long) {}
        override fun incrementMetric(name: String, by: Long) {}
        override fun putAttribute(name: String, value: String) {}
        override fun stop() {}
    }
}
