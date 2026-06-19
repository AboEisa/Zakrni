package com.zakrni.app.clean.ui.compose.qibla

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Animated Qibla compass drawn with Compose [Canvas].
 *
 * - The outer dial (cardinal marks + degree ticks) rotates against the device
 *   heading so North on the dial always points to true north.
 * - A bold Qibla pointer indicates the direction to the Kaaba.
 * - When [aligned] the pointer turns gold/green to signal a match.
 *
 * Colours come from [MaterialTheme]/brand tokens, so it adapts to light & dark.
 */
@Composable
fun QiblaCompass(
    azimuth: Float,
    qiblaPointerAngle: Float,
    aligned: Boolean,
    modifier: Modifier = Modifier,
    cardinalLabels: CardinalLabels = CardinalLabels(),
) {
    // Smooth rotations on top of the already-filtered azimuth.
    val dialRotation by animateFloatAsState(
        targetValue = -azimuth,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 120f),
        label = "dial-rotation",
    )
    val pointerRotation by animateFloatAsState(
        targetValue = qiblaPointerAngle,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 120f),
        label = "pointer-rotation",
    )

    val dialColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tickColor = MaterialTheme.colorScheme.outline
    val ringColor = MaterialTheme.colorScheme.surfaceVariant
    val cardinalColor = MaterialTheme.colorScheme.onSurface
    val northColor = MaterialTheme.colorScheme.error

    val pointerColor by animateColorAsState(
        targetValue = if (aligned) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.secondary
        },
        label = "pointer-color",
    )
    val haloColor by animateColorAsState(
        targetValue = if (aligned) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        } else {
            Color.Transparent
        },
        label = "halo-color",
    )

    val density = LocalDensity.current
    val labelPx = remember(density) { with(density) { 18.sp.toPx() } }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp),
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = min(size.width, size.height) / 2f * 0.92f

        // Alignment halo behind everything.
        drawCircle(color = haloColor, radius = radius * 1.02f, center = center)

        // Outer ring.
        drawCircle(
            color = ringColor,
            radius = radius,
            center = center,
            style = Stroke(width = radius * 0.06f),
        )

        // Rotating dial: ticks + cardinal marks.
        rotate(degrees = dialRotation, pivot = center) {
            drawTicks(center, radius, tickColor, dialColor)
            drawCardinals(center, radius, labelPx, cardinalColor, northColor, cardinalLabels)
        }

        // Qibla pointer rotates relative to screen-up (independent of dial).
        rotate(degrees = pointerRotation, pivot = center) {
            drawQiblaPointer(center, radius, pointerColor)
        }

        // Center hub.
        drawCircle(color = pointerColor, radius = radius * 0.05f, center = center)

        // Fixed top index marker (where you aim the device).
        drawIndexMarker(center, radius, northColor)
    }
}

/** Degree ticks around the dial; longer ticks every 30°. */
private fun DrawScope.drawTicks(center: Offset, radius: Float, tick: Color, major: Color) {
    for (deg in 0 until 360 step 6) {
        val isMajor = deg % 30 == 0
        val rad = Math.toRadians((deg - 90).toDouble())
        val outer = radius * 0.93f
        val inner = if (isMajor) radius * 0.80f else radius * 0.87f
        val start = Offset(
            center.x + (cos(rad) * outer).toFloat(),
            center.y + (sin(rad) * outer).toFloat(),
        )
        val end = Offset(
            center.x + (cos(rad) * inner).toFloat(),
            center.y + (sin(rad) * inner).toFloat(),
        )
        drawLine(
            color = if (isMajor) major else tick,
            start = start,
            end = end,
            strokeWidth = if (isMajor) radius * 0.012f else radius * 0.006f,
        )
    }
}

/** N / E / S / W labels; North gets the accent colour. */
private fun DrawScope.drawCardinals(
    center: Offset,
    radius: Float,
    textSize: Float,
    color: Color,
    northColor: Color,
    labels: CardinalLabels,
) {
    val marks = listOf(
        Triple(0, labels.north, northColor),
        Triple(90, labels.east, color),
        Triple(180, labels.south, color),
        Triple(270, labels.west, color),
    )
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            this.textSize = textSize
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
        }
        for ((deg, label, c) in marks) {
            paint.color = android.graphics.Color.argb(
                (c.alpha * 255).toInt(),
                (c.red * 255).toInt(),
                (c.green * 255).toInt(),
                (c.blue * 255).toInt(),
            )
            val rad = Math.toRadians((deg - 90).toDouble())
            val r = radius * 0.66f
            val x = center.x + (cos(rad) * r).toFloat()
            // Baseline offset so the glyph is vertically centred.
            val y = center.y + (sin(rad) * r).toFloat() + textSize / 3f
            drawText(label, x, y, paint)
        }
    }
}

/** The Qibla needle: a kite/arrow pointing toward the Kaaba. */
private fun DrawScope.drawQiblaPointer(center: Offset, radius: Float, color: Color) {
    val tip = Offset(center.x, center.y - radius * 0.74f)
    val left = Offset(center.x - radius * 0.10f, center.y)
    val right = Offset(center.x + radius * 0.10f, center.y)
    val tail = Offset(center.x, center.y + radius * 0.18f)

    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(right.x, right.y)
        lineTo(tail.x, tail.y)
        lineTo(left.x, left.y)
        close()
    }
    drawPath(path = path, color = color)
}

/** Fixed marker at the top of the screen — the direction you physically face. */
private fun DrawScope.drawIndexMarker(center: Offset, radius: Float, color: Color) {
    val tipY = center.y - radius * 1.0f
    val baseY = center.y - radius * 0.88f
    val half = radius * 0.04f
    val path = Path().apply {
        moveTo(center.x, tipY)
        lineTo(center.x - half, baseY)
        lineTo(center.x + half, baseY)
        close()
    }
    drawPath(path = path, color = color)
}

/** Localised single-letter cardinal labels (defaults to English). */
data class CardinalLabels(
    val north: String = "N",
    val east: String = "E",
    val south: String = "S",
    val west: String = "W",
)
