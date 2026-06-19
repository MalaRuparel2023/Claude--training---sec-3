package com.mr.claudetraining.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mr.claudetraining.ui.components.AnimatedEntry
import com.mr.claudetraining.ui.components.SimpleTopBar
import com.mr.claudetraining.ui.theme.FiteloGreen
import com.mr.claudetraining.ui.theme.FiteloGreenDark
import com.mr.claudetraining.ui.theme.FiteloOrange
import com.mr.claudetraining.ui.viewmodel.HealthUiState
import com.mr.claudetraining.ui.viewmodel.HealthViewModel
import kotlinx.coroutines.delay

private const val ACTIVE_MINUTES_GOAL = 30

@Composable
fun HealthDashboardScreen(
    onOpenActivity: () -> Unit = {},
    viewModel: HealthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showWeightDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
        SimpleTopBar(title = "Healthify")

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(0.dp, 12.dp, 0.dp, 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Header() }

            // Auto-advancing horizontal slider of featured sessions.
            item { AnimatedEntry(index = 0) { FeaturedSlider() } }

            // Fitness summary for today (no food/diary data — that lives on the Diary tab).
            item { Padded { AnimatedEntry(index = 1) { StatsHeroCard(state) } } }

            item { Padded { AnimatedEntry(index = 2) {
                QuickActions(
                    onLogWeight = { showWeightDialog = true },
                    onAddWater = { viewModel.changeWater(1) },
                    onActivity = onOpenActivity
                )
            } } }

            item { Padded { AnimatedEntry(index = 3) { TodaysGoalsCard(state) } } }

            // Horizontal yoga-pose row.
            item {
                AnimatedEntry(index = 4) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader("Yoga poses", "Flow at your pace")
                        PoseRow(YOGA_POSES)
                    }
                }
            }

            // Horizontal gym / cardio row.
            item {
                AnimatedEntry(index = 5) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader("Strength & cardio", "Build. Burn. Repeat.")
                        PoseRow(WORKOUTS)
                    }
                }
            }

            item { Padded { AnimatedEntry(index = 6) { WaterCard(state.water.glasses, state.goal.waterGoalGlasses, viewModel::changeWater) } } }
            item { Padded { AnimatedEntry(index = 7) { ActivityCard(state, onClick = onOpenActivity) } } }
            item { Padded { AnimatedEntry(index = 8) { WeightCard(state, onClick = { showWeightDialog = true }) } } }
        }
    }

    if (showWeightDialog) {
        LogWeightDialog(
            onDismiss = { showWeightDialog = false },
            onSave = { kg ->
                viewModel.addWeight(kg)
                showWeightDialog = false
            }
        )
    }
}

/** Horizontal screen padding wrapper — the slider/rows bleed full-width, cards stay inset. */
@Composable
private fun Padded(content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(horizontal = 16.dp)) { content() }
}

@Composable
private fun Header() {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            "Today",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Let's move 🔥",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// ---------------------------------------------------------------------------
// Stats hero — today's fitness summary
// ---------------------------------------------------------------------------

@Composable
private fun StatsHeroCard(state: HealthUiState) {
    val workouts = state.workoutEntries
    val burned = workouts.sumOf { it.caloriesBurned }
    val minutes = workouts.sumOf { it.durationMinutes }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.horizontalGradient(listOf(FiteloGreen, FiteloGreenDark)))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                "Today's activity",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeroStat(Icons.Filled.LocalFireDepartment, "$burned", "kcal burned")
                HeroStat(Icons.Filled.Timer, "$minutes", "active min")
                HeroStat(Icons.Filled.FitnessCenter, "${workouts.size}", "workouts")
                HeroStat(Icons.Filled.WaterDrop, "${state.water.glasses}", "glasses")
            }
        }
    }
}

@Composable
private fun HeroStat(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
    }
}

// ---------------------------------------------------------------------------
// Quick actions
// ---------------------------------------------------------------------------

@Composable
private fun QuickActions(
    onLogWeight: () -> Unit,
    onAddWater: () -> Unit,
    onActivity: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QuickAction(Icons.Filled.MonitorWeight, "Log weight", FiteloGreen, Modifier.weight(1f), onLogWeight)
        QuickAction(Icons.Filled.WaterDrop, "Add water", MaterialTheme.colorScheme.tertiary, Modifier.weight(1f), onAddWater)
        QuickAction(Icons.Filled.FitnessCenter, "Activity", FiteloOrange, Modifier.weight(1f), onActivity)
    }
}

@Composable
private fun QuickAction(icon: ImageVector, label: String, tint: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(tint.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
            }
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
        }
    }
}

// ---------------------------------------------------------------------------
// Today's goals checklist
// ---------------------------------------------------------------------------

@Composable
private fun TodaysGoalsCard(state: HealthUiState) {
    val minutes = state.workoutEntries.sumOf { it.durationMinutes }
    val loggedWeightToday = state.weightEntries.any { it.date == state.date }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Today's goals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            GoalRow("Move $ACTIVE_MINUTES_GOAL minutes", minutes, ACTIVE_MINUTES_GOAL, "min")
            GoalRow("Drink ${state.goal.waterGoalGlasses} glasses", state.water.glasses, state.goal.waterGoalGlasses, "glasses")
            GoalCheckRow("Log today's weight", loggedWeightToday)
        }
    }
}

@Composable
private fun GoalRow(label: String, value: Int, goal: Int, unit: String) {
    val done = goal > 0 && value >= goal
    val fraction = if (goal > 0) (value.toFloat() / goal).coerceIn(0f, 1f) else 0f
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (done) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 10.dp).weight(1f))
            Text("$value / $goal $unit", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
        )
    }
}

@Composable
private fun GoalCheckRow(label: String, done: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (done) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 10.dp).weight(1f))
        Text(if (done) "Done" else "Pending", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ---------------------------------------------------------------------------
// Log weight dialog
// ---------------------------------------------------------------------------

@Composable
private fun LogWeightDialog(onDismiss: () -> Unit, onSave: (Float) -> Unit) {
    var text by remember { mutableStateOf("") }
    val weight = text.toFloatOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log weight") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Weight (kg)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { weight?.let(onSave) },
                enabled = weight != null && weight > 0f
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ---------------------------------------------------------------------------
// Featured slider (continuous auto-advance)
// ---------------------------------------------------------------------------

private data class Slide(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val colors: List<Color>
)

private val SLIDES = listOf(
    Slide("Morning Yoga Flow", "15 min · Beginner", Icons.Filled.SelfImprovement, listOf(FiteloGreen, FiteloGreenDark)),
    Slide("HIIT Fat Burn", "20 min · Advanced", Icons.Filled.LocalFireDepartment, listOf(Color(0xFFFF922B), Color(0xFFE8590C))),
    Slide("Full Body Strength", "30 min · Gym", Icons.Filled.FitnessCenter, listOf(Color(0xFF4C6EF5), Color(0xFF3B5BDB))),
    Slide("Evening Stretch", "10 min · Relax", Icons.Filled.Spa, listOf(Color(0xFF9775FA), Color(0xFF7048E8)))
)

@Composable
private fun FeaturedSlider() {
    // Start in the middle of a huge virtual range so it scrolls "continuously" both ways.
    val pageCount = Int.MAX_VALUE
    val startPage = pageCount / 2
    val pagerState = rememberPagerState(initialPage = startPage) { pageCount }

    LaunchedEffect(pagerState) {
        while (true) {
            delay(3000)
            pagerState.animateScrollToPage(
                pagerState.currentPage + 1,
                animationSpec = tween(700, easing = FastOutSlowInEasing)
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 12.dp
        ) { page ->
            SlideCard(SLIDES[page % SLIDES.size])
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val active = pagerState.currentPage % SLIDES.size
            SLIDES.indices.forEach { i ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (i == active) 9.dp else 7.dp)
                        .clip(CircleShape)
                        .background(
                            if (i == active) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
    }
}

@Composable
private fun SlideCard(slide: Slide) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.horizontalGradient(slide.colors))
    ) {
        Icon(
            slide.icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.18f),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .size(140.dp)
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Text(
                slide.subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium
            )
            Text(
                slide.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Horizontal pose / workout rows
// ---------------------------------------------------------------------------

private data class Pose(
    val name: String,
    val meta: String,
    val icon: ImageVector,
    val tint: Color
)

private val YOGA_POSES = listOf(
    Pose("Warrior II", "Strength", Icons.Filled.SelfImprovement, FiteloGreen),
    Pose("Tree Pose", "Balance", Icons.Filled.Spa, Color(0xFF7048E8)),
    Pose("Cobra", "Flexibility", Icons.Filled.SelfImprovement, Color(0xFF4C6EF5)),
    Pose("Downward Dog", "Full body", Icons.Filled.SportsGymnastics, FiteloGreenDark),
    Pose("Child's Pose", "Recovery", Icons.Filled.Spa, FiteloOrange)
)

private val WORKOUTS = listOf(
    Pose("Push Day", "Chest · Arms", Icons.Filled.FitnessCenter, Color(0xFF3B5BDB)),
    Pose("Leg Day", "Quads · Glutes", Icons.Filled.SportsGymnastics, FiteloGreen),
    Pose("Cardio HIIT", "Fat burn", Icons.AutoMirrored.Filled.DirectionsRun, FiteloOrange),
    Pose("Core Blast", "Abs", Icons.Filled.FitnessCenter, Color(0xFF7048E8)),
    Pose("Swim", "Endurance", Icons.Filled.Pool, Color(0xFF4C6EF5))
)

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PoseRow(poses: List<Pose>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(poses) { pose -> PoseCard(pose) }
    }
}

@Composable
private fun PoseCard(pose: Pose) {
    Card(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(pose.tint.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(pose.icon, contentDescription = null, tint = pose.tint, modifier = Modifier.size(28.dp))
            }
            Column {
                Text(pose.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    pose.meta,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Fitness cards
// ---------------------------------------------------------------------------

@Composable
private fun WaterCard(glasses: Int, goal: Int, onChange: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                    Text("Water", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        "$glasses of $goal glasses",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Stepper(Icons.Filled.Remove, "Remove glass") { onChange(-1) }
                Text("$glasses", modifier = Modifier.padding(horizontal = 12.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Stepper(Icons.Filled.Add, "Add glass") { onChange(1) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(goal) { i ->
                    Icon(
                        Icons.Filled.WaterDrop,
                        contentDescription = null,
                        tint = if (i < glasses) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityCard(state: HealthUiState, onClick: () -> Unit) {
    val workouts = state.workoutEntries
    val minutes = workouts.sumOf { it.durationMinutes }
    val burned = workouts.sumOf { it.caloriesBurned }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.FitnessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
                Text("Activity", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    if (workouts.isEmpty()) "No workouts logged — tap to see details"
                    else "${workouts.size} workout${if (workouts.size == 1) "" else "s"} · $minutes min",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "$burned kcal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun WeightCard(state: HealthUiState, onClick: () -> Unit) {
    val latest = state.weightEntries.maxByOrNull { it.date }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.MonitorWeight, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
            }
            Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
                Text("Weight", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    latest?.let { "Last logged ${it.date}" } ?: "Tap to log your weight",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                latest?.let { "${it.weightKg} kg" } ?: "—",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun Stepper(icon: ImageVector, desc: String, onClick: () -> Unit) {
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
