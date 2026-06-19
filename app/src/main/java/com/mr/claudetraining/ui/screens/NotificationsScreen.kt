package com.mr.claudetraining.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mr.claudetraining.ui.components.SimpleTopBar
import com.mr.claudetraining.ui.theme.FiteloGreen
import com.mr.claudetraining.ui.theme.FiteloOrange

private data class AppNotification(
    val id: Int,
    val icon: ImageVector,
    val title: String,
    val message: String,
    val time: String,
    val tint: Color,
    val section: String,
    val unread: Boolean
)

private val sampleNotifications = listOf(
    AppNotification(1, Icons.Filled.LocalFireDepartment, "Streak on fire!", "You've worked out 5 days in a row. Keep it going!", "2m ago", FiteloOrange, "Today", true),
    AppNotification(2, Icons.Filled.SelfImprovement, "Time for Surya Namaskar", "Your morning yoga reminder is here.", "1h ago", FiteloGreen, "Today", true),
    AppNotification(3, Icons.Filled.WaterDrop, "Stay hydrated", "You're 2 glasses away from your water goal.", "3h ago", Color(0xFF4C6EF5), "Today", false),
    AppNotification(4, Icons.Filled.EmojiEvents, "New badge unlocked", "You earned the “Early Bird” badge.", "Yesterday", Color(0xFFF59F00), "Earlier", false),
    AppNotification(5, Icons.Filled.Favorite, "Weekly summary", "You burned 3,200 kcal this week. Great job!", "2d ago", Color(0xFFE64980), "Earlier", false)
)

@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val notifications = remember { mutableStateListOf(*sampleNotifications.toTypedArray()) }
    val unreadCount = notifications.count { it.unread }

    fun markRead(id: Int) {
        val i = notifications.indexOfFirst { it.id == id }
        if (i >= 0 && notifications[i].unread) notifications[i] = notifications[i].copy(unread = false)
    }

    fun markAllRead() {
        notifications.indices.forEach { i ->
            if (notifications[i].unread) notifications[i] = notifications[i].copy(unread = false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SimpleTopBar(
            title = "Notifications",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (unreadCount > 0) {
                    IconButton(onClick = { markAllRead() }) {
                        Icon(Icons.Filled.DoneAll, contentDescription = "Mark all read")
                    }
                }
            }
        )

        if (notifications.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { UnreadSummary(unreadCount) }

                val sections = notifications.groupBy { it.section }
                sections.forEach { (section, items) ->
                    item(key = "header_$section") { SectionLabel(section) }
                    items(items, key = { it.id }) { NotificationRow(it, onClick = { markRead(it.id) }) }
                }
            }
        }
    }
}

@Composable
private fun UnreadSummary(unreadCount: Int) {
    Text(
        text = if (unreadCount == 0) "You're all caught up 🎉" else "$unreadCount unread notification${if (unreadCount == 1) "" else "s"}",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 2.dp)
    )
}

@Composable
private fun SectionLabel(section: String) {
    Text(
        text = section,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
    )
}

@Composable
private fun NotificationRow(item: AppNotification, onClick: () -> Unit) {
    val container by animateColorAsState(
        targetValue = if (item.unread)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        else
            MaterialTheme.colorScheme.surface,
        label = "notifBg"
    )
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(item.tint.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = null, tint = item.tint, modifier = Modifier.size(24.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    item.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    item.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.unread) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.NotificationsNone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(36.dp)
                )
            }
            Text("No notifications yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "We'll let you know when something needs your attention.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
