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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            NextPrayerHero(
                nextPrayer = nextPrayer,
                remainingTime = remainingTime,
                locationName = locationName,
                isArabic = isArabic,
            )
        }
        items(rows, key = { it.key }) { row ->
            PrayerTimelineRow(
                row = row,
                isCurrent = row.key == currentPrayer?.name,
                isNext = row.key == nextPrayer?.name,
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
    locationName: String,
    isArabic: Boolean,
) {
    ZCard(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 24.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    modifier = Modifier.size(15.dp),
                )
                Spacer(Modifier.size(5.dp))
                Text(
                    text = locationName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = stringResource(R.string.prn_next_prayer),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            )
            val displayName = nextPrayer?.let { if (isArabic) it.nameArabic else it.name }
            Text(
                text = displayName ?: "—",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(top = 2.dp),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = remainingTime.ifBlank { "--:--" },
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            if (nextPrayer != null) {
                Text(
                    text = "${stringResource(R.string.prn_remaining)} • ${nextPrayer.time}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun PrayerTimelineRow(row: PrayerRowUi, isCurrent: Boolean, isNext: Boolean) {
    val active = isCurrent || isNext
    val accent = MaterialTheme.colorScheme.primary
    val containerColor = if (active) accent.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface

    ZCard(modifier = Modifier.fillMaxWidth(), color = containerColor, contentPadding = 14.dp) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (active) accent.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Image(painter = painterResource(row.icon), contentDescription = null, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(row.nameRes),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    color = if (active) accent else MaterialTheme.colorScheme.onSurface,
                )
                if (active) {
                    Text(
                        text = stringResource(if (isCurrent) R.string.prn_now_label else R.string.prn_next_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                    )
                }
            }
            Text(
                text = row.time,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (active) accent else MaterialTheme.colorScheme.onSurface,
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
