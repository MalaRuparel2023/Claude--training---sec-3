package com.mr.claudetraining.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mr.claudetraining.domain.model.WorkoutIntensity
import com.mr.claudetraining.domain.model.WorkoutType
import com.mr.claudetraining.ui.components.SimpleTopBar
import com.mr.claudetraining.ui.viewmodel.WorkoutSessionCreateViewModel

@Composable
fun WorkoutSessionCreateScreen(
    onSuccess: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: WorkoutSessionCreateViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.sessionCreated) {
        onSuccess()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SimpleTopBar(
            title = "New Workout",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type selector
            TypeSelector(
                selected = state.type,
                onSelect = { viewModel.setType(it) }
            )

            // Intensity selector
            IntensitySelector(
                selected = state.intensity,
                onSelect = { viewModel.setIntensity(it) }
            )

            // Duration slider
            Column {
                Text("Duration: ${state.durationMinutes} min", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = state.durationMinutes.toFloat(),
                    onValueChange = { viewModel.setDuration(it.toInt()) },
                    valueRange = 5f..180f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Calories slider
            Column {
                Text("Calories: ${state.caloriesBurned}", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = state.caloriesBurned.toFloat(),
                    onValueChange = { viewModel.setCalories(it.toInt()) },
                    valueRange = 0f..500f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Notes field
            OutlinedTextField(
                value = state.notes,
                onValueChange = { viewModel.setNotes(it) },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            if (state.error != null) {
                Text(
                    state.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isLoading
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = { viewModel.createSession() },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterVertically))
                    } else {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeSelector(
    selected: WorkoutType,
    onSelect: (WorkoutType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selected.name)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            WorkoutType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.name) },
                    onClick = {
                        onSelect(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun IntensitySelector(
    selected: WorkoutIntensity,
    onSelect: (WorkoutIntensity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selected.name)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            WorkoutIntensity.entries.forEach { intensity ->
                DropdownMenuItem(
                    text = { Text(intensity.name) },
                    onClick = {
                        onSelect(intensity)
                        expanded = false
                    }
                )
            }
        }
    }
}
