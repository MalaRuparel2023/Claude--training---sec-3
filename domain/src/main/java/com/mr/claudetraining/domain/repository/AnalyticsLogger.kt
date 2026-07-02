package com.mr.claudetraining.domain.repository

import com.mr.claudetraining.domain.model.AnalyticsEvent

/**
 * Analytics abstraction (backed by Firebase Analytics). Framework-free so ViewModels can
 * log typed [AnalyticsEvent]s and be unit-tested with a no-op double.
 */
interface AnalyticsLogger {
    fun log(event: AnalyticsEvent)

    /** Associates subsequent events with a user; pass null on sign-out to detach. */
    fun setUserId(id: String?)

    /** A user-scoped dimension for segmenting funnels (e.g. "role" = "candidate"). */
    fun setUserProperty(name: String, value: String?)
}
