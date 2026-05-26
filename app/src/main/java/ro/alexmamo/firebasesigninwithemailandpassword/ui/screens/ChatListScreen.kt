package ro.alexmamo.firebasesigninwithemailandpassword.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import ro.alexmamo.firebasesigninwithemailandpassword.data.model.ChatListUiState
import ro.alexmamo.firebasesigninwithemailandpassword.data.model.ChannelUi
import ro.alexmamo.firebasesigninwithemailandpassword.ui.viewmodel.ChatListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatListScreen(
    onChannelSelected: (String) -> Unit,
    viewModel: ChatListViewModel = viewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value

    when {
        uiState.isLoading -> LoadingState()
        uiState.error != null -> ErrorState(
            error = uiState.error,
            onRetry = { viewModel.retryLoadChannels() },
            onCreateChannel = { viewModel.createDemoChannel() }
        )
        uiState.channels.isEmpty() -> EmptyState()
        else -> ChatListContent(
            channels = uiState.channels,
            onChannelSelected = onChannelSelected
        )
    }
}

@Composable
private fun ChatListContent(
    channels: List<ChannelUi>,
    onChannelSelected: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(
            items = channels,
            key = { it.id }
        ) { channel ->
            ChatListItem(
                channel = channel,
                onSelect = { onChannelSelected(channel.id) }
            )
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
private fun ChatListItem(
    channel: ChannelUi,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = channel.image.ifEmpty { "https://via.placeholder.com/48" },
            contentDescription = channel.name,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Text(
                text = channel.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = channel.lastMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = formatTime(channel.lastMessageTime),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (channel.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = channel.unreadCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    onCreateChannel: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Error loading channels",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            Button(onClick = onCreateChannel) {
                Text("Create Demo Channel")
            }
            Text(
                text = "Retry",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onRetry)
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "No channels available",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Start a new conversation or join an existing channel",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTime(timeMillis: Long): String {
    if (timeMillis == 0L) return ""

    val date = Date(timeMillis)
    val now = Date()
    val diffMs = now.time - date.time
    val diffMins = diffMs / (1000 * 60)
    val diffHours = diffMs / (1000 * 60 * 60)
    val diffDays = diffMs / (1000 * 60 * 60 * 24)

    return when {
        diffMins < 1 -> "now"
        diffMins < 60 -> "${diffMins}m"
        diffHours < 24 -> "${diffHours}h"
        diffDays < 7 -> "${diffDays}d"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
    }
}
