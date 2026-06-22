package com.mr.claudetraining

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import ro.alexmamo.firebasesigninwithemailandpassword.R
import com.mr.claudetraining.data.sync.SyncManager
import com.mr.claudetraining.domain.model.ChatConnectionState
import com.mr.claudetraining.domain.repository.ChatRepository
import com.mr.claudetraining.domain.repository.CrashReporter
import com.mr.claudetraining.domain.repository.FeatureFlagProvider
import javax.inject.Inject

@HiltAndroidApp
class SrteamChatApp : Application() {

    @Inject lateinit var syncManager: SyncManager
    @Inject lateinit var crashReporter: CrashReporter
    @Inject lateinit var chatRepository: ChatRepository

    // Injecting the singleton provider instantiates it, kicking off the initial
    // Remote Config fetch and registering the real-time update listener.
    @Inject lateinit var featureFlagProvider: FeatureFlagProvider

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        syncManager.start()
        observeChatLifecycle()
        crashReporter.log("App started — feature flags: ${featureFlagProvider.flags.value}")
    }

    // Tie the Stream connection to the whole-app foreground/background lifecycle:
    // connect when the app comes to the foreground, release the socket when it leaves.
    // An explicit sign-out (SignedOut) is left untouched. Network drops while in the
    // foreground are handled by Stream's own auto-reconnect.
    private fun observeChatLifecycle() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                if (chatRepository.connectionState.value !is ChatConnectionState.SignedOut) {
                    chatRepository.connect()
                }
            }

            override fun onStop(owner: LifecycleOwner) {
                if (chatRepository.connectionState.value !is ChatConnectionState.SignedOut) {
                    chatRepository.disconnect(clearData = false)
                }
            }
        })
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
