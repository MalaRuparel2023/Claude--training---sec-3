package com.mr.claudetraining.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mr.claudetraining.ui.components.AnimatedEntry
import com.mr.claudetraining.ui.components.JobCard
import com.mr.claudetraining.ui.components.SimpleTopBar
import com.mr.claudetraining.ui.viewmodel.JobListViewModel

// Cap content width so the list doesn't stretch edge-to-edge on tablets/landscape.
private val MaxContentWidth = 640.dp

@Composable
fun JobListScreen(
    onOpenJob: (String) -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: JobListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SimpleTopBar(
            title = "Jobs",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
        when {
            state.isLoading -> CenterBox { CircularProgressIndicator() }
            state.jobs.isEmpty() -> CenterBox {
                Text(
                    text = state.error?.let { "Couldn't load jobs" } ?: "No jobs right now",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            else -> BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .widthIn(max = MaxContentWidth)
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.jobs, key = { it.job.id }) { enhanced ->
                        AnimatedEntry {
                            JobCard(
                                enhanced = enhanced,
                                onToggleSave = { saved -> viewModel.toggleSaved(enhanced.job.id, saved) },
                                onClick = { onOpenJob(enhanced.job.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CenterBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) { content() }
}
