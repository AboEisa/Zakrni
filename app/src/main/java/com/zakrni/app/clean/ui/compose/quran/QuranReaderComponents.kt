package com.zakrni.app.clean.ui.compose.quran

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zakrni.app.R

/**
 * Bottom audio player bar wired to the existing AudioPlayerManager (via the ViewModel).
 *
 * Shows a play/pause (or loading) button, a seek slider with current / total time, and a button to
 * open the reciter picker. While the user drags the slider the displayed position follows the drag
 * and only commits on release, so playback progress updates don't fight the gesture.
 */
@Composable
internal fun AudioPlayerBar(
    isPlaying: Boolean,
    isLoading: Boolean,
    progressMs: Int,
    durationMs: Int,
    reciterName: String,
    isArabic: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Int) -> Unit,
    onPickReciter: () -> Unit,
    modifier: Modifier = Modifier,
    surahProgress: Float = -1f,
    progressLabel: String = "",
    onSeekToAyah: (Float) -> Unit = {},
) {
    var dragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableFloatStateOf(0f) }

    // Follow mode drives one continuous bar across the whole surah (0f..1f); otherwise the
    // bar tracks the current clip's milliseconds as before.
    val followMode = surahProgress >= 0f
    val duration = durationMs.coerceAtLeast(0)
    val sliderMax = if (followMode) 1f else duration.toFloat().coerceAtLeast(1f)
    val displayedMs = if (dragging) dragValue.toInt() else progressMs.coerceIn(0, duration)
    val sliderValue = when {
        dragging -> dragValue
        followMode -> surahProgress
        else -> progressMs.toFloat().coerceIn(0f, sliderMax)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 10.dp,
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlayPauseButton(
                    isPlaying = isPlaying,
                    isLoading = isLoading,
                    onClick = onPlayPause,
                )

                Spacer(Modifier.width(12.dp))

                ReciterChip(
                    reciterName = reciterName,
                    onClick = onPickReciter,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(4.dp))

            Slider(
                value = sliderValue,
                onValueChange = {
                    dragging = true
                    dragValue = it
                },
                onValueChangeFinished = {
                    if (followMode) onSeekToAyah(dragValue.coerceIn(0f, 1f)) else onSeek(dragValue.toInt())
                    dragging = false
                },
                valueRange = 0f..sliderMax,
                enabled = if (followMode) true else duration > 0,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
                ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (followMode) progressLabel else formatAudioTime(displayedMs, isArabic),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (followMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (followMode) formatAudioTime(progressMs.coerceAtLeast(0), isArabic)
                    else formatAudioTime(duration, isArabic),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PlayPauseButton(
    isPlaying: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(if (isPlaying) 1f else 0.96f, label = "play-scale")
    Box(
        modifier = Modifier
            .scale(scale)
            .size(54.dp)
            .shadow(6.dp, CircleShape)
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .clickableNoLoading(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.5.dp,
            )
        } else {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = stringResource(if (isPlaying) R.string.qrn_pause else R.string.qrn_play),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun ReciterChip(
    reciterName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.qrn_reciter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = reciterName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Reciter picker modal sheet. [reciters] is the list of (identifier, displayName) pairs from the
 * ViewModel; tapping one selects it and dismisses.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReciterPickerSheet(
    reciters: List<Pair<String, String>>,
    selectedId: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Text(
            text = stringResource(R.string.qrn_reciter_picker_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        ) {
            items(reciters, key = { it.first }) { (id, name) ->
                val selected = id == selectedId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickableNoLoading(enabled = true) { onSelect(id) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = if (selected) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.weight(1f),
                    )
                    if (selected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = stringResource(R.string.qrn_reciter_selected),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }
    }
}

/** Tafsir modal sheet showing the surah's tafsir text (or an unavailable message). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TafsirSheet(
    title: String,
    tafsirText: String?,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightCapped()
                .padding(horizontal = 20.dp),
        ) {
            item {
                Text(
                    text = tafsirText ?: stringResource(R.string.qrn_tafsir_unavailable),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (tafsirText != null) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 28.dp),
                )
            }
        }
    }
}

/** Caps the tafsir scroll body to a comfortable max height inside the sheet. */
private fun Modifier.heightCapped(): Modifier = this.height(420.dp)

/** Clickable that no-ops while disabled, with no ripple bloat — small local helper. */
private fun Modifier.clickableNoLoading(
    enabled: Boolean,
    onClick: () -> Unit,
): Modifier = this.then(
    Modifier.clickable(enabled = enabled, onClick = onClick),
)
