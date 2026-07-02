package com.mr.claudetraining.data.analytics

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.mr.claudetraining.domain.repository.PerfTrace
import com.mr.claudetraining.domain.repository.PerformanceTracer
import javax.inject.Inject
import javax.inject.Singleton

/** Firebase-backed [PerformanceTracer]; wraps a Firebase [Trace] per timing span. */
@Singleton
class FirebasePerformanceTracer @Inject constructor(
    private val performance: FirebasePerformance
) : PerformanceTracer {

    override fun newTrace(name: String): PerfTrace = FirebasePerfTrace(performance.newTrace(name))
}

private class FirebasePerfTrace(private val trace: Trace) : PerfTrace {
    override fun start() = trace.start()
    override fun putMetric(name: String, value: Long) = trace.putMetric(name, value)
    override fun incrementMetric(name: String, by: Long) = trace.incrementMetric(name, by)
    override fun putAttribute(name: String, value: String) = trace.putAttribute(name, value)
    override fun stop() = trace.stop()
}
