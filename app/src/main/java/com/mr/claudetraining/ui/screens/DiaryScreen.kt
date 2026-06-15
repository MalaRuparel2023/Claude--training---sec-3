package com.mr.claudetraining.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mr.claudetraining.domain.model.FoodEntry
import com.mr.claudetraining.domain.model.FoodItem
import com.mr.claudetraining.domain.model.Meal
import com.mr.claudetraining.ui.components.CalorieRing
import com.mr.claudetraining.ui.components.MacroBar
import com.mr.claudetraining.ui.components.SimpleTopBar
import com.mr.claudetraining.ui.viewmodel.HealthViewModel
import kotlin.math.roundToInt

@Composable
fun DiaryScreen(viewModel: HealthViewModel = hiltViewModel()) {
    val state = viewModel.uiState.collectAsState().value
    var addingTo by remember { mutableStateOf<Meal?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SimpleTopBar(title = "Diary")

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { SummaryCard(state) }

            Meal.entries.forEach { meal ->
                item(key = meal.name) {
                    MealSection(
                        meal = meal,
                        entries = state.entriesFor(meal),
                        onAdd = { addingTo = meal },
                        onDelete = { viewModel.deleteFood(it) }
                    )
                }
            }
        }
    }

    addingTo?.let { meal ->
        AddFoodDialog(
            meal = meal,
            catalog = state.foodCatalog,
            onDismiss = { addingTo = null },
            onConfirm = { item, servings ->
                viewModel.addFood(item, meal, servings)
                addingTo = null
            }
        )
    }
}

@Composable
private fun SummaryCard(state: com.mr.claudetraining.ui.viewmodel.HealthUiState) {
    val n = state.nutrition
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CalorieRing(
                consumed = n.caloriesConsumed,
                goal = state.goal.calorieGoal,
                burned = n.caloriesBurned
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CalStat("Eaten", n.caloriesConsumed)
                CalStat("Burned", n.caloriesBurned)
                CalStat("Goal", state.goal.calorieGoal)
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MacroBar("Protein", n.protein, state.goal.proteinGoal, MaterialTheme.colorScheme.primary)
                MacroBar("Carbs", n.carbs, state.goal.carbGoal, MaterialTheme.colorScheme.tertiary)
                MacroBar("Fat", n.fat, state.goal.fatGoal, MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
private fun CalStat(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MealSection(
    meal: Meal,
    entries: List<FoodEntry>,
    onAdd: () -> Unit,
    onDelete: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = meal.label(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${entries.sumOf { it.calories }} kcal",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = onAdd) {
                    Icon(Icons.Filled.Add, contentDescription = "Add to ${meal.label()}")
                }
            }

            if (entries.isEmpty()) {
                Text(
                    text = "No items yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            } else {
                entries.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.name, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "${formatServings(entry.servings)} serving · ${entry.calories} kcal",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onDelete(entry.id) }, modifier = Modifier.size(36.dp)) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete ${entry.name}",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddFoodDialog(
    meal: Meal,
    catalog: List<FoodItem>,
    onDismiss: () -> Unit,
    onConfirm: (FoodItem, Float) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<FoodItem?>(null) }
    var servings by remember { mutableStateOf(1f) }

    val filtered = remember(query, catalog) {
        if (query.isBlank()) catalog
        else catalog.filter { it.name.contains(query, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Add to ${meal.label()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                val current = selected
                if (current == null) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search foods…") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(filtered, key = { it.id }) { food ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selected = food; servings = 1f }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(food.name, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${food.servingLabel} · ${food.calories} kcal",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(current.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${current.servingLabel} · ${(current.calories * servings).roundToInt()} kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Servings", modifier = Modifier.weight(1f))
                        StepButton(Icons.Filled.Remove, "Less") {
                            servings = (servings - 0.5f).coerceAtLeast(0.5f)
                        }
                        Text(formatServings(servings), style = MaterialTheme.typography.titleMedium)
                        StepButton(Icons.Filled.Add, "More") {
                            servings += 0.5f
                        }
                    }
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { selected = null }) { Text("Back") }
                        TextButton(onClick = { onConfirm(current, servings) }) {
                            Text("Add", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepButton(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = desc, modifier = Modifier.size(18.dp))
    }
}

private fun Meal.label(): String = when (this) {
    Meal.BREAKFAST -> "Breakfast"
    Meal.LUNCH -> "Lunch"
    Meal.DINNER -> "Dinner"
    Meal.SNACK -> "Snacks"
}

private fun formatServings(value: Float): String =
    if (value % 1f == 0f) value.toInt().toString() else value.toString()