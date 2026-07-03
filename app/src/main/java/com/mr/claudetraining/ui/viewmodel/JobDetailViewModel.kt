package com.mr.claudetraining.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.mr.claudetraining.domain.model.AnalyticsEvent
import com.mr.claudetraining.domain.model.Job
import com.mr.claudetraining.domain.repository.AnalyticsLogger
import com.mr.claudetraining.domain.repository.JobRepository
import javax.inject.Inject

data class JobDetailUiState(
    val job: Job? = null,
    val isSaved: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * Backs the job detail screen: streams a single [Job] joined with its saved state, and toggles
 * the bookmark. Logs `view_job` once when the posting first resolves and `save_job` on toggle.
 */
@HiltViewModel
class JobDetailViewModel @Inject constructor(
    private val jobRepository: JobRepository,
    private val analytics: AnalyticsLogger,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val jobId: String = savedStateHandle.get<String>("jobId").orEmpty()

    private val _uiState = MutableStateFlow(JobDetailUiState())
    val uiState: StateFlow<JobDetailUiState> = _uiState.asStateFlow()

    private var loggedView = false

    init {
        viewModelScope.launch {
            combine(
                jobRepository.observeJob(jobId),
                jobRepository.observeSavedJobIds()
            ) { job, savedIds -> job to (job != null && job.id in savedIds) }
                .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
                .collect { (job, isSaved) ->
                    _uiState.value = JobDetailUiState(job = job, isSaved = isSaved, isLoading = false)
                    if (!loggedView && job != null) {
                        analytics.log(AnalyticsEvent.ViewJob(job.id, job.title))
                        loggedView = true
                    }
                }
        }
    }

    fun toggleSaved() {
        val job = _uiState.value.job ?: return
        val newSaved = !_uiState.value.isSaved
        viewModelScope.launch {
            runCatching { jobRepository.setSaved(job.id, newSaved) }
                .onSuccess { analytics.log(AnalyticsEvent.SaveJob(job.id, newSaved)) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }
}
