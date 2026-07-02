package com.mr.claudetraining.data.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.mr.claudetraining.domain.model.AnalyticsEvent
import com.mr.claudetraining.domain.repository.AnalyticsLogger
import javax.inject.Inject
import javax.inject.Singleton

/** Firebase-backed [AnalyticsLogger]: translates a typed [AnalyticsEvent] into one logEvent. */
@Singleton
class FirebaseAnalyticsLogger @Inject constructor(
    private val analytics: FirebaseAnalytics
) : AnalyticsLogger {

    override fun log(event: AnalyticsEvent) {
        analytics.logEvent(event.name, event.params.toBundle())
    }

    override fun setUserId(id: String?) = analytics.setUserId(id)

    override fun setUserProperty(name: String, value: String?) =
        analytics.setUserProperty(name, value)

    // Firebase params accept String/Long/Double only; Boolean is stored as 1/0.
    private fun Map<String, Any?>.toBundle() = Bundle().apply {
        forEach { (key, value) ->
            when (value) {
                null -> {}
                is String -> putString(key, value)
                is Int -> putLong(key, value.toLong())
                is Long -> putLong(key, value)
                is Double -> putDouble(key, value)
                is Float -> putDouble(key, value.toDouble())
                is Boolean -> putLong(key, if (value) 1L else 0L)
                else -> putString(key, value.toString())
            }
        }
    }
}
