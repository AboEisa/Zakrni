package com.zakrni.app.clean.ui.compose.azkardua

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import java.util.Locale

/**
 * Whether the current UI configuration is Arabic. Matches the legacy
 * [com.zakrni.app.clean.ui.utils.LocaleHelper.isArabic] used by the adapters, but reads from the
 * Compose [LocalConfiguration] so it recomposes when the app locale changes.
 */
@Composable
internal fun isArabicUi(): Boolean {
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0] ?: Locale.getDefault()
    return locale.language.equals("ar", ignoreCase = true)
}

/**
 * Rounded surface used for an individual azkar / dua entry inside an expanded section.
 * Centralizes the per-item card look so both screens stay consistent.
 */
@Composable
internal fun AzkarDuaItemSurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), content = content)
    }
}
