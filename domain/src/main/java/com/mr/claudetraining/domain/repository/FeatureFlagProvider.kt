package com.mr.claudetraining.domain.repository

import com.mr.claudetraining.domain.model.ConfigParameter
import com.mr.claudetraining.domain.model.FeatureFlags
import kotlinx.coroutines.flow.StateFlow

/**
 * Single source of truth for feature flags. Implementations back this with
 * Firebase Remote Config and keep [flags] live so the UI updates when values
 * change server-side — no app redeploy required.
 */
interface FeatureFlagProvider {

    /** Current flags; emits a new value whenever Remote Config activates. */
    val flags: StateFlow<FeatureFlags>

    /** Fetch + activate the latest config. Returns true if values changed. */
    suspend fun refresh(): Boolean

    /** All Remote Config parameters (for the dev/QA Config screen). */
    fun parameters(): List<ConfigParameter>
}
