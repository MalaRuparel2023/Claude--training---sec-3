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
import com.mr.claudetraining.domain.model.JobFilter
import com.mr.claudetraining.domain.model.JobSearchItem
import com.mr.claudetraining.domain.repository.AnalyticsLogger
import com.mr.claudetraining.domain.repository.SearchRepository
import javax.inject.Inject

data class JobSearchUiState(
    val query: String = "",
    val filter: JobFilter = JobFilter.None,
    val results: List<JobSearchItem> = emptyList(),
    val isLoading: Boolean = false,
    val totalCount: Int = 0,
    val error: String? = null
)

@HiltViewModel
class JobSearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val analytics: AnalyticsLogger
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _filter = MutableStateFlow(JobFilter.None)
    val filter: StateFlow<JobFilter> = _filter.asStateFlow()

    private val _uiState = MutableStateFlow(JobSearchUiState())
    val uiState: StateFlow<JobSearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            searchRepository.search(_query, _filter)
                .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
                .collect { result ->
                    _uiState.value = JobSearchUiState(
                        query = result.query,
                        filter = _filter.value,
                        results = result.items,
                        isLoading = false,
                        totalCount = result.totalCount,
                        error = null
                    )
                    if (result.query.isNotBlank()) {
                        analytics.log(AnalyticsEvent.SearchJobs(result.query, result.totalCount))
                    }
                }
        }
    }

    fun setQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun setFilter(newFilter: JobFilter) {
        _filter.value = newFilter
    }

    fun toggleJobType(jobType: com.mr.claudetraining.domain.model.JobType) {
        val current = _filter.value.types.toMutableSet()
        if (jobType in current) {
            current.remove(jobType)
        } else {
            current.add(jobType)
        }
        _filter.value = _filter.value.copy(types = current)
    }

    fun toggleRemoteOnly() {
        _filter.value = _filter.value.copy(remoteOnly = !_filter.value.remoteOnly)
    }

    fun setLocation(location: String) {
        _filter.value = _filter.value.copy(location = location.takeIf { it.isNotBlank() })
    }

    fun setMinSalary(salary: Int) {
        _filter.value = _filter.value.copy(minSalary = salary.takeIf { it > 0 })
    }

    fun toggleSkillTag(tag: String) {
        val current = _filter.value.tags.toMutableSet()
        if (tag in current) {
            current.remove(tag)
        } else {
            current.add(tag)
        }
        _filter.value = _filter.value.copy(tags = current)
    }

    fun addSkillTag(tag: String) {
        if (tag.isNotBlank()) {
            val current = _filter.value.tags.toMutableSet()
            current.add(tag)
            _filter.value = _filter.value.copy(tags = current)
        }
    }

    fun resetFilter() {
        _filter.value = JobFilter.None
    }
}
