package com.mr.claudetraining.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Fades + slides its content up on first composition. [index] staggers the start so a list of
 * entries cascades in. Used across the Home, Yoga and Profile screens for a consistent feel.
 */
@Composable
fun AnimatedEntry(
    index: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 400, delayMillis = index * 80)) +
            slideInVertically(
                animationSpec = tween(durationMillis = 400, delayMillis = index * 80),
                initialOffsetY = { it / 4 }
            )
    ) {
        content()
    }
}