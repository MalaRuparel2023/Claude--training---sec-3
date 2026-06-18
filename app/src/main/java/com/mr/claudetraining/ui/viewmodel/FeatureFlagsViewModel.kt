package com.mr.claudetraining.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.mr.claudetraining.domain.model.ConfigParameter
import com.mr.claudetraining.domain.model.FeatureFlags
import com.mr.claudetraining.domain.repository.CrashReporter
import com.mr.claudetraining.domain.repository.FeatureFlagProvider
import javax.inject.Inject

/**
 * Backs both the flag-driven conditional UI and the dev/QA Config screen.
 * Reads flags live from [FeatureFlagProvider] and exposes manual refresh +
 * Crashlytics test hooks.
 */
@HiltViewModel
class FeatureFlagsViewModel @Inject constructor(
    private val featureFlagProvider: FeatureFlagProvider,
    private val crashReporter: CrashReporter
) : ViewModel() {

    val flags: StateFlow<FeatureFlags> = featureFlagProvider.flags

    private val _parameters = MutableStateFlow<List<ConfigParameter>>(emptyList())
    val parameters: StateFlow<List<ConfigParameter>> = _parameters.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    /** Null until the first refresh; then true if the last fetch changed values. */
    private val _lastRefreshChanged = MutableStateFlow<Boolean?>(null)
    val lastRefreshChanged: StateFlow<Boolean?> = _lastRefreshChanged.asStateFlow()

    init {
        loadParameters()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _lastRefreshChanged.value = featureFlagProvider.refresh()
            loadParameters()
            _isRefreshing.value = false
        }
    }

    fun triggerTestCrash() = crashReporter.forceTestCrash()

    fun logNonFatal() {
        crashReporter.recordNonFatal(
            RuntimeException("Non-fatal test logged from the Config screen")
        )
    }

    private fun loadParameters() {
        _parameters.value = featureFlagProvider.parameters()
    }
}
