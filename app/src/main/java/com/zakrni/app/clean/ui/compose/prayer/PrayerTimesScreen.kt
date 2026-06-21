package com.zakrni.app.clean.ui.compose.prayer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import com.zakrni.app.R
import com.zakrni.app.clean.ui.theme.BrandGold
import com.zakrni.app.clean.ui.theme.components.ErrorState
import com.zakrni.app.clean.ui.theme.components.LoadingState
import com.zakrni.app.clean.ui.theme.components.ZButton
import com.zakrni.app.clean.ui.theme.components.ZButtonStyle
import com.zakrni.app.clean.ui.theme.components.ZCard
import com.zakrni.app.clean.ui.theme.components.ZTopBar
import com.zakrni.app.clean.ui.utils.PrayerTimeUtils
import com.zakrni.app.clean.ui.viewmodels.PrayerTimesViewModel

/**
 * Main bottom-nav tab: the next prayer with a live countdown ring, then the five daily prayers
 * as a vertical timeline with the current one highlighted. Handles the location-permission gate
 * plus loading / error states.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PrayerTimesScreen(
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PrayerTimesViewModel = hiltViewModel(),
) {
    val permissionState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
        ),
    )

    val prayerTimes by viewModel.prayerTimes.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val nextPrayer by viewModel.nextPrayer.collectAsStateWithLifecycle()
    val currentPrayer by viewModel.currentPrayer.collectAsStateWithLifecycle()
    val remainingTime by viewModel.remainingTime.collectAsStateWithLifecycle()
    val locationName by viewModel.locationName.collectAsStateWithLifecycle()

    // When the permission flips to granted, let the ViewModel re-check and load.
    val granted = permissionState.allPermissionsGranted
    androidx.compose.runtime.LaunchedEffect(granted) {
        if (granted) viewModel.checkLocationPermission()
    }

    // Re-fetch when the user changed the calculation method/madhab in Settings.
    val reloadContext = androidx.compose.ui.platform.LocalContext.current
    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        if (com.zakrni.app.clean.ui.utils.PrayerCalcSettings.consumeDirty(reloadContext)) {
            viewModel.reloadPrayerTimes()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ZTopBar(title = stringResource(R.string.prn_title))

        when {
            !granted -> LocationPermissionPane(
                showRationale = permissionState.shouldShowRationale,
                onRequest = { permissionState.launchMultiplePermissionRequest() },
            )

            error != null && prayerTimes == null -> ErrorState(
                message = error ?: "",
                onRetry = { viewModel.loadPrayerTimes() },
                retryLabel = stringResource(R.string.prn_retry),
            )

            prayerTimes == null && isLoading -> LoadingState()

            prayerTimes == null -> LoadingState()

            else -> PrayerContent(
                rows = prayerTimes!!.data.timings.toPrayerRowsNoSunrise(),
                nextPrayer = nextPrayer,
                currentPrayer = currentPrayer,
                remainingTime = remainingTime,
                locationName = locationName,
                onViewAll = onViewAll,
            )
        }
    }
}

@Composable
private fun PrayerContent(
    rows: List<PrayerRowUi>,
    nextPrayer: PrayerTimeUtils.PrayerInfo?,
    currentPrayer: PrayerTimeUtils.PrayerInfo?,
    remainingTime: String,
    locationName: String,
    onViewAll: () -> Unit,
) {
    val isArabic = stringResource(R.string.prn_fajr) == "الفجر"

    // Ring progress: 1 - remaining/total, where `total` is the full window captured when the
    // next prayer first appears. We track the largest remaining seen for this prayer as the total.
    var totalSeconds by remember { mutableStateOf(0L) }
    var lastKey by remember { mutableStateOf<String?>(null) }
    val remainingSeconds = parseRemaining(remainingTime)
    androidx.compose.runtime.LaunchedEffect(nextPrayer?.name, remainingSeconds) {
        if (nextPrayer?.name != lastKey) {
            lastKey = nextPrayer?.name
            totalSeconds = remainingSeconds.coerceAtLeast(1)
        } else if (remainingSeconds > totalSeconds) {
            totalSeconds = remainingSeconds
        }
    }
    val progress = if (totalSeconds > 0L) {
        1f - (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            LocationRow(locationName = locationName)
        }
        item {
            NextPrayerHero(
                nextPrayer = nextPrayer,
                remainingTime = remainingTime,
                progress = progress,
                isArabic = isArabic,
            )
        }
        item {
            Text(
                text = stringResource(R.string.prn_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
            )
        }
        items(rows, key = { it.key }) { row ->
            val isCurrent = row.key == currentPrayer?.name
            val isNext = row.key == nextPrayer?.name
            PrayerTimelineRow(row = row, isCurrent = isCurrent, isNext = isNext)
        }
        item {
            Spacer(Modifier.height(4.dp))
            ZButton(
                text = stringResource(R.string.prn_view_all),
                onClick = onViewAll,
                style = ZButtonStyle.Gold,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun LocationRow(locationName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = stringResource(R.string.prn_cd_location),
            tint = BrandGold,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.size(6.dp))
        Text(
            text = locationName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun NextPrayerHero(
    nextPrayer: PrayerTimeUtils.PrayerInfo?,
    remainingTime: String,
    progress: Float,
    isArabic: Boolean,
) {
    ZCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 20.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.prn_next_prayer),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val displayName = nextPrayer?.let { if (isArabic) it.nameArabic else it.name }
            if (displayName != null) {
                Text(
                    text = stringResource(R.string.prn_time_for_prayer, displayName),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                )
            }
            CountdownRing(
                progress = progress,
                label = remainingTime.ifBlank { "--:--" },
                ringColor = BrandGold,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            if (nextPrayer != null) {
                Text(
                    text = "${stringResource(R.string.prn_remaining)} • ${nextPrayer.time}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun PrayerTimelineRow(row: PrayerRowUi, isCurrent: Boolean, isNext: Boolean) {
    val highlightColor by animateFloatAsState(
        targetValue = if (isCurrent) 1f else 0f,
        animationSpec = tween(400),
        label = "row-highlight",
    )
    val containerColor = if (isCurrent) {
        BrandGold.copy(alpha = 0.04f + 0.10f * highlightColor)
    } else {
        MaterialTheme.colorScheme.surface
    }

    ZCard(
        modifier = Modifier.fillMaxWidth(),
        color = containerColor,
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
                AnimatedVisibility(
                    visible = isCurrent || isNext,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
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
                color = if (isCurrent) BrandGold else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun LocationPermissionPane(
    showRationale: Boolean,
    onRequest: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(BrandGold.copy(alpha = 0.14f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = BrandGold,
                modifier = Modifier.size(48.dp),
            )
        }
        Text(
            text = stringResource(R.string.prn_location_permission_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp),
        )
        Text(
            text = stringResource(R.string.prn_location_permission_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
        )
        ZButton(
            text = stringResource(
                if (showRationale) R.string.prn_open_settings else R.string.prn_grant_permission,
            ),
            onClick = onRequest,
            style = ZButtonStyle.Gold,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Parses "HH:MM:SS" / "MM:SS" countdown strings back to seconds for ring progress. */
private fun parseRemaining(value: String): Long {
    if (value.isBlank()) return 0L
    val parts = value.split(":").mapNotNull { it.trim().toLongOrNull() }
    return when (parts.size) {
        3 -> parts[0] * 3600 + parts[1] * 60 + parts[2]
        2 -> parts[0] * 60 + parts[1]
        else -> 0L
    }
}
