package com.mr.claudetraining.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import com.mr.claudetraining.domain.repository.PushTokenRepository
import javax.inject.Inject

@HiltViewModel
class PushTokenViewModel @Inject constructor(
    private val pushTokenRepository: PushTokenRepository
) : ViewModel() {

    /** Fetch the current FCM token and register it for the signed-in user. */
    fun syncToken() {
        viewModelScope.launch {
            pushTokenRepository.currentToken()?.let { pushTokenRepository.registerToken(it) }
        }
    }
}
