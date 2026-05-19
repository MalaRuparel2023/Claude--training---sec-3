package com.mala.training.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ro.alexmamo.firebasesigninwithemailandpassword.theme.MyApplicationTheme

@Composable
private fun SearchInputBarPreviewScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Empty state — shows placeholder
            SearchInputBar(
                query = "",
                onQueryChange = {},
                onClearClick = {},
                placeholder = "Search jobs",
            )
            // Active query — shows clear button
            SearchInputBar(
                query = "Android",
                onQueryChange = {},
                onClearClick = {},
                placeholder = "Search jobs",
            )
            // Long query — verifies singleLine truncation
            SearchInputBar(
                query = "Senior Android Engineer with Jetpack Compose experience in San Francisco",
                onQueryChange = {},
                onClearClick = {},
                placeholder = "Search people",
            )
            // Custom placeholder
            SearchInputBar(
                query = "",
                onQueryChange = {},
                onClearClick = {},
                placeholder = "Search people by name or handle",
            )
        }
    }
}

@Preview(showBackground = true, name = "SearchInputBarScreen — Light")
@Composable
private fun SearchInputBarScreenLightPreview() {
    MyApplicationTheme(darkTheme = false, dynamicColor = false) {
        SearchInputBarPreviewScreen()
    }
}

@Preview(
    showBackground = true,
    name = "SearchInputBarScreen — Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SearchInputBarScreenDarkPreview() {
    MyApplicationTheme(darkTheme = true, dynamicColor = false) {
        SearchInputBarPreviewScreen()
    }
}