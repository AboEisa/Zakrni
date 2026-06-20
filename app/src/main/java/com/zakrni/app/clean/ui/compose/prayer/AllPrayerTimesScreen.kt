package com.zakrni.app.clean.ui.compose.prayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zakrni.app.R
import com.zakrni.app.clean.ui.theme.BrandGold
import com.zakrni.app.clean.ui.theme.components.LoadingState
import com.zakrni.app.clean.ui.theme.components.ZCard
import com.zakrni.app.clean.ui.theme.components.ZTopBar
import com.zakrni.app.clean.ui.utils.PrayerTimeUtils
import com.zakrni.app.clean.ui.viewmodels.PrayerTimesViewModel

/**
 * Full detailed list of the day's prayer times (including Sunrise), reached from
 * [PrayerTimesScreen]. Shows the Hijri + Gregorian dates, the location, and highlights the
 * current prayer. Backed by the same [PrayerTimesViewModel] instance via [hiltViewModel].
 */
@Composable
fun AllPrayerTimesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PrayerTimesViewModel = hiltViewModel(),
) {
    val prayerTimes by viewModel.prayerTimes.collectAsStateWithLifecycle()
    val currentPrayer by viewModel.currentPrayer.collectAsStateWithLifecycle()
    val nextPrayer by viewModel.nextPrayer.collectAsStateWithLifecycle()
    val locationName by viewModel.locationName.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        ZTopBar(
            title = stringResource(R.string.prn_all_title),
            onBack = onBack,
        )

        val response = prayerTimes
        if (response == null) {
            LoadingState()
        } else {
            val hijri = PrayerTimeUtils.formatHijriDateFromApi(response)
            val gregorian = PrayerTimeUtils.formatGregorianDateFromApi(response)
            val rows = response.data.timings.toPrayerRows()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    DateHeader(
                        hijri = hijri,
                        gregorian = gregorian,
                        locationName = locationName,
                    )
                }
                items(rows, key = { it.key }) { row ->
                    val isCurrent = row.isPrayer && row.key == currentPrayer?.name
                    val isNext = row.isPrayer && row.key == nextPrayer?.name
                    AllPrayerRow(row = row, isCurrent = isCurrent, isNext = isNext)
                }
            }
        }
    }
}

@Composable
private fun DateHeader(hijri: String, gregorian: String, locationName: String) {
    ZCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary,
        contentPadding = 20.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = hijri,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = gregorian,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(Modifier.size(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = stringResource(R.string.prn_cd_location),
                    tint = BrandGold,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = locationName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.85f),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun AllPrayerRow(row: PrayerRowUi, isCurrent: Boolean, isNext: Boolean) {
    ZCard(
        modifier = Modifier.fillMaxWidth(),
        color = if (isCurrent) BrandGold.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        contentPadding = 14.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (isCurrent) BrandGold.copy(alpha = 0.22f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(row.icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(row.nameRes),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isCurrent || isNext) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrent) BrandGold else MaterialTheme.colorScheme.onSurface,
                )
                if (isCurrent || isNext) {
                    Text(
                        text = stringResource(
                            if (isCurrent) R.string.prn_now_label else R.string.prn_next_label,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCurrent) BrandGold else MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                text = row.time,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isCurrent) BrandGold
                else if (row.isPrayer) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
