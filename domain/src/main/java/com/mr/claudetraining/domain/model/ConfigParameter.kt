package com.mr.claudetraining.domain.model

/** A single Remote Config key/value plus where the active value came from. */
data class ConfigParameter(
    val key: String,
    val value: String,
    val source: ConfigSource
)

enum class ConfigSource { REMOTE, DEFAULT, STATIC }
