package com.mr.claudetraining.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mr.claudetraining.ui.components.AnimatedEntry
import com.mr.claudetraining.ui.components.CalorieRing
import com.mr.claudetraining.ui.components.MacroBar
import com.mr.claudetraining.ui.components.SimpleTopBar
import com.mr.claudetraining.ui.theme.FiteloGreen
import com.mr.claudetraining.ui.theme.FiteloGreenDark
import com.mr.claudetraining.ui.theme.FiteloOrange
import com.mr.claudetraining.ui.viewmodel.HealthUiState
import com.mr.claudetraining.ui.viewmodel.HealthViewModel
import kotlinx.coroutines.delay

@Composable
fun HealthDashboardScreen(
    onOpenActivity: () -> Unit = {},
    viewModel: HealthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
        SimpleTopBar(title = "Healthify")

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(0.dp, 12.dp, 0.dp, 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Header() }

            // Upper: continuous, auto-advancing horizontal slider of featured sessions.
            item { AnimatedEntry(index = 0) { FeaturedSlider() } }

            item { Padded { AnimatedEntry(index = 1) { CalorieHeroCard(state) } } }

            // Lower: horizontal yoga-pose row.
            item {
                AnimatedEntry(index = 2) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader("Yoga poses", "Flow at your pace")
                        PoseRow(YOGA_POSES)
                    }
                }
            }

            // Lower: horizontal gym / cardio row.
            item {
                AnimatedEntry(index = 3) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader("Strength & cardio", "Build. Burn. Repeat.")
                        PoseRow(WORKOUTS)
                    }
                }
            }

            item { Padded { AnimatedEntry(index = 4) { NetCaloriesCard(state) } } }
            item { Padded { AnimatedEntry(index = 5) { WaterCard(state.water.glasses, state.goal.waterGoalGlasses, viewModel::changeWater) } } }
            item { Padded { AnimatedEntry(index = 6) { ActivityCard(state, onClick = onOpenActivity) } } }
            item { Padded { AnimatedEntry(index = 7) { WeightCard(state) } } }
        }
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
// Existing health cards
// ---------------------------------------------------------------------------

@Composable
private fun CalorieHeroCard(state: HealthUiState) {
    val n = state.nutrition
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            CalorieRing(
                consumed = n.caloriesConsumed,
                goal = state.goal.calorieGoal,
                burned = n.caloriesBurned
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BudgetStat(Icons.Filled.Flag, "Goal", state.goal.calorieGoal, MaterialTheme.colorScheme.onSurfaceVariant)
                VDivider()
                BudgetStat(Icons.Filled.Restaurant, "Eaten", n.caloriesConsumed, MaterialTheme.colorScheme.primary)
                VDivider()
                BudgetStat(Icons.Filled.LocalFireDepartment, "Burned", n.caloriesBurned, MaterialTheme.colorScheme.secondary)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MacroBar("Protein", n.protein, state.goal.proteinGoal, MaterialTheme.colorScheme.primary)
                MacroBar("Carbs", n.carbs, state.goal.carbGoal, MaterialTheme.colorScheme.tertiary)
                MacroBar("Fat", n.fat, state.goal.fatGoal, MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
private fun BudgetStat(icon: ImageVector, label: String, value: Int, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Text("$value", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun VDivider() {
    Box(
        modifier = Modifier
            .height(36.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

@Composable
private fun NetCaloriesCard(state: HealthUiState) {
    val n = state.nutrition
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Net calories",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                "${n.net} kcal",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                "Goal ${state.goal.calorieGoal} · eaten ${n.caloriesConsumed} − burned ${n.caloriesBurned}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

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
private fun WeightCard(state: HealthUiState) {
    val latest = state.weightEntries.maxByOrNull { it.date }
    Card(
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
                    latest?.let { "Last logged ${it.date}" } ?: "Not logged yet",
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
