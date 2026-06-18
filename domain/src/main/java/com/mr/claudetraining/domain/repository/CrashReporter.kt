package com.mr.claudetraining.domain.repository

/** Crash + non-fatal reporting abstraction (backed by Firebase Crashlytics). */
interface CrashReporter {
    fun log(message: String)
    fun setCustomKey(key: String, value: String)
    fun recordNonFatal(throwable: Throwable)

    /** Throws on purpose — used from the dev/QA Config screen to verify reporting. */
    fun forceTestCrash()
}
