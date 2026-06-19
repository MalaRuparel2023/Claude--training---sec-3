package com.mr.claudetraining

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp
import ro.alexmamo.firebasesigninwithemailandpassword.R
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
        createNotificationChannel()
        syncManager.start()
        crashReporter.log("App started — feature flags: ${featureFlagProvider.flags.value}")
    }

    // Registered up front so FCM notifications received while the app is backgrounded
    // (handled by the system, not our service) land on the correct channel.
    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channelId = getString(R.string.default_notification_channel_id)
        if (manager.getNotificationChannel(channelId) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    getString(R.string.default_notification_channel_name),
                    // HIGH so notifications pop as a heads-up banner over the app (foreground).
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
    }
}
