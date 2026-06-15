package com.mr.claudetraining.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import com.mr.claudetraining.domain.model.DailyGoal
import com.mr.claudetraining.domain.model.UserProfile
import com.mr.claudetraining.domain.model.YogaProgram
import com.mr.claudetraining.domain.repository.YogaRepository
import javax.inject.Inject

data class YogaUiState(
    val programs: List<YogaProgram> = emptyList(),
    val profile: UserProfile? = null,
    val dailyGoal: DailyGoal? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class YogaViewModel @Inject constructor(
    private val repository: YogaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(YogaUiState())
    val uiState: StateFlow<YogaUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            runCatching { repository.seedSampleDataIfEmpty() }

            launch {
                repository.observePrograms()
                    .catch { e -> setError(e) }
                    .collect { programs ->
                        _uiState.value = _uiState.value.copy(
                            programs = programs,
                            isLoading = false,
                            error = null
                        )
                    }
            }
            launch {
                repository.observeProfile()
                    .catch { }
                    .collect { profile -> _uiState.value = _uiState.value.copy(profile = profile) }
            }
            launch {
                repository.observeDailyGoal()
                    .catch { }
                    .collect { goal -> _uiState.value = _uiState.value.copy(dailyGoal = goal) }
            }
        }
    }

    private fun setError(e: Throwable) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = e.message ?: "Failed to load data"
        )
    }
}