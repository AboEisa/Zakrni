package com.zakrni.app.clean.ui.compose.fasting

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zakrni.app.R
import com.zakrni.app.clean.ui.theme.components.ZCard
import com.zakrni.app.clean.ui.theme.components.ZTopBar
import java.util.Calendar

private const val PREFS = "zakrni_fasting"
private const val KEY_FASTED = "fasted_dates"
private const val KEY_JUZ = "khatma_juz"
private const val KEY_KHATMA_DAYS = "khatma_days"
private const val JUZ_TOTAL = 30

/**
 * Self-contained "Fasting & Khatma" tracker. Persists to a private SharedPreferences (no Hilt/Room):
 *  - Fasting: a set of "yyyy-M-d" strings the user marked as fasted, shown as the current month grid.
 *  - Khatma: a set of completed juz numbers (1..30) with a progress bar.
 */
@Composable
fun FastingKhatmaScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val arabic = isArabicLocale()
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }

    Column(modifier = Modifier.fillMaxSize()) {
        ZTopBar(title = stringResource(R.string.rd_cat_fasting), onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FastingSection(prefs, arabic)
            KhatmaSection(prefs, arabic)
        }
    }
}

@Composable
private fun FastingSection(prefs: SharedPreferences, arabic: Boolean) {
    val cal = remember { Calendar.getInstance() }
    val month = cal.get(Calendar.MONTH)
    val year = cal.get(Calendar.YEAR)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val today = cal.get(Calendar.DAY_OF_MONTH)
    val prefix = "$year-${month + 1}-"

    var fasted by remember { mutableStateOf(prefs.getStringSet(KEY_FASTED, emptySet())!!.toSet()) }
    fun toggle(day: Int) {
        val key = "$prefix$day"
        val next = if (key in fasted) fasted - key else fasted + key
        fasted = next
        prefs.edit().putStringSet(KEY_FASTED, next).apply()
    }
    val monthCount = fasted.count { it.startsWith(prefix) }
    val fastedToday = "$prefix$today" in fasted

    ZCard(contentPadding = 16.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionHeader(Icons.Filled.DarkMode, stringResource(R.string.fk_fasting_title))
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.fk_fasting_month_count, monthCount),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.fk_fasting_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
            Surface(
                onClick = { toggle(today) },
                shape = RoundedCornerShape(14.dp),
                color = if (fastedToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (fastedToday) Icons.Filled.Check else Icons.Filled.Add,
                        contentDescription = null,
                        tint = if (fastedToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = stringResource(R.string.fk_fasting_today),
                        style = MaterialTheme.typography.titleSmall,
                        color = if (fastedToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            (1..daysInMonth).toList().chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    week.forEach { day ->
                        DayCell(
                            label = localizeNum(day, arabic),
                            filled = "$prefix$day" in fasted,
                            outlined = day == today,
                            onClick = { toggle(day) },
                        )
                    }
                    repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun KhatmaSection(prefs: SharedPreferences, arabic: Boolean) {
    var done by remember {
        mutableStateOf(prefs.getStringSet(KEY_JUZ, emptySet())!!.mapNotNull { it.toIntOrNull() }.toSet())
    }
    fun toggleJuz(j: Int) {
        val next = if (j in done) done - j else done + j
        done = next
        prefs.edit().putStringSet(KEY_JUZ, next.map { it.toString() }.toSet()).apply()
    }
    fun reset() {
        done = emptySet()
        prefs.edit().remove(KEY_JUZ).apply()
    }
    val count = done.size
    var planDays by remember { mutableStateOf(prefs.getInt(KEY_KHATMA_DAYS, 30)) }

    ZCard(contentPadding = 16.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                SectionHeader(Icons.AutoMirrored.Filled.MenuBook, stringResource(R.string.fk_khatma_title), Modifier.weight(1f))
                TextButton(onClick = { reset() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(stringResource(R.string.fk_khatma_reset), style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { count.toFloat() / JUZ_TOTAL },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (count >= JUZ_TOTAL) {
                    stringResource(R.string.fk_khatma_complete)
                } else {
                    stringResource(R.string.fk_khatma_progress, count)
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            if (count in 1 until JUZ_TOTAL) {
                Text(
                    text = stringResource(R.string.fk_khatma_remaining, JUZ_TOTAL - count),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.fk_khatma_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Reading plan: pick a target number of days -> suggested daily juz.
            Spacer(Modifier.height(14.dp))
            Text(stringResource(R.string.fk_plan_title), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(7, 10, 15, 30).forEach { d ->
                    val sel = planDays == d
                    Surface(
                        modifier = Modifier.weight(1f).clickable {
                            planDays = d
                            prefs.edit().putInt(KEY_KHATMA_DAYS, d).apply()
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    ) {
                        Text(
                            text = localizeNum(d, arabic),
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (count >= JUZ_TOTAL) stringResource(R.string.fk_khatma_complete)
                else stringResource(
                    R.string.fk_plan_daily,
                    localizeNum(kotlin.math.ceil((JUZ_TOTAL - count).toDouble() / planDays).toInt().coerceAtLeast(1), arabic),
                    localizeNum(planDays, arabic),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(14.dp))
            (1..JUZ_TOTAL).toList().chunked(6).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    row.forEach { j ->
                        DayCell(
                            label = localizeNum(j, arabic),
                            filled = j in done,
                            outlined = false,
                            onClick = { toggleJuz(j) },
                        )
                    }
                    repeat(6 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.size(10.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun RowScope.DayCell(label: String, filled: Boolean, outlined: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (filled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            )
            .then(
                if (outlined) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                else Modifier,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            color = if (filled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun isArabicLocale(): Boolean {
    val config = LocalConfiguration.current
    @Suppress("DEPRECATION")
    return config.locale.language == "ar"
}

private fun localizeNum(value: Int, arabic: Boolean): String {
    val s = value.toString()
    if (!arabic) return s
    return s.map { c -> if (c in '0'..'9') ('٠' + (c - '0')) else c }.joinToString("")
}
