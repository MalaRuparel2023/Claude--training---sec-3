package com.mr.claudetraining.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.AssistChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mr.claudetraining.domain.model.Job
import com.mr.claudetraining.ui.components.SimpleTopBar
import com.mr.claudetraining.ui.viewmodel.JobDetailViewModel

@Composable
fun JobDetailScreen(
    onBack: () -> Unit = {},
    viewModel: JobDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val job = state.job

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SimpleTopBar(
            title = job?.title?.ifBlank { "Job" } ?: "Job",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (job != null) {
                    IconButton(onClick = viewModel::toggleSaved) {
                        Crossfade(targetState = state.isSaved, label = "detailBookmark") { saved ->
                            Icon(
                                imageVector = if (saved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = if (saved) "Saved" else "Save"
                            )
                        }
                    }
                }
            }
        )
        when {
            state.isLoading -> Center { CircularProgressIndicator() }
            job == null -> Center {
                Text("Job not found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> JobDetailBody(job)
        }
    }
}

@Composable
private fun JobDetailBody(job: Job) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .widthIn(max = 640.dp)
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(job.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface)
        Text(
            listOf(job.company, job.location).filter { it.isNotBlank() }.joinToString(" · "),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (job.salaryMin > 0 && job.salaryMax > 0) {
            Text(
                "€${job.salaryMin / 1000}k–€${job.salaryMax / 1000}k",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (job.tags.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                job.tags.chunked(2).forEach { chunk ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        chunk.forEach { tag ->
                            AssistChip(
                                onClick = {},
                                label = { Text(tag) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Center(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) { content() }
}
