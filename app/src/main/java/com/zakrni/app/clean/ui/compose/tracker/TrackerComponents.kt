package com.zakrni.app.clean.ui.compose.tracker

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Animated circular progress ring with the prayed/total count in its center.
 * Pure Compose [Canvas]; the sweep animates whenever [progress] changes.
 */
@Composable
fun ProgressRing(
    progress: Float,
    centerLabel: String,
    centerSubLabel: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700),
        label = "ring-progress",
    )
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val ringStart = MaterialTheme.colorScheme.primary
    val ringEnd = MaterialTheme.colorScheme.secondary

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(
            modifier = Modifier
                .size(140.dp)
                .semanticsDesc(contentDescription),
        ) {
            val stroke = 14.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)
            // Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            // Progress (blended brand color for a subtle gradient feel)
            if (animated > 0f) {
                drawArc(
                    color = lerp(ringStart, ringEnd, animated),
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerLabel,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = centerSubLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * A single toggleable prayer row: name + animated check pill.
 * The whole row is clickable for a large, RTL-friendly touch target.
 */
@Composable
fun PrayerCheckItem(
    label: String,
    checked: Boolean,
    stateDescription: String,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val checkColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(250),
        label = "check-color",
    )
    val scale by animateFloatAsState(
        targetValue = if (checked) 1f else 0.7f,
        animationSpec = tween(250),
        label = "check-scale",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onToggle)
            .semanticsDesc("$label, $stateDescription")
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(checkColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(18.dp)
                    .scaleIcon(scale),
            )
        }
    }
}

/**
 * GitHub-style completion heatmap drawn on a [Canvas]. Each cell is shaded by the fraction of
 * prayers prayed that day. Laid out as weeks (columns) × 7 weekdays (rows). RTL is handled by
 * reading right-to-left so the most recent day stays in the natural "latest" corner.
 */
@Composable
fun CompletionHeatmap(
    days: List<DayCompletion>,
    isRtl: Boolean,
    modifier: Modifier = Modifier,
) {
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant
    val fullColor = MaterialTheme.colorScheme.primary
    val weeks = if (days.isEmpty()) 1 else ((days.size + 6) / 7)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(weeks / 7f),
    ) {
        if (days.isEmpty()) return@Canvas
        val cols = weeks
        val rows = 7
        val gap = 4.dp.toPx()
        val cellW = (size.width - gap * (cols - 1)) / cols
        val cellH = (size.height - gap * (rows - 1)) / rows
        val cell = minOf(cellW, cellH)
        val radius = cell * 0.22f

        days.forEachIndexed { index, day ->
            val col = index / 7
            val row = index % 7
            // In RTL, fill columns from the right edge so time still flows naturally.
            val drawCol = if (isRtl) (cols - 1 - col) else col
            val x = drawCol * (cell + gap)
            val y = row * (cell + gap)
            val color = if (day.prayedCount == 0) {
                emptyColor
            } else {
                lerp(emptyColor, fullColor, 0.25f + 0.75f * day.fraction)
            }
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(cell, cell),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
            )
        }
    }
}

/** Small color-scale legend (less → more) for the heatmap. */
@Composable
fun HeatmapLegend(lessLabel: String, moreLabel: String, modifier: Modifier = Modifier) {
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant
    val fullColor = MaterialTheme.colorScheme.primary
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(lessLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { f ->
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (f == 0f) emptyColor else lerp(emptyColor, fullColor, 0.25f + 0.75f * f)),
            )
        }
        Text(moreLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// --- tiny modifier helpers kept local so this file is self-contained ---

private fun Modifier.scaleIcon(scale: Float): Modifier =
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }

private fun Modifier.semanticsDesc(description: String): Modifier =
    this.semantics {
        // `contentDescription` here is the real Compose SemanticsPropertyReceiver property.
        contentDescription = description
    }

// Spacer alias to keep imports tidy where used.
@Composable
fun VSpace(height: androidx.compose.ui.unit.Dp) {
    Spacer(modifier = Modifier.size(height))
}
