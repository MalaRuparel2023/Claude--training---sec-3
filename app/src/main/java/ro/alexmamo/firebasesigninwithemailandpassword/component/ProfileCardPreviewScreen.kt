package com.mala.training.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ro.alexmamo.firebasesigninwithemailandpassword.theme.MyApplicationTheme

private data class ProfileCardSample(
    val initials: String,
    val name: String,
    val handle: String,
    val isFollowing: Boolean,
)

private val sampleProfiles = listOf(
    ProfileCardSample("MR", "Mala Ruparel", "@mala", isFollowing = false),
    ProfileCardSample("JS", "Jane Smith", "@janesmith", isFollowing = true),
    ProfileCardSample("AK", "Alexander König", "@alexanderkoenig_design", isFollowing = false),
    ProfileCardSample(
        "LM",
        "Longest Possible Name That Should Ellipsize On A Small Screen",
        "@longhandle_that_overflows",
        isFollowing = true,
    ),
)

@Composable
private fun ProfileCardPreviewScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(sampleProfiles) { profile ->
                ProfileCard(
                    avatarInitials = profile.initials,
                    name = profile.name,
                    handle = profile.handle,
                    isFollowing = profile.isFollowing,
                    onFollowClick = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "ProfileCardScreen — Light")
@Composable
private fun ProfileCardScreenLightPreview() {
    MyApplicationTheme(darkTheme = false, dynamicColor = false) {
        ProfileCardPreviewScreen()
    }
}

@Preview(
    showBackground = true,
    name = "ProfileCardScreen — Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ProfileCardScreenDarkPreview() {
    MyApplicationTheme(darkTheme = true, dynamicColor = false) {
        ProfileCardPreviewScreen()
    }
}