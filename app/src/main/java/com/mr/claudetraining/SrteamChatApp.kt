package com.mr.claudetraining

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.mr.claudetraining.data.sync.SyncManager
import com.mr.claudetraining.domain.repository.CrashReporter
import com.mr.claudetraining.domain.repository.FeatureFlagProvider
import javax.inject.Inject

@HiltAndroidApp
class SrteamChatApp : Application() {

    @Inject lateinit var syncManager: SyncManager
    @Inject lateinit var crashReporter: CrashReporter

    // Injecting the singleton provider instantiates it, kicking off the initial
    // Remote Config fetch and registering the real-time update listener.
    @Inject lateinit var featureFlagProvider: FeatureFlagProvider

    override fun onCreate() {
        super.onCreate()
        syncManager.start()
        crashReporter.log("App started — feature flags: ${featureFlagProvider.flags.value}")
    }
}
