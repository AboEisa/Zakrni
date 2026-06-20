package com.zakrni.app.clean.ui.compose.quran

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

/**
 * Whether the current UI configuration is Arabic. Mirrors the legacy
 * [com.zakrni.app.clean.ui.utils.LocaleHelper.isArabic] used by the fragments, but reads from the
 * Compose [LocalConfiguration] so it recomposes when the app locale changes.
 */
@Composable
internal fun isArabicUi(): Boolean {
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0] ?: Locale.getDefault()
    return locale.language.equals("ar", ignoreCase = true)
}

private val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

/** Renders [value] with Arabic-Indic digits when [arabic], otherwise plain Latin digits. */
internal fun localizeNumber(value: Int, arabic: Boolean): String {
    if (!arabic) return value.toString()
    return buildString {
        for (ch in value.toString()) {
            append(if (ch in '0'..'9') arabicDigits[ch - '0'] else ch)
        }
    }
}

/** Formats a millisecond position as `m:ss`, with locale-aware digits. */
internal fun formatAudioTime(milliseconds: Int, arabic: Boolean): String {
    val safe = milliseconds.coerceAtLeast(0)
    val totalSeconds = safe / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val latin = String.format(Locale.US, "%d:%02d", minutes, seconds)
    if (!arabic) return latin
    return buildString {
        for (ch in latin) {
            append(if (ch in '0'..'9') arabicDigits[ch - '0'] else ch)
        }
    }
}
