package com.zakrni.app.clean.ui.compose.qibla

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.zakrni.app.R
import com.zakrni.app.clean.ui.theme.components.EmptyState
import com.zakrni.app.clean.ui.theme.components.LoadingState
import com.zakrni.app.clean.ui.theme.components.ZButton
import com.zakrni.app.clean.ui.theme.components.ZButtonStyle
import com.zakrni.app.clean.ui.theme.components.ZCard
import com.zakrni.app.clean.ui.theme.components.ZTopBar

/** Route constant for the Qibla destination. */
const val QIBLA_ROUTE = "qibla"

/**
 * Adds the Qibla compass destination to a [NavGraphBuilder].
 *
 * Navigate to it with `navController.navigate(QIBLA_ROUTE)`.
 */
fun NavGraphBuilder.qiblaGraph(navController: NavHostController) {
    composable(QIBLA_ROUTE) {
        QiblaScreen(onBack = { navController.popBackStack() })
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QiblaScreen(
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: QiblaViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
        ),
    )
    val granted = permissionState.permissions.any { it.status.isGranted }

    // Register/unregister sensors with the screen lifecycle.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.startSensors()
                Lifecycle.Event.ON_PAUSE -> viewModel.stopSensors()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Resolve location once permission is granted.
    LaunchedEffect(granted) {
        if (granted) viewModel.resolveLocation()
    }

    Column(modifier = modifier.fillMaxSize()) {
        ZTopBar(
            title = stringResource(R.string.qibla_title),
            onBack = onBack,
        )

        when {
            !granted -> PermissionRationale(
                shouldShowRationale = permissionState.shouldShowRationale,
                onRequest = { permissionState.launchMultiplePermissionRequest() },
            )

            uiState.isLoading && uiState.qiblaBearing == null -> LoadingState()

            !uiState.hasLocation && uiState.qiblaBearing == null -> LocationUnavailable(
                onRetry = { viewModel.resolveLocation() },
            )

            else -> QiblaContent(uiState = uiState)
        }
    }
}

@Composable
private fun QiblaContent(uiState: QiblaUiState) {
    val context = LocalContext.current
    val haptics = remember { Haptics(context) }

    // One-shot haptic when we transition into alignment.
    LaunchedEffect(uiState.isAligned) {
        if (uiState.isAligned) haptics.tick()
    }

    val cardinals = CardinalLabels(
        north = stringResource(R.string.qibla_cardinal_n),
        east = stringResource(R.string.qibla_cardinal_e),
        south = stringResource(R.string.qibla_cardinal_s),
        west = stringResource(R.string.qibla_cardinal_w),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))

        val statusText = if (uiState.isAligned) {
            stringResource(R.string.qibla_aligned)
        } else {
            stringResource(R.string.qibla_rotate_hint)
        }
        Text(
            text = statusText,
            style = MaterialTheme.typography.titleMedium,
            color = if (uiState.isAligned) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        QiblaCompass(
            azimuth = uiState.deviceAzimuth,
            qiblaPointerAngle = uiState.qiblaPointerAngle,
            aligned = uiState.isAligned,
            cardinalLabels = cardinals,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InfoCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.qibla_bearing_label),
                value = uiState.qiblaBearing?.let {
                    stringResource(R.string.qibla_degrees_format, it.toInt())
                } ?: "—",
            )
            InfoCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.qibla_distance_label),
                value = uiState.distanceKm?.let {
                    stringResource(R.string.qibla_km_format, it.toInt())
                } ?: "—",
            )
        }

        if (uiState.accuracy == CompassAccuracy.LOW) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.qibla_low_accuracy),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // TODO(AR): Augmented-reality camera mode (live camera overlay with the
        //  Qibla direction) is intentionally not implemented yet. See report.
    }
}

@Composable
private fun InfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    ZCard(modifier = modifier, contentPadding = 16.dp) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionRationale(
    shouldShowRationale: Boolean,
    onRequest: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            EmptyState(
                icon = Icons.Filled.Explore,
                title = stringResource(R.string.qibla_permission_title),
                message = stringResource(R.string.qibla_permission_message),
            )
        }
        ZButton(
            text = stringResource(R.string.qibla_grant_permission),
            onClick = onRequest,
            style = ZButtonStyle.Gold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp),
        )
    }
}

@Composable
private fun LocationUnavailable(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            EmptyState(
                icon = Icons.Filled.LocationOff,
                title = stringResource(R.string.qibla_location_unavailable_title),
                message = stringResource(R.string.qibla_location_unavailable_message),
            )
        }
        ZButton(
            text = stringResource(R.string.qibla_retry),
            onClick = onRetry,
            style = ZButtonStyle.Gold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp),
        )
    }
}
