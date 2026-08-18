package com.mr.claudetraining.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import com.mr.claudetraining.domain.model.WorkoutSession
import com.mr.claudetraining.domain.repository.WorkoutSessionRepository
import javax.inject.Inject

data class WorkoutSessionListUiState(
    val sessions: List<WorkoutSession> = emptyList(),
    val isLoading: Boolean = true,
    val isOnline: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class WorkoutSessionListViewModel @Inject constructor(
    private val repository: WorkoutSessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutSessionListUiState())
    val uiState: StateFlow<WorkoutSessionListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeSessions()
                .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
                .collect { sessions ->
                    _uiState.value = _uiState.value.copy(sessions = sessions, isLoading = false)
                }
        }

        viewModelScope.launch {
            repository.observeSyncStatus()
                .collect { isOnline ->
                    _uiState.value = _uiState.value.copy(isOnline = isOnline)
                }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            repository.syncToCloud()
        }
    }
}
