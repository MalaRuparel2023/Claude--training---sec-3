package com.mr.claudetraining.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import com.mr.claudetraining.domain.model.ChatConnectionState
import com.mr.claudetraining.domain.repository.ChatRepository
import javax.inject.Inject

@HiltViewModel
class UserConnectionViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    // The repository already exposes connection state as a StateFlow of the domain
    // model; the UI consumes it directly instead of mirroring it into a parallel type.
    val connectionState: StateFlow<ChatConnectionState> = chatRepository.connectionState

    init {
        chatRepository.connect()
    }

    fun signIn() = chatRepository.connect()

    fun signOut() = chatRepository.disconnect(clearData = true)
}