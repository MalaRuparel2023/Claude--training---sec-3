package com.mr.claudetraining.data.crash

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mr.claudetraining.domain.repository.CrashReporter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashlyticsReporter @Inject constructor(
    private val crashlytics: FirebaseCrashlytics
) : CrashReporter {

    override fun log(message: String) = crashlytics.log(message)

    override fun setCustomKey(key: String, value: String) = crashlytics.setCustomKey(key, value)

    override fun recordNonFatal(throwable: Throwable) = crashlytics.recordException(throwable)

    override fun forceTestCrash(): Nothing =
        throw RuntimeException("Test crash triggered from the Config screen")
}
