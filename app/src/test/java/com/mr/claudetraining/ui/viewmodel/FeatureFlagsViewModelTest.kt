package com.mr.claudetraining.ui.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import com.mr.claudetraining.domain.model.ConfigParameter
import com.mr.claudetraining.domain.model.ConfigSource
import com.mr.claudetraining.domain.model.FeatureFlags
import com.mr.claudetraining.domain.repository.CrashReporter
import com.mr.claudetraining.domain.repository.FeatureFlagProvider

@OptIn(ExperimentalCoroutinesApi::class)
class FeatureFlagsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private class FakeFeatureFlagProvider : FeatureFlagProvider {
        val flagsState = MutableStateFlow(FeatureFlags.DEFAULTS)
        override val flags: StateFlow<FeatureFlags> = flagsState

        var refreshResult = true
        var refreshCount = 0
        var params = listOf(ConfigParameter("new_job_detail_ui", "true", ConfigSource.REMOTE))

        override suspend fun refresh(): Boolean {
            refreshCount++
            return refreshResult
        }

        override fun parameters(): List<ConfigParameter> = params
    }

    private class FakeCrashReporter : CrashReporter {
        val logged = mutableListOf<String>()
        val nonFatals = mutableListOf<Throwable>()
        val keys = mutableMapOf<String, String>()
        var forced = false

        override fun log(message: String) { logged += message }
        override fun setCustomKey(key: String, value: String) { keys[key] = value }
        override fun recordNonFatal(throwable: Throwable) { nonFatals += throwable }
        override fun forceTestCrash() { forced = true }
    }

    private val provider = FakeFeatureFlagProvider()
    private val crashReporter = FakeCrashReporter()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = FeatureFlagsViewModel(provider, crashReporter)

    @Test
    fun `flags mirror the provider`() = runTest(dispatcher) {
        provider.flagsState.value = FeatureFlags(newJobDetailUi = true, betaFeatures = true)

        val vm = viewModel()

        assertTrue(vm.flags.value.newJobDetailUi)
        assertTrue(vm.flags.value.betaFeatures)
        assertFalse(vm.flags.value.newChatFeatures)
    }

    @Test
    fun `init loads parameters from provider`() = runTest(dispatcher) {
        val vm = viewModel()

        assertEquals(provider.params, vm.parameters.value)
    }

    @Test
    fun `lastRefreshChanged is null before any refresh`() = runTest(dispatcher) {
        val vm = viewModel()

        assertNull(vm.lastRefreshChanged.value)
        assertFalse(vm.isRefreshing.value)
    }

    @Test
    fun `refresh fetches, reloads parameters and clears refreshing`() = runTest(dispatcher) {
        provider.refreshResult = true
        provider.params = listOf(
            ConfigParameter("beta_features", "true", ConfigSource.REMOTE),
            ConfigParameter("welcome_message", "Hi", ConfigSource.DEFAULT)
        )

        val vm = viewModel()
        vm.refresh()
        advanceUntilIdle()

        assertEquals(1, provider.refreshCount)
        assertEquals(true, vm.lastRefreshChanged.value)
        assertEquals(provider.params, vm.parameters.value)
        assertFalse(vm.isRefreshing.value)
    }

    @Test
    fun `refresh reports no change when nothing activated`() = runTest(dispatcher) {
        provider.refreshResult = false

        val vm = viewModel()
        vm.refresh()
        advanceUntilIdle()

        assertEquals(false, vm.lastRefreshChanged.value)
    }

    @Test
    fun `triggerTestCrash delegates to crash reporter`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.triggerTestCrash()

        assertTrue(crashReporter.forced)
    }

    @Test
    fun `logNonFatal records a non-fatal exception`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.logNonFatal()

        assertEquals(1, crashReporter.nonFatals.size)
        assertTrue(crashReporter.nonFatals.first() is RuntimeException)
    }
}
