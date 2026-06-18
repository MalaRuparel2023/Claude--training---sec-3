package com.mr.claudetraining.data.config

import android.content.Context
import android.content.pm.ApplicationInfo
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.mr.claudetraining.domain.model.ConfigParameter
import com.mr.claudetraining.domain.model.ConfigSource
import com.mr.claudetraining.domain.model.FeatureFlags
import com.mr.claudetraining.domain.repository.FeatureFlagProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigFeatureFlagProvider @Inject constructor(
    @ApplicationContext context: Context,
    private val remoteConfig: FirebaseRemoteConfig
) : FeatureFlagProvider {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _flags = MutableStateFlow(FeatureFlags.DEFAULTS)
    override val flags: StateFlow<FeatureFlags> = _flags.asStateFlow()

    init {
        val debuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        remoteConfig.setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder()
                // Dev/QA: fetch every time so console changes show up immediately.
                // Release: throttle to once an hour (Firebase's recommended floor).
                .setMinimumFetchIntervalInSeconds(if (debuggable) 0L else 3600L)
                .build()
        )
        remoteConfig.setDefaultsAsync(DEFAULTS)
        publish()
        scope.launch { refresh() }

        // Real-time updates — flags refresh live without a redeploy or app restart.
        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                remoteConfig.activate().addOnCompleteListener { publish() }
            }

            override fun onError(error: FirebaseRemoteConfigException) = Unit
        })
    }

    override suspend fun refresh(): Boolean = try {
        val activated = remoteConfig.fetchAndActivate().await()
        publish()
        activated
    } catch (_: Exception) {
        false
    }

    override fun parameters(): List<ConfigParameter> =
        remoteConfig.all
            .map { (key, value) ->
                ConfigParameter(
                    key = key,
                    value = value.asString(),
                    source = when (value.source) {
                        FirebaseRemoteConfig.VALUE_SOURCE_REMOTE -> ConfigSource.REMOTE
                        FirebaseRemoteConfig.VALUE_SOURCE_DEFAULT -> ConfigSource.DEFAULT
                        else -> ConfigSource.STATIC
                    }
                )
            }
            .sortedBy { it.key }

    private fun publish() {
        _flags.value = FeatureFlags(
            newJobDetailUi = remoteConfig.getBoolean(KEY_NEW_JOB_DETAIL_UI),
            newChatFeatures = remoteConfig.getBoolean(KEY_NEW_CHAT_FEATURES),
            betaFeatures = remoteConfig.getBoolean(KEY_BETA_FEATURES)
        )
    }

    companion object {
        const val KEY_NEW_JOB_DETAIL_UI = "new_job_detail_ui"
        const val KEY_NEW_CHAT_FEATURES = "new_chat_features"
        const val KEY_BETA_FEATURES = "beta_features"
        const val KEY_WELCOME_MESSAGE = "welcome_message"
        const val KEY_MAX_JOB_RESULTS = "max_job_results"

        private val DEFAULTS: Map<String, Any> = mapOf(
            KEY_NEW_JOB_DETAIL_UI to false,
            KEY_NEW_CHAT_FEATURES to false,
            KEY_BETA_FEATURES to false,
            KEY_WELCOME_MESSAGE to "Welcome to TalentSure",
            KEY_MAX_JOB_RESULTS to 20L
        )
    }
}
