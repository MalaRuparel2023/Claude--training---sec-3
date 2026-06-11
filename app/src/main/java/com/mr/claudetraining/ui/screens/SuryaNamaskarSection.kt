package com.mr.claudetraining.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

data class SuryaPose(
    val step: Int,
    val sanskritName: String,
    val englishName: String,
    val description: String,
    val imageUrl: String
)

private fun commons(file: String) = "https://commons.wikimedia.org/wiki/Special:FilePath/$file"

// The 12 positions of Surya Namaskar (Sun Salutation), in sequence.
val suryaNamaskarPoses = listOf(
    SuryaPose(1, "Pranamasana", "Prayer Pose", "Stand tall with palms pressed together at the heart; steady your breath.", commons("Pranamasana.jpg")),
    SuryaPose(2, "Hasta Uttanasana", "Raised Arms Pose", "Inhale, stretch the arms overhead and arch gently backward.", commons("Hasta Uttanasana.jpg")),
    SuryaPose(3, "Padahastasana", "Standing Forward Bend", "Exhale and fold forward, bringing the hands beside the feet.", commons("Uttanasana.jpg")),
    SuryaPose(4, "Ashwa Sanchalanasana", "Equestrian Pose", "Inhale, step the right leg back into a low lunge and lift the gaze.", commons("Ashwa Sanchalanasana.jpg")),
    SuryaPose(5, "Dandasana", "Plank Pose", "Hold the breath and bring the body into a straight plank line.", commons("Dandasana.jpg")),
    SuryaPose(6, "Ashtanga Namaskara", "Eight-Limbed Pose", "Exhale, lower knees, chest and chin to the floor.", commons("Ashtanga Namaskara.jpg")),
    SuryaPose(7, "Bhujangasana", "Cobra Pose", "Inhale, slide forward and lift the chest into cobra.", commons("Bhujangasana Yoga-Asana Nina-Mel.jpg")),
    SuryaPose(8, "Adho Mukha Svanasana", "Downward Dog", "Exhale, lift the hips up and back into an inverted V.", commons("Adho Mukha Svanasana.jpg")),
    SuryaPose(9, "Ashwa Sanchalanasana", "Equestrian Pose", "Inhale, step the right foot forward between the hands.", commons("Ashwa Sanchalanasana.jpg")),
    SuryaPose(10, "Padahastasana", "Standing Forward Bend", "Exhale, bring the left foot forward and fold over the legs.", commons("Uttanasana.jpg")),
    SuryaPose(11, "Hasta Uttanasana", "Raised Arms Pose", "Inhale, rise up and stretch the arms overhead.", commons("Hasta Uttanasana.jpg")),
    SuryaPose(12, "Tadasana", "Mountain Pose", "Exhale, return to standing with palms together at the heart.", commons("Tadasana.jpg"))
)

@Composable
fun SuryaNamaskarSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column {
            Text(
                "Surya Namaskar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "12 poses · Sun Salutation flow",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LazyRow(
            contentPadding = PaddingValues(end = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(suryaNamaskarPoses, key = { it.step }) { PoseCard(it) }
        }
    }
}

@Composable
private fun PoseCard(pose: SuryaPose) {
    Card(
        modifier = Modifier.width(180.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                // Fallback icon shown if the remote image fails to load.
                Icon(
                    Icons.Filled.SelfImprovement,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.size(56.dp)
                )
                AsyncImage(
                    model = pose.imageUrl,
                    contentDescription = pose.englishName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .align(Alignment.TopStart),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        pose.step.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    pose.englishName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    pose.sanskritName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    pose.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}