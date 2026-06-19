package com.mr.claudetraining.data.messaging

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ro.alexmamo.firebasesigninwithemailandpassword.R
import com.mr.claudetraining.domain.repository.CrashReporter
import com.mr.claudetraining.domain.repository.PushTokenRepository
import javax.inject.Inject

@AndroidEntryPoint
class AppFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var pushTokenRepository: PushTokenRepository
    @Inject lateinit var crashReporter: CrashReporter

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Fired on first install and whenever FCM rotates the token. Register the
    // fresh token so the backend keeps targeting this device.
    override fun onNewToken(token: String) {
        Log.d("FCM_TOKEN", "FCM token: $token")
        scope.launch { pushTokenRepository.registerToken(token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        ensureChannel()
        val notification = message.notification
        val title = notification?.title ?: message.data["title"] ?: getString(R.string.app_name)
        val body = notification?.body ?: message.data["body"].orEmpty()
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        // Android 13+ won't display the notification without POST_NOTIFICATIONS.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val builder = NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            // HIGH + alarm/notification defaults → heads-up banner shown even while the app is open.
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        NotificationManagerCompat.from(this)
            .notify(System.identityHashCode(body), builder.build())
    }

    private fun ensureChannel() {
        val channelId = getString(R.string.default_notification_channel_id)
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(channelId) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    getString(R.string.default_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
    }
}
