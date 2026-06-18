package com.mr.claudetraining.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mr.claudetraining.domain.model.ConfigParameter
import com.mr.claudetraining.domain.model.ConfigSource
import com.mr.claudetraining.ui.components.SimpleTopBar
import com.mr.claudetraining.ui.viewmodel.FeatureFlagsViewModel

/**
 * Dev/QA screen: shows the live feature flags and every Remote Config parameter,
 * lets you force a fetch, and exposes Crashlytics test hooks (crash / non-fatal).
 */
@Composable
fun ConfigScreen(
    onBack: () -> Unit = {},
    viewModel: FeatureFlagsViewModel = hiltViewModel()
) {
    val flags by viewModel.flags.collectAsState()
    val parameters by viewModel.parameters.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val lastChanged by viewModel.lastRefreshChanged.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        SimpleTopBar(
            title = "Config (Dev/QA)",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { SectionTitle("Feature Flags") }
            item { FlagRow("new_job_detail_ui", flags.newJobDetailUi) }
            item { FlagRow("new_chat_features", flags.newChatFeatures) }
            item { FlagRow("beta_features", flags.betaFeatures) }

            item { SectionTitle("Remote Config Parameters") }
            if (parameters.isEmpty()) {
                item {
                    Text(
                        "No parameters loaded yet — tap Refresh.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(parameters) { ParameterRow(it) }
            }

            item { SectionTitle("Actions") }
            item {
                RefreshCard(
                    isRefreshing = isRefreshing,
                    lastChanged = lastChanged,
                    onRefresh = viewModel::refresh
                )
            }
            item {
                OutlinedButton(
                    onClick = viewModel::logNonFatal,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Log non-fatal to Crashlytics") }
            }
            item {
                OutlinedButton(
                    onClick = viewModel::triggerTestCrash,
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Trigger test crash") }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun FlagRow(name: String, enabled: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f)
            )
            // Read-only mirror of the remote value; flags are driven by Remote Config.
            Switch(checked = enabled, onCheckedChange = null)
        }
    }
}

@Composable
private fun ParameterRow(param: ConfigParameter) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = param.key,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = param.value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "source: ${param.source.label()}",
                style = MaterialTheme.typography.labelSmall,
                color = when (param.source) {
                    ConfigSource.REMOTE -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun RefreshCard(
    isRefreshing: Boolean,
    lastChanged: Boolean?,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Fetch & activate now", style = MaterialTheme.typography.bodyLarge)
                val status = when {
                    isRefreshing -> "Refreshing…"
                    lastChanged == true -> "Last fetch: values updated"
                    lastChanged == false -> "Last fetch: no changes"
                    else -> "Not refreshed yet"
                }
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isRefreshing) {
                Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            } else {
                OutlinedButton(onClick = onRefresh) { Text("Refresh") }
            }
        }
    }
}

private fun ConfigSource.label(): String = when (this) {
    ConfigSource.REMOTE -> "remote"
    ConfigSource.DEFAULT -> "default"
    ConfigSource.STATIC -> "static"
}
