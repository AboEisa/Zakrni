package com.zakrni.app.clean.ui.theme.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

/** Entrance motion styles so different screens can feel distinct rather than uniform. */
enum class AppearStyle { FadeUp, FadeScale, FadeSlideStart }

/**
 * One-shot entrance animation for list/grid items: fades the item in with a small directional
 * (or scale) motion. Pass a staggered [delayMillis] (e.g. `index * 40`) for a cascade effect.
 */
@Composable
fun Modifier.appear(
    style: AppearStyle = AppearStyle.FadeUp,
    delayMillis: Int = 0,
): Modifier {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (delayMillis > 0) delay(delayMillis.toLong())
        shown = true
    }
    val p by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
        label = "appear",
    )
    return this.graphicsLayer {
        alpha = p
        when (style) {
            AppearStyle.FadeUp -> translationY = (1f - p) * 40f
            AppearStyle.FadeScale -> {
                val s = 0.90f + 0.10f * p
                scaleX = s
                scaleY = s
            }
            AppearStyle.FadeSlideStart -> translationX = (1f - p) * -40f
        }
    }
}
