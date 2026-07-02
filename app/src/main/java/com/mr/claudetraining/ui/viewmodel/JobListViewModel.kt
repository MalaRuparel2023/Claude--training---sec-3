package com.mr.claudetraining.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import com.mr.claudetraining.domain.model.AnalyticsEvent
import com.mr.claudetraining.domain.model.EnhancedJob
import com.mr.claudetraining.domain.repository.AnalyticsLogger
import com.mr.claudetraining.domain.repository.JobRepository
import javax.inject.Inject

data class JobListUiState(
    val jobs: List<EnhancedJob> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * Backs the Jobs list: streams the catalog joined with the viewer's favorites
 * ([JobRepository.observeEnhancedJobs]) and toggles bookmarks. Logs `view_job_list` once per
 * first load and `save_job` on each toggle — depends only on domain interfaces, so it's
 * unit-testable with a fake repository + mock logger.
 */
@HiltViewModel
class JobListViewModel @Inject constructor(
    private val jobRepository: JobRepository,
    private val analytics: AnalyticsLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow(JobListUiState())
    val uiState: StateFlow<JobListUiState> = _uiState.asStateFlow()

    private var loggedListView = false

    init {
        viewModelScope.launch {
            jobRepository.observeEnhancedJobs()
                .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
                .collect { jobs ->
                    _uiState.value = JobListUiState(jobs = jobs, isLoading = false, error = null)
                    if (!loggedListView) {
                        analytics.log(AnalyticsEvent.ViewJobList(jobs.size))
                        loggedListView = true
                    }
                }
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
