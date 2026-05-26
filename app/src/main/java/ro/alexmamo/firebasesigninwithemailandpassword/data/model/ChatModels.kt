package ro.alexmamo.firebasesigninwithemailandpassword.data.model

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User

data class ChatListUiState(
    val channels: List<ChannelUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ChannelUi(
    val id: String,
    val name: String,
    val image: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = 0,
    val unreadCount: Int = 0
) {
    companion object {
        fun fromChannel(channel: Channel): ChannelUi {
            val lastMessage = channel.messages.lastOrNull()
            return ChannelUi(
                id = channel.id,
                name = channel.name.ifEmpty { channel.id },
                image = channel.image,
                lastMessage = lastMessage?.text ?: "",
                lastMessageTime = lastMessage?.createdAt?.time ?: 0,
                unreadCount = channel.unreadCount
            )
        }
    }
}

data class ChatDetailUiState(
    val channelId: String = "",
    val channelName: String = "",
    val messages: List<MessageUi> = emptyList(),
    val typingUsers: List<UserUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class MessageUi(
    val id: String,
    val text: String,
    val author: UserUi,
    val createdAt: Long,
    val isMine: Boolean = false,
    val reactions: List<ReactionUi> = emptyList()
) {
    companion object {
        fun fromMessage(message: Message, currentUserId: String): MessageUi {
            return MessageUi(
                id = message.id,
                text = message.text,
                author = UserUi.fromStreamUser(message.user ?: User()),
                createdAt = message.createdAt?.time ?: 0,
                isMine = message.user?.id == currentUserId,
                reactions = message.reactionCounts.map { (emoji, count) ->
                    ReactionUi(emoji, count)
                }
            )
        }
    }
}

data class UserUi(
    val id: String,
    val name: String = "",
    val image: String = "",
    val isOnline: Boolean = false,
    val isTyping: Boolean = false
) {
    companion object {
        fun fromStreamUser(user: User): UserUi {
            return UserUi(
                id = user.id,
                name = user.name.ifEmpty { user.id },
                image = user.image,
                isOnline = user.online
            )
        }
    }
}

data class ReactionUi(
    val emoji: String,
    val count: Int
)