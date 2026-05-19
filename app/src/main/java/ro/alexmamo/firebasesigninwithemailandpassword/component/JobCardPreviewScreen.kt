package com.mala.training.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ro.alexmamo.firebasesigninwithemailandpassword.theme.MyApplicationTheme
import ro.alexmamo.firebasesigninwithemailandpassword.component.JobCard

private val sampleJobs = listOf(
    JobData("Senior Android Engineer", "Acme Corp", "San Francisco, CA", "\$140k – \$180k"),
    JobData(
        "Staff Mobile Engineer — very long title that should ellipsize gracefully on small screens",
        "Globex Corporation",
        "Remote",
        null,
    ),
    JobData("Junior Kotlin Developer", "Initech", "New York, NY (Hybrid)", "\$80k – \$100k"),
    JobData("Lead Android Architect", "Umbrella Tech", "Austin, TX", "\$160k – \$200k + equity"),
)

private data class JobData(
    val title: String,
    val company: String,
    val location: String,
    val salary: String?,
)

@Preview(showBackground = true, name = "JobCard Feed — Light")
@Composable
fun JobCardFeedLightPreview() {
    MyApplicationTheme(darkTheme = false, dynamicColor = false) {
        JobCardFeed()
    }
}

@Preview(
    showBackground = true,
    name = "JobCard Feed — Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun JobCardFeedDarkPreview() {
    MyApplicationTheme(darkTheme = true, dynamicColor = false) {
        JobCardFeed()
    }
}

@Composable
private fun JobCardFeed() {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(sampleJobs) { job ->
            JobCard(
                jobTitle = job.title,
                company = job.company,
                location = job.location,
                salary = job.salary,
                onQuickApply = {},
            )
        }
    }
}