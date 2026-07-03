package com.mr.claudetraining.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.mr.claudetraining.domain.model.WorkoutIntensity
import com.mr.claudetraining.domain.model.WorkoutSession
import com.mr.claudetraining.domain.model.WorkoutType
import com.mr.claudetraining.domain.repository.WorkoutSessionRepository
import javax.inject.Inject

data class WorkoutSessionCreateUiState(
    val type: WorkoutType = WorkoutType.YOGA,
    val intensity: WorkoutIntensity = WorkoutIntensity.MODERATE,
    val durationMinutes: Int = 30,
    val caloriesBurned: Int = 100,
    val notes: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val sessionCreated: Boolean = false
)

@HiltViewModel
class WorkoutSessionCreateViewModel @Inject constructor(
    private val repository: WorkoutSessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutSessionCreateUiState())
    val uiState: StateFlow<WorkoutSessionCreateUiState> = _uiState.asStateFlow()

    fun setType(type: WorkoutType) {
        _uiState.value = _uiState.value.copy(type = type)
    }

    fun setIntensity(intensity: WorkoutIntensity) {
        _uiState.value = _uiState.value.copy(intensity = intensity)
    }

    fun setDuration(duration: Int) {
        _uiState.value = _uiState.value.copy(durationMinutes = duration)
    }

    fun setCalories(calories: Int) {
        _uiState.value = _uiState.value.copy(caloriesBurned = calories)
    }

    fun setNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun createSession() {
        val state = _uiState.value
        _uiState.value = state.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val session = WorkoutSession(
                type = state.type,
                intensity = state.intensity,
                durationMinutes = state.durationMinutes,
                caloriesBurned = state.caloriesBurned,
                notes = state.notes,
                startTime = System.currentTimeMillis(),
                endTime = System.currentTimeMillis()
            )

            repository.createSession(session)
                .onSuccess {
                    _uiState.value = state.copy(isLoading = false, sessionCreated = true)
                }
                .onFailure { e ->
                    _uiState.value = state.copy(isLoading = false, error = e.message)
                }
        }
    }
}
