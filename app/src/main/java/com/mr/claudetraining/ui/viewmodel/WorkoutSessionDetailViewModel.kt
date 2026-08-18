package com.mr.claudetraining.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
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

data class WorkoutSessionDetailUiState(
    val session: WorkoutSession? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class WorkoutSessionDetailViewModel @Inject constructor(
    private val repository: WorkoutSessionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sessionId: String = savedStateHandle.get<String>("sessionId").orEmpty()

    private val _uiState = MutableStateFlow(WorkoutSessionDetailUiState())
    val uiState: StateFlow<WorkoutSessionDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeSession(sessionId)
                .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
                .collect { session ->
                    _uiState.value = WorkoutSessionDetailUiState(session = session, isLoading = false)
                }
        }
    }

    fun deleteSession() {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            repository.deleteSession(session.id)
        }
    }
}
