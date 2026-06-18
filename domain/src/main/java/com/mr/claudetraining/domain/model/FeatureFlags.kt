package com.mr.claudetraining.domain.model

/**
 * App-wide feature toggles, sourced from Firebase Remote Config.
 * Defaults are the "off" / legacy behaviour so a fresh install (or a failed
 * fetch) behaves exactly like the shipped build until Remote Config activates.
 */
data class FeatureFlags(
    val newJobDetailUi: Boolean = false,
    val newChatFeatures: Boolean = false,
    val betaFeatures: Boolean = false
) {
    companion object {
        val DEFAULTS = FeatureFlags()
    }
}
