package com.mr.claudetraining.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.mr.claudetraining.domain.model.AnalyticsEvent
import com.mr.claudetraining.domain.repository.AnalyticsLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Hosts app-wide analytics calls that don't belong to a feature ViewModel — currently the
 * manual screen-view tracking driven from [com.mr.claudetraining.ui.navigation] (Compose has
 * no Activities for Firebase to auto-track).
 */
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analytics: AnalyticsLogger
) : ViewModel() {
    fun logScreenView(screen: String) = analytics.log(AnalyticsEvent.ScreenView(screen))
}
