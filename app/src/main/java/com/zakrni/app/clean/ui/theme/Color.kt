package com.zakrni.app.clean.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Zakrni brand palette — single source of truth for Compose colors.
 *
 * Brand identity (carried over from the original XML design):
 *  - Deep Teal Green  #1A3C34  (headers / nav / depth)
 *  - Gold             #D4AF37  (accent / highlights)
 *  - Cream            #FEFBF4  (warm background)
 *  - Green            #2E7D32  (primary action)
 */

// ---- Raw brand tokens (use sparingly; prefer ColorScheme roles) ----
val BrandTeal = Color(0xFF1A3C34)
val BrandTealDark = Color(0xFF0D2818)
val BrandGold = Color(0xFFD4AF37)
val BrandGoldSoft = Color(0xFFF0C040)
val BrandCream = Color(0xFFFEFBF4)
val BrandGreen = Color(0xFF2E7D32)
val BrandGreenDark = Color(0xFF1B5E20)
val BrandGreenLight = Color(0xFF4CAF50)

// ---- Light scheme roles ----
val LightPrimary = BrandGreen
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFA8DAB5)
val LightOnPrimaryContainer = Color(0xFF00210A)

val LightSecondary = BrandGold
val LightOnSecondary = Color(0xFF1A1A1A)
val LightSecondaryContainer = Color(0xFFFFF3D1)
val LightOnSecondaryContainer = Color(0xFF3D2E00)

val LightTertiary = BrandTeal
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFC8E6C9)
val LightOnTertiaryContainer = Color(0xFF00210A)

val LightBackground = BrandCream
val LightOnBackground = Color(0xFF1A1C18)
val LightSurface = Color(0xFFFEFBFF)
val LightOnSurface = Color(0xFF1A1C18)
val LightSurfaceVariant = Color(0xFFF4F7F0)
val LightOnSurfaceVariant = Color(0xFF6A706B)
val LightSurfaceContainer = Color(0xFFF3F1EA)
val LightOutline = Color(0xFFD9DDD6)
val LightOutlineVariant = Color(0xFFEDF0E9)

val LightError = Color(0xFFB00020)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF410002)

// ---- Dark scheme roles ----
val DarkPrimary = Color(0xFF7FD389)
val DarkOnPrimary = Color(0xFF00390F)
val DarkPrimaryContainer = Color(0xFF1B5E20)
val DarkOnPrimaryContainer = Color(0xFFC8F3CD)

val DarkSecondary = Color(0xFFE6C158)
val DarkOnSecondary = Color(0xFF3A2F00)
val DarkSecondaryContainer = Color(0xFF5A4A12)
val DarkOnSecondaryContainer = Color(0xFFFFF3D1)

val DarkTertiary = Color(0xFF4DB6AC)
val DarkOnTertiary = Color(0xFF00322C)
val DarkTertiaryContainer = Color(0xFF14463F)
val DarkOnTertiaryContainer = Color(0xFFB2DFDB)

val DarkBackground = Color(0xFF101411)
val DarkOnBackground = Color(0xFFE2E3DE)
val DarkSurface = Color(0xFF1A1E1B)
val DarkOnSurface = Color(0xFFE2E3DE)
val DarkSurfaceVariant = Color(0xFF2A2F2B)
val DarkOnSurfaceVariant = Color(0xFFBFC9BF)
val DarkSurfaceContainer = Color(0xFF22271F)
val DarkOutline = Color(0xFF8A938B)
val DarkOutlineVariant = Color(0xFF3F4A41)

val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

// ---- Shared semantic accents (same in both themes) ----
val SuccessGreen = Color(0xFF27AE60)
val WarningAmber = Color(0xFFE67E22)
val InfoBlue = Color(0xFF3498DB)
