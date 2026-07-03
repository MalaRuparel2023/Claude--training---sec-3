package com.mr.claudetraining.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mr.claudetraining.domain.model.JobFilter
import com.mr.claudetraining.domain.model.JobType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    filter: JobFilter,
    onApplyFilter: (JobFilter) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    var localFilter by remember { mutableStateOf(filter) }
    var skillInput by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filter", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            // Job type
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Job Type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                JobType.entries.forEach { type ->
                    FilterCheckboxRow(
                        label = type.displayName,
                        checked = type in localFilter.types,
                        onCheckedChange = { checked ->
                            val updated = localFilter.types.toMutableSet()
                            if (checked) updated.add(type) else updated.remove(type)
                            localFilter = localFilter.copy(types = updated)
                        }
                    )
                }
            }

            // Remote
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Remote Only", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = localFilter.remoteOnly,
                    onCheckedChange = { localFilter = localFilter.copy(remoteOnly = it) }
                )
            }

            // Location
            TextField(
                value = localFilter.location.orEmpty(),
                onValueChange = { value ->
                    localFilter = localFilter.copy(location = value.takeIf { it.isNotBlank() })
                },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Salary
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Minimum Salary", style = MaterialTheme.typography.bodyLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Slider(
                        value = (localFilter.minSalary ?: 0).toFloat(),
                        onValueChange = { value ->
                            localFilter = localFilter.copy(minSalary = value.toInt().takeIf { it > 0 })
                        },
                        modifier = Modifier.weight(1f),
                        valueRange = 0f..200f,
                        steps = 19
                    )
                    Text(
                        "€${localFilter.minSalary?.div(1000) ?: 0}k",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // Skills
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Skills / Tags", style = MaterialTheme.typography.bodyLarge)
                SkillChipsRow(
                    tags = localFilter.tags,
                    onRemoveTag = { tag ->
                        val updated = localFilter.tags.toMutableSet()
                        updated.remove(tag)
                        localFilter = localFilter.copy(tags = updated)
                    }
                )
                TextField(
                    value = skillInput,
                    onValueChange = { skillInput = it },
                    label = { Text("Add a skill") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        if (skillInput.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val updated = localFilter.tags.toMutableSet()
                                    updated.add(skillInput)
                                    localFilter = localFilter.copy(tags = updated)
                                    skillInput = ""
                                }
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Add", modifier = Modifier.padding(4.dp))
                            }
                        }
                    }
                )
            }

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        localFilter = JobFilter.None
                    },
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Button(
                    onClick = { onApplyFilter(localFilter) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply")
                }
            }
        }
    }
}

@Composable
private fun FilterCheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SkillChipsRow(
    tags: Set<String>,
    onRemoveTag: (String) -> Unit
) {
    val tagsList = tags.toList()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        (tagsList.indices).groupBy { it / 2 }.forEach { (_, indices) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                indices.forEach { idx ->
                    val tag = tagsList[idx]
                    AssistChip(
                        onClick = { onRemoveTag(tag) },
                        label = { Text(tag) },
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private val JobType.displayName: String
    get() = when (this) {
        JobType.FULL_TIME -> "Full-time"
        JobType.PART_TIME -> "Part-time"
        JobType.CONTRACT -> "Contract"
        JobType.INTERNSHIP -> "Internship"
    }
