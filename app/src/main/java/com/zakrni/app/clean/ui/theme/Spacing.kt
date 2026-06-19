package com.zakrni.app.clean.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing scale — replaces the scattered sdp/dimens usage with a single 4dp-based system.
 * Access via [LocalSpacing] inside composables, or use the default [Spacing] constants.
 */
data class Spacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,
    val huge: Dp = 48.dp,
    // common semantic gaps
    val screenPadding: Dp = 16.dp,
    val cardPadding: Dp = 16.dp,
    val itemGap: Dp = 12.dp,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }
