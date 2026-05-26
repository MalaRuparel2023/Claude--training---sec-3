package ro.alexmamo.firebasesigninwithemailandpassword.ui.viewmodel

import androidx.lifecycle.ViewModel
import ro.alexmamo.firebasesigninwithemailandpassword.SrteamChatApp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.models.User
import io.getstream.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class ConnectionUiState {
    object Idle : ConnectionUiState()
    object Connecting : ConnectionUiState()
    data class Connected(val userId: String) : ConnectionUiState()
    data class Error(val message: String) : ConnectionUiState()
    object Disconnected : ConnectionUiState()
}

class UserConnectionViewModel(
    private val chatClient: ChatClient = SrteamChatApp.streamChatClient!!
) : ViewModel() {

    private val _connectionState = MutableStateFlow<ConnectionUiState>(ConnectionUiState.Idle)
    val connectionState: StateFlow<ConnectionUiState> = _connectionState.asStateFlow()

    init {
        connectUser()
    }

    private fun connectUser() {
        _connectionState.value = ConnectionUiState.Connecting

        // TODO: Replace with real user credentials from backend
        // For now, using Stream's public quickstart credentials
        // API Key: yj2prjbtfw2k (public, for demo only)
        // User: tutorial-demi (or thierry, ravi, jack for demo channels)
        val user = User(
            id = "tutorial-demi",
            name = "Tutorial User",
            image = "https://getstream.io/random_png/?id=tutorial-demi&size=200"
        )
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidHV0b3JpYWwtZGVtaSJ9.cWndtYsPXVwINzDWUbYFmvUnpja73ytEorSQA-LEdPA"

        android.util.Log.d("UserConnectionViewModel", "Connecting user: ${user.id}")
        chatClient.connectUser(user = user, token = token).enqueue { result ->
            when (result) {
                is Result.Success -> {
                    android.util.Log.d("UserConnectionViewModel", "Connected successfully: ${user.id}")
                    _connectionState.value = ConnectionUiState.Connected(user.id)
                    subscribeToConnectionEvents()
                }
                is Result.Failure -> {
                    val errorMsg = result.value.message ?: "Failed to connect"
                    android.util.Log.e("UserConnectionViewModel", "Connection failed: $errorMsg")
                    _connectionState.value = ConnectionUiState.Error(errorMsg)
                }
            }
        }
    }

    private fun subscribeToConnectionEvents() {
        chatClient.subscribeFor(
            ConnectedEvent::class.java,
            DisconnectedEvent::class.java
        ) { event ->
            when (event) {
                is ConnectedEvent -> {
                    _connectionState.value = ConnectionUiState.Connected(event.me.id)
                }
                is DisconnectedEvent -> {
                    _connectionState.value = ConnectionUiState.Disconnected
                }
                else -> Unit
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatClient.disconnect(flushPersistence = false).enqueue()
    }
}
