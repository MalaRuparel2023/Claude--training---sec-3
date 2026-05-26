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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import ro.alexmamo.firebasesigninwithemailandpassword.data.model.ChatDetailUiState
import ro.alexmamo.firebasesigninwithemailandpassword.data.model.MessageUi
import ro.alexmamo.firebasesigninwithemailandpassword.ui.viewmodel.ChatDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
//55qnjdzuxcxhtkfs3eyxyv3qcy7vvjmnzgmvcpmpchb6hwa72uaqtt54kkupk68a
@Composable
fun ChatDetailScreen(
    channelId: String,
    onBack: () -> Unit,
    viewModel: ChatDetailViewModel = viewModel { ChatDetailViewModel(channelId = channelId) }
) {
    val uiState = viewModel.uiState.collectAsState().value

    Column(modifier = Modifier.fillMaxSize()) {
        ChatDetailHeader(
            channelName = uiState.channelName,
            onBack = onBack
        )

        when {
            uiState.isLoading -> ChatLoadingState()
            uiState.error != null -> ChatErrorState(
                error = uiState.error,
                onRetry = { viewModel.retry() }
            )
            else -> {
                ChatMessagesContent(
                    messages = uiState.messages,
                    typingUsers = uiState.typingUsers,
                    modifier = Modifier.weight(1f)
                )

                MessageInput(
                    onSendMessage = { text ->
                        viewModel.sendMessage(text)
                    },
                    onTypingStart = { viewModel.startTyping() },
                    onTypingStop = { viewModel.stopTyping() }
                )
            }
        }

        if (uiState.error != null) {
            Text(
                text = uiState.error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun ChatDetailHeader(
    channelName: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Text(
                text = channelName,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ChatMessagesContent(
    messages: List<MessageUi>,
    typingUsers: List<ro.alexmamo.firebasesigninwithemailandpassword.data.model.UserUi>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)
    ) {
        items(
            items = messages,
            key = { it.id }
        ) { message ->
            MessageBubble(message)
        }

        if (typingUsers.isNotEmpty()) {
            item {
                TypingIndicator(users = typingUsers)
            }
        }
    }
}

@Composable
private fun MessageBubble(message: MessageUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalArrangement = if (message.isMine) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isMine) {
            AsyncImage(
                model = message.author.image.ifEmpty { "https://via.placeholder.com/32" },
                contentDescription = message.author.name,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(end = 8.dp)
            )
        }

        Column(
            horizontalAlignment = if (message.isMine) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (!message.isMine) {
                Text(
                    text = message.author.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .background(
                        color = if (message.isMine)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (message.isMine)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (message.reactions.isNotEmpty()) {
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            message.reactions.forEach { reaction ->
                                Text(
                                    text = "${reaction.emoji} ${reaction.count}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = formatMessageTime(message.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TypingIndicator(
    users: List<ro.alexmamo.firebasesigninwithemailandpassword.data.model.UserUi>
) {
    val userNames = users.take(2).joinToString(", ") { it.name }
    val andMore = if (users.size > 2) " and ${users.size - 2} more" else ""

    Row(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$userNames$andMore is typing",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Light
        )

        repeat(3) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant,
                        CircleShape
                    )
            )
        }
    }
}

@Composable
private fun MessageInput(
    onSendMessage: (String) -> Unit,
    onTypingStart: () -> Unit,
    onTypingStop: () -> Unit
) {
    val inputText = remember { mutableStateOf("") }
    val isTyping = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = inputText.value,
            onValueChange = { text ->
                inputText.value = text
                if (text.isNotEmpty() && !isTyping.value) {
                    isTyping.value = true
                    onTypingStart()
                } else if (text.isEmpty() && isTyping.value) {
                    isTyping.value = false
                    onTypingStop()
                }
            },
            modifier = Modifier
                .weight(1f)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = MaterialTheme.typography.bodySmall.fontSize
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine = true
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    CircleShape
                )
                .clip(CircleShape)
                .clickable(enabled = inputText.value.isNotEmpty()) {
                    val text = inputText.value.trim()
                    if (text.isNotEmpty()) {
                        onSendMessage(text)
                        inputText.value = ""
                        isTyping.value = false
                        onTypingStop()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "→",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Composable
private fun ChatLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ChatErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Error loading chat",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

private fun formatMessageTime(timeMillis: Long): String {
    val date = Date(timeMillis)
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
}