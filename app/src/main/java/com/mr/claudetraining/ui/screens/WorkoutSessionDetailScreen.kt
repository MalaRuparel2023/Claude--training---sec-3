package com.mr.claudetraining.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mr.claudetraining.ui.components.SimpleTopBar
import com.mr.claudetraining.ui.viewmodel.WorkoutSessionDetailViewModel

@Composable
fun WorkoutSessionDetailScreen(
    onBack: () -> Unit = {},
    viewModel: WorkoutSessionDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val session = state.session

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        SimpleTopBar(
            title = session?.type?.name ?: "Workout",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        when {
            state.isLoading -> Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            session == null -> Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) { Text("Session not found") }
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Type: ${session.type.name}", style = MaterialTheme.typography.titleMedium)
                Text("Intensity: ${session.intensity.name}", style = MaterialTheme.typography.bodyLarge)
                Text("Duration: ${session.durationMinutes} minutes", style = MaterialTheme.typography.bodyLarge)
                Text("Calories: ${session.caloriesBurned} kcal", style = MaterialTheme.typography.bodyLarge)
                Text("Date: ${session.dateStr}", style = MaterialTheme.typography.bodyLarge)
                if (session.notes.isNotEmpty()) {
                    Text("Notes: ${session.notes}", style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    "Synced: ${if (session.syncedToCloud) "✓ Yes" else "✗ No"}",
                    color = if (session.syncedToCloud) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Button(
                    onClick = { viewModel.deleteSession(); onBack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete Session")
                }
            }
        }
    }
}
