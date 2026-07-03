package com.mr.claudetraining.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.mr.claudetraining.domain.model.AnalyticsEvent
import com.mr.claudetraining.domain.model.EnhancedJob
import com.mr.claudetraining.domain.repository.AnalyticsLogger
import com.mr.claudetraining.domain.repository.JobRepository
import javax.inject.Inject

data class SavedJobsUiState(
    val jobs: List<EnhancedJob> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * Backs the Saved list: the enhanced catalog filtered to favorites. Un-saving from here removes
 * the row live (the same enhanced stream re-emits). Logs `save_job` on toggle.
 */
@HiltViewModel
class SavedJobsViewModel @Inject constructor(
    private val jobRepository: JobRepository,
    private val analytics: AnalyticsLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedJobsUiState())
    val uiState: StateFlow<SavedJobsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            jobRepository.observeEnhancedJobs()
                .map { list -> list.filter(EnhancedJob::isFavorite) }
                .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
                .collect { saved -> _uiState.value = SavedJobsUiState(jobs = saved, isLoading = false) }
        }
    }

    fun toggleSaved(jobId: String, saved: Boolean) {
        if (jobId.isBlank()) return
        viewModelScope.launch {
            runCatching { jobRepository.setSaved(jobId, saved) }
                .onSuccess { analytics.log(AnalyticsEvent.SaveJob(jobId, saved)) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }
}
