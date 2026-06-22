package com.mr.claudetraining.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.mr.claudetraining.domain.model.DailyNutrition
import com.mr.claudetraining.domain.model.NutritionGoal
import com.mr.claudetraining.domain.model.WeightEntry
import com.mr.claudetraining.domain.model.WorkoutEntry
import com.mr.claudetraining.ui.components.CalorieRing
import com.mr.claudetraining.ui.components.MacroBar
import com.mr.claudetraining.ui.components.SimpleTopBar
import com.mr.claudetraining.ui.theme.BrandBlue
import com.mr.claudetraining.ui.theme.FiteloGreen
import com.mr.claudetraining.ui.theme.FiteloGreenDark
import com.mr.claudetraining.ui.theme.FiteloOrange
import com.mr.claudetraining.ui.viewmodel.HealthViewModel
import kotlin.math.abs

private enum class WeightRange(val label: String, val count: Int) {
    WEEK("7 days", 7),
    MONTH("30 days", 30),
    ALL("All", Int.MAX_VALUE)
}

@Composable
fun ProgressScreen(viewModel: HealthViewModel = hiltViewModel()) {
    val state = viewModel.uiState.collectAsState().value
    var addingWeight by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
        SimpleTopBar(title = "Progress")

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                WeightHero(
                    entries = state.weightEntries,
                    onLog = { addingWeight = true }
                )
            }
            item {
                WeightCard(
                    entries = state.weightEntries,
                    onAdd = { addingWeight = true },
                    onDelete = { viewModel.deleteWeight(it) }
                )
            }
            item {
                NutritionSummaryCard(
                    nutrition = state.nutrition,
                    goal = state.goal
                )
            }
            item {
                ActivityCard(workouts = state.workoutEntries)
            }
            item {
                WaterCard(
                    glasses = state.water.glasses,
                    goal = state.goal.waterGoalGlasses,
                    onAdd = { viewModel.changeWater(1) },
                    onRemove = { viewModel.changeWater(-1) }
                )
            }
        }
    }

    if (addingWeight) {
        AddWeightDialog(
            onDismiss = { addingWeight = false },
            onConfirm = {
                viewModel.addWeight(it)
                addingWeight = false
            }
        )
    }
}

/** Gradient hero summarizing the user's weight journey at a glance. */
@Composable
private fun WeightHero(entries: List<WeightEntry>, onLog: () -> Unit) {
    val latest = entries.lastOrNull()?.weightKg
    val start = entries.firstOrNull()?.weightKg
    val delta = if (latest != null && start != null) latest - start else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(listOf(FiteloGreen, FiteloGreenDark)))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                "Current weight",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.85f)
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = latest?.let { "%.1f".format(it) } ?: "—",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (latest != null) {
                    Text(
                        "  kg",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                if (entries.size >= 2) {
                    val down = delta < 0f
                    val icon = when {
                        abs(delta) < 0.05f -> Icons.AutoMirrored.Filled.TrendingFlat
                        down -> Icons.AutoMirrored.Filled.TrendingDown
                        else -> Icons.AutoMirrored.Filled.TrendingUp
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Text(
                            " ${if (delta > 0) "+" else ""}${"%.1f".format(delta)} kg",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            Text(
                text = when {
                    entries.size < 2 -> "Log your weight to start tracking your trend."
                    delta < 0f -> "You're down ${"%.1f".format(abs(delta))} kg since you started. Keep going!"
                    delta > 0f -> "Up ${"%.1f".format(delta)} kg since you started."
                    else -> "Holding steady since you started."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(onClick = onLog)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = FiteloGreenDark, modifier = Modifier.size(18.dp))
                Text("  Log weight", color = FiteloGreenDark, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun WeightCard(entries: List<WeightEntry>, onAdd: () -> Unit, onDelete: (String) -> Unit) {
    var range by remember { mutableStateOf(WeightRange.ALL) }
    val shown = if (entries.size > range.count) entries.takeLast(range.count) else entries

    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionHeader(Icons.Filled.MonitorWeight, "Weight trend", Modifier.weight(1f))
            IconButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = "Log weight", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WeightRange.entries.forEach { r ->
                RangeChip(label = r.label, selected = r == range, onClick = { range = r })
            }
        }

        if (shown.size >= 2) {
            WeightChart(
                entries = shown,
                modifier = Modifier.fillMaxWidth().height(160.dp)
            )
            val values = shown.map { it.weightKg }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatChip("Low", "%.1f".format(values.min()), Modifier.weight(1f))
                StatChip("High", "%.1f".format(values.max()), Modifier.weight(1f))
                StatChip("Avg", "%.1f".format(values.average().toFloat()), Modifier.weight(1f))
            }
        } else {
            Text(
                "Log at least two entries to see your trend.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        entries.asReversed().take(5).forEach { entry ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(entry.date, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Text("${entry.weightKg} kg", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = { onDelete(entry.id) }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete entry",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/** Today's calories + macros, reusing the dashboard ring and bars. */
@Composable
private fun NutritionSummaryCard(nutrition: DailyNutrition, goal: NutritionGoal) {
    SectionCard {
        SectionHeader(Icons.Filled.LocalFireDepartment, "Today's nutrition")
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CalorieRing(
                consumed = nutrition.caloriesConsumed,
                goal = goal.calorieGoal,
                burned = nutrition.caloriesBurned
            )
        }
        MacroBar("Protein", nutrition.protein, goal.proteinGoal, FiteloGreen, Modifier.fillMaxWidth())
        MacroBar("Carbs", nutrition.carbs, goal.carbGoal, FiteloOrange, Modifier.fillMaxWidth())
        MacroBar("Fat", nutrition.fat, goal.fatGoal, BrandBlue, Modifier.fillMaxWidth())
    }
}

/** Today's workout totals. */
@Composable
private fun ActivityCard(workouts: List<WorkoutEntry>) {
    val minutes = workouts.sumOf { it.durationMinutes }
    val burned = workouts.sumOf { it.caloriesBurned }

    SectionCard {
        SectionHeader(Icons.Filled.Timer, "Today's activity")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            MetricTile(Icons.Filled.Timer, "$minutes", "minutes", FiteloGreen, Modifier.weight(1f))
            MetricTile(Icons.Filled.LocalFireDepartment, "$burned", "kcal burned", FiteloOrange, Modifier.weight(1f))
            MetricTile(Icons.Filled.MonitorWeight, "${workouts.size}", "workouts", BrandBlue, Modifier.weight(1f))
        }
        if (workouts.isEmpty()) {
            Text(
                "No workouts logged today.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WaterCard(glasses: Int, goal: Int, onAdd: () -> Unit, onRemove: () -> Unit) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionHeader(Icons.Filled.WaterDrop, "Water", Modifier.weight(1f))
            Text(
                "$glasses / $goal glasses",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(goal.coerceAtLeast(1)) { index ->
                Box(
                    modifier = Modifier
                        .size(width = 22.dp, height = 30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (index < glasses) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WaterButton(Icons.Filled.Remove, "Remove glass", onRemove)
            WaterButton(Icons.Filled.Add, "Add glass", onAdd)
        }
    }
}

@Composable
private fun WaterButton(icon: ImageVector, desc: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = desc, tint = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

/* ---------- shared building blocks ---------- */

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            content()
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(
            "  $title",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RangeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MetricTile(icon: ImageVector, value: String, label: String, tint: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(tint.copy(alpha = 0.12f))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Smooth area chart with a gradient fill under the weight line. */
@Composable
private fun WeightChart(entries: List<WeightEntry>, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    val values = entries.map { it.weightKg }
    val min = values.min()
    val max = values.max()
    val range = (max - min).takeIf { it > 0f } ?: 1f
    val animated by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800),
        label = "weightChart"
    )

    Canvas(modifier = modifier) {
        val padding = 14f
        val w = size.width - padding * 2
        val h = size.height - padding * 2
        val stepX = if (values.size > 1) w / (values.size - 1) else 0f

        val points = values.mapIndexed { i, v ->
            val x = padding + stepX * i
            val y = padding + h - ((v - min) / range) * h * animated - (h * (1 - animated) / 2)
            Offset(x, y)
        }

        val fill = Path().apply {
            moveTo(points.first().x, size.height - padding)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, size.height - padding)
            close()
        }
        drawPath(
            fill,
            brush = Brush.verticalGradient(
                listOf(color.copy(alpha = 0.30f), color.copy(alpha = 0.02f))
            )
        )

        val line = Path().apply {
            points.forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) }
        }
        drawPath(line, color = color, style = Stroke(width = 5f, cap = StrokeCap.Round))
        points.forEach { p ->
            drawCircle(color = Color.White, radius = 7f, center = p)
            drawCircle(color = color, radius = 4f, center = p)
        }
    }
}

@Composable
private fun AddWeightDialog(onDismiss: () -> Unit, onConfirm: (Float) -> Unit) {
    var text by remember { mutableStateOf("") }
    val value = text.toFloatOrNull()

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Log weight", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Weight (kg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(
                        onClick = { value?.let(onConfirm) },
                        enabled = value != null && value > 0f
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
