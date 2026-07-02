package com.mr.claudetraining.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mr.claudetraining.domain.model.EnhancedJob
import com.mr.claudetraining.domain.model.Job
import com.mr.claudetraining.domain.model.JobType

/**
 * A single job posting row: title, company · location, type/remote, salary, and a bookmark toggle.
 * Reusable across the job list and saved list. Colors come from the theme (dark-mode correct);
 * the bookmark cross-fades and pulses when toggled, and its `contentDescription` reflects state.
 */
@Composable
fun JobCard(
    enhanced: EnhancedJob,
    onToggleSave: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val job = enhanced.job
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = job.title.ifBlank { "Untitled role" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = listOf(job.company, job.location).filter { it.isNotBlank() }
                        .joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = job.metaLine(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            BookmarkToggle(saved = enhanced.isFavorite, onToggle = { onToggleSave(!enhanced.isFavorite) })
        }
    }
}

@Composable
private fun BookmarkToggle(saved: Boolean, onToggle: () -> Unit) {
    // A subtle pulse each time the saved state flips.
    val scale by animateFloatAsState(
        targetValue = if (saved) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "bookmarkScale"
    )
    IconButton(onClick = onToggle, modifier = Modifier.scale(scale)) {
        Crossfade(targetState = saved, label = "bookmarkIcon") { isSaved ->
            Icon(
                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                contentDescription = if (isSaved) "Saved" else "Save",
                tint = if (isSaved) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun Job.metaLine(): String {
    val type = when (type) {
        JobType.FULL_TIME -> "Full-time"
        JobType.PART_TIME -> "Part-time"
        JobType.CONTRACT -> "Contract"
        JobType.INTERNSHIP -> "Internship"
    }
    val remote = if (remote) "Remote" else null
    val salary = if (salaryMin > 0 && salaryMax > 0) "€${salaryMin / 1000}k–${salaryMax / 1000}k" else null
    return listOfNotNull(type, remote, salary).joinToString(" · ")
}
