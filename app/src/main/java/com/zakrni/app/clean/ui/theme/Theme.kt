package com.zakrni.app.clean.ui.theme

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

private val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainer = LightSurfaceContainer,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainer = DarkSurfaceContainer,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
)

/**
 * Root theme for the whole Compose UI. Dark mode follows the app configuration, which the
 * existing [com.zakrni.app.clean.ui.utils.ThemeManager] drives via AppCompatDelegate night mode.
 */
@Composable
fun ZakrniTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            @Suppress("DEPRECATION")
            window.statusBarColor = (if (darkTheme) Color(0xFF081512) else Color(0xFFA9D6CC)).toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = (if (darkTheme) Color(0xFF06100E) else Color(0xFFB4DCD1)).toArgb()
            val insets = WindowCompat.getInsetsController(window, view)
            insets.isAppearanceLightStatusBars = !darkTheme
            insets.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    val hazeState = remember { HazeState() }
    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalHazeState provides hazeState,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ZakrniTypography,
            shapes = ZakrniShapes,
        ) {
            // Rich, colourful backdrop (the "wallpaper") that the frosted glass blurs — iOS-style.
            Box(Modifier.fillMaxSize().haze(state = hazeState)) {
                Box(Modifier.fillMaxSize().background(glassBackgroundBrush(darkTheme)))
                Box(Modifier.fillMaxSize().background(glassBlobTop(darkTheme)))
                Box(Modifier.fillMaxSize().background(glassBlobBottom(darkTheme)))
                content()
            }
        }
    }
}

/** App-wide [HazeState] so shared glass surfaces (cards, bars, sheets) can blur content behind them. */
val LocalHazeState = compositionLocalOf<HazeState?> { null }

/** Apply real GPU backdrop-blur to a glass surface when a [HazeState] is available. */
@Composable
fun Modifier.glassChild(shape: Shape): Modifier {
    val state = LocalHazeState.current ?: return this
    return this.clip(shape).hazeChild(state = state)
}

/** Rich base gradient — the "wallpaper" the frosted glass blurs. */
fun glassBackgroundBrush(dark: Boolean): Brush = Brush.linearGradient(
    if (dark) listOf(Color(0xFF081512), Color(0xFF0D201A), Color(0xFF06100E))
    else listOf(Color(0xFFA9D6CC), Color(0xFFCFE9E1), Color(0xFFB4DCD1)),
)

/** Warm gold glow (top-right) seen blurred through the glass — the colour you see in iOS glass. */
fun glassBlobTop(dark: Boolean): Brush = Brush.radialGradient(
    colors = listOf(BrandGold.copy(alpha = if (dark) 0.34f else 0.26f), Color.Transparent),
    center = Offset(950f, 280f), radius = 780f,
)

/** Emerald glow (bottom-left) seen blurred through the glass. */
fun glassBlobBottom(dark: Boolean): Brush = Brush.radialGradient(
    colors = listOf(Color(0xFF1E9E86).copy(alpha = if (dark) 0.42f else 0.28f), Color.Transparent),
    center = Offset(130f, 1850f), radius = 900f,
)

/** Translucent frosted surface color for glass cards/bars over [glassBackgroundBrush]. */
val androidx.compose.material3.ColorScheme.glassSurface: Color
    get() = if (surface.luminanceIsDark()) surface.copy(alpha = 0.36f) else Color.White.copy(alpha = 0.34f)

val androidx.compose.material3.ColorScheme.glassBorder: Color
    get() = Color.White.copy(alpha = 0.45f)

private fun Color.luminanceIsDark(): Boolean =
    (0.299f * red + 0.587f * green + 0.114f * blue) < 0.5f
