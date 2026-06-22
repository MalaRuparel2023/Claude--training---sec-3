package com.mr.claudetraining.data.messaging

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
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
import com.mr.claudetraining.MainActivity
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
        val kind = NotificationKind.fromType(message.data["type"])
        ensureChannel(kind)
        val notification = message.notification
        val title = notification?.title ?: message.data["title"] ?: getString(R.string.app_name)
        val body = notification?.body ?: message.data["body"].orEmpty()
        showNotification(kind, title, body, message.data["channelId"])
    }

    private fun showNotification(kind: NotificationKind, title: String, body: String, channelId: String?) {
        // Android 13+ won't display the notification without POST_NOTIFICATIONS.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val builder = NotificationCompat.Builder(this, getString(kind.channelIdRes))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(contentIntent(kind, channelId))
            // HIGH + alarm/notification defaults → heads-up banner shown even while the app is open.
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        NotificationManagerCompat.from(this)
            .notify(System.identityHashCode(body), builder.build())
    }

    // Tapping the notification opens the app and routes to the screen that owns this kind.
    private fun contentIntent(kind: NotificationKind, channelId: String?): PendingIntent {
        val route = if (kind == NotificationKind.MESSAGE && channelId != null) {
            "chat_detail/$channelId"
        } else {
            kind.route
        }
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NAV_ROUTE, route)
        }
        return PendingIntent.getActivity(
            this,
            route.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun ensureChannel(kind: NotificationKind) {
        val channelId = getString(kind.channelIdRes)
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(channelId) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    getString(kind.channelNameRes),
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
    }

    // Each push carries a `type` data field; it selects the status-bar channel and
    // the in-app destination opened on tap. Unknown/absent types fall back to General.
    private enum class NotificationKind(
        val channelIdRes: Int,
        val channelNameRes: Int,
        val route: String
    ) {
        MESSAGE(
            R.string.messages_notification_channel_id,
            R.string.messages_notification_channel_name,
            "chat_list"
        ),
        JOB_ALERT(
            R.string.job_alerts_notification_channel_id,
            R.string.job_alerts_notification_channel_name,
            "notifications"
        ),
        GENERAL(
            R.string.default_notification_channel_id,
            R.string.default_notification_channel_name,
            "home"
        );

        companion object {
            fun fromType(type: String?): NotificationKind = when (type) {
                "message" -> MESSAGE
                "job_alert" -> JOB_ALERT
                else -> GENERAL
            }
        }
    }

    companion object {
        const val EXTRA_NAV_ROUTE = "nav_route"
    }
}
