package com.zakrni.app.clean.ui.compose.hijri

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zakrni.app.R
import com.zakrni.app.clean.ui.theme.BrandGold
import com.zakrni.app.clean.ui.theme.components.EmptyState
import com.zakrni.app.clean.ui.theme.components.ZCard
import com.zakrni.app.clean.ui.theme.components.ZSectionHeader
import com.zakrni.app.clean.ui.theme.components.ZTopBar
import java.util.Calendar

/** A single cell in the month grid. [hijriDay] == 0 marks a leading/trailing blank. */
private data class DayCell(
    val hijriDay: Int,
    val hijriDate: HijriDate?,
    val isToday: Boolean,
    val event: IslamicEvent?,
)

@Composable
fun HijriCalendarScreen(
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val today = remember { HijriCalendarConverter.toHijri(Calendar.getInstance()) }

    // Visible month is tracked as (year, month). direction drives the slide animation.
    var year by remember { mutableStateOf(today.year) }
    var month by remember { mutableStateOf(today.month) }
    var direction by remember { mutableStateOf(0) } // -1 prev, +1 next
    var selected by remember { mutableStateOf<HijriDate?>(null) }

    fun goNext() {
        direction = 1
        if (month == 12) { month = 1; year += 1 } else month += 1
    }
    fun goPrev() {
        direction = -1
        if (month == 1) { month = 12; year -= 1 } else month -= 1
    }

    Column(modifier = modifier.fillMaxSize()) {
        ZTopBar(title = stringResource(R.string.hijri_title), onBack = onBack)

        MonthHeader(
            year = year,
            month = month,
            onPrev = { goPrev() },
            onNext = { goNext() },
        )

        WeekdayRow()

        AnimatedContent(
            targetState = year to month,
            transitionSpec = {
                val dir = direction
                (slideInHorizontally(tween(300)) { w -> dir * w } + fadeIn(tween(300)))
                    .togetherWith(
                        slideOutHorizontally(tween(300)) { w -> -dir * w } + fadeOut(tween(300)),
                    )
                    .using(SizeTransform(clip = false))
            },
            label = "month-grid",
        ) { (y, m) ->
            MonthGrid(
                year = y,
                month = m,
                today = today,
                onDayClick = { selected = it },
            )
        }

        EventsSection(month = month)
    }

    selected?.let { date ->
        DayDetailDialog(date = date, onDismiss = { selected = null })
    }
}

@Composable
private fun MonthHeader(
    year: Int,
    month: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    val info = HijriMonth.entries[month - 1]
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        NavArrow(Icons.AutoMirrored.Filled.KeyboardArrowRight, onClick = onPrev)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${info.arabic} $year ${stringResource(R.string.hijri_ah)}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = info.transliteration,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        NavArrow(Icons.AutoMirrored.Filled.KeyboardArrowLeft, onClick = onNext)
    }
}

@Composable
private fun NavArrow(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun WeekdayRow() {
    val labels = stringArrayResource(R.array.hijri_weekday_short)
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        labels.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f).padding(vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun MonthGrid(
    year: Int,
    month: Int,
    today: HijriDate,
    onDayClick: (HijriDate) -> Unit,
) {
    val cells = remember(year, month) { buildCells(year, month, today) }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { cell ->
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(3.dp)) {
                        if (cell.hijriDay > 0 && cell.hijriDate != null) {
                            DayBox(cell = cell, onClick = { onDayClick(cell.hijriDate) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayBox(cell: DayCell, onClick: () -> Unit) {
    val bg = when {
        cell.isToday -> MaterialTheme.colorScheme.primary
        cell.event != null -> BrandGold.copy(alpha = 0.18f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val fg = if (cell.isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val gregDay = cell.hijriDate
        ?.let { HijriCalendarConverter.toGregorian(it).get(Calendar.DAY_OF_MONTH) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg, RoundedCornerShape(12.dp))
            .then(
                if (cell.event != null && !cell.isToday) {
                    Modifier.border(1.dp, BrandGold, RoundedCornerShape(12.dp))
                } else Modifier,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = cell.hijriDay.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = fg,
                fontWeight = if (cell.isToday) FontWeight.Bold else FontWeight.Normal,
            )
            if (gregDay != null) {
                Text(
                    text = gregDay.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (cell.isToday) {
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            if (cell.event != null) {
                Box(
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .size(4.dp)
                        .background(
                            if (cell.isToday) MaterialTheme.colorScheme.onPrimary else BrandGold,
                            CircleShape,
                        ),
                )
            }
        }
    }
}

@Composable
private fun EventsSection(month: Int) {
    val events = remember(month) { IslamicEvents.forMonth(month) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        ZSectionHeader(title = stringResource(R.string.hijri_events_title))
        Spacer(Modifier.height(8.dp))
        if (events.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Event,
                title = stringResource(R.string.hijri_events_empty),
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(events) { event -> EventRow(event) }
            }
        }
    }
}

@Composable
private fun EventRow(event: IslamicEvent) {
    val monthInfo = HijriMonth.entries[event.month - 1]
    ZCard(contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(BrandGold.copy(alpha = 0.16f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = event.day.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(event.titleRes),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${event.day} ${monthInfo.arabic} · ${stringResource(event.subtitleRes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DayDetailDialog(date: HijriDate, onDismiss: () -> Unit) {
    val greg = remember(date) { HijriCalendarConverter.toGregorian(date) }
    val info = date.monthInfo
    val event = remember(date) { IslamicEvents.forDate(date) }
    val gregMonths = stringArrayResource(R.array.hijri_gregorian_months)
    val gregText = "${greg.get(Calendar.DAY_OF_MONTH)} " +
        "${gregMonths[greg.get(Calendar.MONTH)]} ${greg.get(Calendar.YEAR)}"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "${date.day} ${info.arabic} ${date.year}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "${date.day} ${info.transliteration} ${date.year} ${stringResource(R.string.hijri_ah)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.hijri_gregorian_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = gregText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                if (event != null) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BrandGold.copy(alpha = 0.16f),
                    ) {
                        Text(
                            text = stringResource(event.titleRes),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.hijri_close))
                }
            }
        }
    }
}

/**
 * Build the 6x7 (or fewer) grid of [DayCell]s for the given Hijri month, with leading blanks so
 * day 1 lands under its Gregorian weekday column. Weekday columns are Sun..Sat to match
 * [Calendar.DAY_OF_WEEK].
 */
private fun buildCells(year: Int, month: Int, today: HijriDate): List<DayCell> {
    val length = HijriCalendarConverter.lengthOfMonth(year, month)
    val firstDow = HijriCalendarConverter.firstDayOfWeek(year, month) // 1=Sun..7=Sat
    val leading = firstDow - 1

    val cells = ArrayList<DayCell>(leading + length)
    repeat(leading) { cells.add(DayCell(0, null, isToday = false, event = null)) }
    for (day in 1..length) {
        val date = HijriDate(year, month, day)
        cells.add(
            DayCell(
                hijriDay = day,
                hijriDate = date,
                isToday = date == today,
                event = IslamicEvents.forDate(date),
            ),
        )
    }
    // Pad to a whole number of weeks for a stable grid.
    while (cells.size % 7 != 0) {
        cells.add(DayCell(0, null, isToday = false, event = null))
    }
    return cells
}
