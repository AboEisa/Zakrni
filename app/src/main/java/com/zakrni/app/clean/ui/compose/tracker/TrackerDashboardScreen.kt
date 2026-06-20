package com.zakrni.app.clean.ui.compose.tracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zakrni.app.R
import com.zakrni.app.clean.data.tracker.Prayer
import com.zakrni.app.clean.ui.theme.components.ZCard
import com.zakrni.app.clean.ui.theme.components.ZSectionHeader
import com.zakrni.app.clean.ui.theme.components.ZTopBar

/**
 * Main worship-tracker dashboard:
 *  - today's five prayers as toggleable check items,
 *  - an animated progress ring + current daily streak,
 *  - a Canvas completion heatmap (last 5 weeks) with a monthly summary + motivational text.
 *
 * Stateless host wires the [TrackerViewModel] to the pure [TrackerDashboardContent].
 */
@Composable
fun TrackerDashboardScreen(
    onBack: (() -> Unit)? = null,
    viewModel: TrackerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TrackerDashboardContent(
        state = state,
        onBack = onBack,
        onTogglePrayer = viewModel::togglePrayer,
    )
}

@Composable
private fun TrackerDashboardContent(
    state: TrackerUiState,
    onBack: (() -> Unit)?,
    onTogglePrayer: (Prayer) -> Unit,
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    Column(modifier = Modifier.fillMaxSize()) {
        ZTopBar(title = stringResource(R.string.tracker_title), onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StreakHeaderCard(state = state)
            TodayPrayersCard(state = state, onTogglePrayer = onTogglePrayer)
            HeatmapCard(state = state, isRtl = isRtl)
        }
    }
}

/** Animated ring + streak count + motivational line. */
@Composable
private fun StreakHeaderCard(state: TrackerUiState) {
    ZCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProgressRing(
                progress = state.todayProgress,
                centerLabel = "${state.todayPrayedCount}/5",
                centerSubLabel = stringResource(R.string.tracker_today_prayers),
                contentDescription = stringResource(R.string.tracker_cd_progress_ring),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.tracker_streak_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = streakLabel(state.currentStreak),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = motivationLine(state),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

/** Today's five prayers as toggleable check items. */
@Composable
private fun TodayPrayersCard(
    state: TrackerUiState,
    onTogglePrayer: (Prayer) -> Unit,
) {
    val checkedDesc = stringResource(R.string.tracker_cd_prayer_checked)
    val uncheckedDesc = stringResource(R.string.tracker_cd_prayer_unchecked)

    Column {
        ZSectionHeader(title = stringResource(R.string.tracker_today_prayers))
        Spacer(Modifier.height(8.dp))
        ZCard(modifier = Modifier.fillMaxWidth()) {
            PrayerOrder.forEach { (prayer, labelRes) ->
                val checked = state.isPrayed(prayer)
                PrayerCheckItem(
                    label = stringResource(labelRes),
                    checked = checked,
                    stateDescription = if (checked) checkedDesc else uncheckedDesc,
                    onToggle = { onTogglePrayer(prayer) },
                )
            }
        }
    }
}

/** Weekly/monthly completion heatmap + summary + legend. */
@Composable
private fun HeatmapCard(state: TrackerUiState, isRtl: Boolean) {
    Column {
        ZSectionHeader(title = stringResource(R.string.tracker_heatmap_title))
        Spacer(Modifier.height(8.dp))
        ZCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = pluralOrSummary(state.completedInWindow),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(12.dp))
            CompletionHeatmap(
                days = state.heatmap,
                isRtl = isRtl,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            HeatmapLegend(
                lessLabel = stringResource(R.string.tracker_heatmap_legend_less),
                moreLabel = stringResource(R.string.tracker_heatmap_legend_more),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.tracker_streak_encourage),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Canonical daily order with the matching string resource for each prayer. */
private val PrayerOrder: List<Pair<Prayer, Int>> = listOf(
    Prayer.FAJR to R.string.tracker_prayer_fajr,
    Prayer.DHUHR to R.string.tracker_prayer_dhuhr,
    Prayer.ASR to R.string.tracker_prayer_asr,
    Prayer.MAGHRIB to R.string.tracker_prayer_maghrib,
    Prayer.ISHA to R.string.tracker_prayer_isha,
)

@Composable
private fun streakLabel(streak: Int): String = when (streak) {
    0 -> stringResource(R.string.tracker_streak_zero)
    1 -> stringResource(R.string.tracker_streak_one_day)
    else -> stringResource(R.string.tracker_streak_days, streak)
}

@Composable
private fun motivationLine(state: TrackerUiState): String = when (state.todayPrayedCount) {
    0 -> stringResource(R.string.tracker_motivation_start)
    5 -> stringResource(R.string.tracker_motivation_complete)
    4 -> stringResource(R.string.tracker_motivation_almost)
    else -> stringResource(R.string.tracker_motivation_progress)
}

@Composable
private fun pluralOrSummary(completedDays: Int): String =
    stringResource(R.string.tracker_heatmap_summary, completedDays)
