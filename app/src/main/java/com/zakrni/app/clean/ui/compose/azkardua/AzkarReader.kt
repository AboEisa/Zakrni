package com.zakrni.app.clean.ui.compose.azkardua

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.zakrni.app.R
import com.zakrni.app.clean.ui.theme.AyahTextStyle
import com.zakrni.app.clean.ui.theme.components.ZTopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/** A single dhikr in the reader: its text and how many times it should be recited. */
data class DhikrSlide(val text: String, val count: Int)

/**
 * Full-screen dhikr reader: one dhikr per swipeable slide with a scale/fade page transition, a
 * tap-to-count circular counter that pulses and auto-advances, prev/next pills, and a top progress
 * bar. Progress is saved per section and resets automatically each new day.
 */
@Composable
fun AzkarReaderView(
    title: String,
    slides: List<DhikrSlide>,
    arabic: Boolean,
    sectionKey: String,
    onBack: () -> Unit,
) {
    if (slides.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize()) { ZTopBar(title = title, onBack = onBack) }
        return
    }
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()
    val readCounts = remember(slides, sectionKey) {
        mutableStateListOf(*loadProgress(context, sectionKey, slides.size).toTypedArray())
    }
    val completed = readCounts.indices.count { readCounts[it] >= slides[it].count }

    Column(modifier = Modifier.fillMaxSize()) {
        ZTopBar(title = title, onBack = onBack)

        Text(
            text = "${localizeNum(pagerState.currentPage + 1, arabic)} / ${localizeNum(slides.size, arabic)}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp),
        )
        LinearProgressIndicator(
            progress = { completed.toFloat() / slides.size },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { page ->
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
            DhikrSlideView(
                slide = slides[page],
                read = readCounts[page],
                arabic = arabic,
                modifier = Modifier.graphicsLayer {
                    val f = 1f - pageOffset.coerceIn(0f, 1f)
                    val s = lerp(0.9f, 1f, f)
                    scaleX = s
                    scaleY = s
                    alpha = lerp(0.45f, 1f, f)
                },
                onTap = {
                    if (readCounts[page] < slides[page].count) {
                        readCounts[page] = readCounts[page] + 1
                        saveProgress(context, sectionKey, readCounts.toList())
                        if (readCounts[page] >= slides[page].count && page < slides.lastIndex) {
                            scope.launch {
                                delay(350)
                                pagerState.animateScrollToPage(page + 1)
                            }
                        }
                    }
                },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavPill(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                label = stringResource(R.string.azr_prev),
                enabled = pagerState.currentPage > 0,
                modifier = Modifier.weight(1f),
            ) { scope.launch { pagerState.animateScrollToPage((pagerState.currentPage - 1).coerceAtLeast(0)) } }
            NavPill(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                label = stringResource(R.string.azr_next),
                enabled = pagerState.currentPage < slides.lastIndex,
                primary = true,
                modifier = Modifier.weight(1f),
            ) { scope.launch { pagerState.animateScrollToPage((pagerState.currentPage + 1).coerceAtMost(slides.lastIndex)) } }
        }
    }
}

@Composable
private fun DhikrSlideView(
    slide: DhikrSlide,
    read: Int,
    arabic: Boolean,
    modifier: Modifier = Modifier,
    onTap: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onTap),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) {
                Text(
                    text = slide.text,
                    style = AyahTextStyle.copy(fontSize = 24.sp, lineHeight = 44.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                )
            }
            CounterRing(read = read, target = slide.count, arabic = arabic)
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.azr_tap_hint),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CounterRing(read: Int, target: Int, arabic: Boolean) {
    val done = read >= target
    val scale = remember { Animatable(1f) }
    LaunchedEffect(read) {
        if (read > 0) {
            scale.snapTo(if (read >= target) 1.35f else 1.22f)
            scale.animateTo(1f, animationSpec = tween(durationMillis = 300))
        }
    }
    Box(
        modifier = Modifier
            .size(108.dp)
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .background(
                color = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (done) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(46.dp))
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = localizeNum(read, arabic),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "${stringResource(R.string.azr_of)} ${localizeNum(target, arabic)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun NavPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    onClick: () -> Unit,
) {
    val container = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        primary -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val content = if (primary && enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Surface(
        onClick = { if (enabled) onClick() },
        shape = RoundedCornerShape(16.dp),
        color = container,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
            Spacer(Modifier.size(6.dp))
            Text(label, style = MaterialTheme.typography.titleSmall, color = content)
        }
    }
}

// ----- Daily-resetting progress persistence -----

private const val PROGRESS_PREFS = "zakrni_azkar_progress"

private fun todayKey(): String =
    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())

private fun loadProgress(context: Context, sectionKey: String, size: Int): IntArray {
    val raw = context.getSharedPreferences(PROGRESS_PREFS, Context.MODE_PRIVATE)
        .getString("p_$sectionKey", "").orEmpty()
    val parts = raw.split(";", limit = 2)
    if (parts.size == 2 && parts[0] == todayKey()) {
        val counts = parts[1].split(",").mapNotNull { it.toIntOrNull() }
        return IntArray(size) { counts.getOrElse(it) { 0 } }
    }
    return IntArray(size)
}

private fun saveProgress(context: Context, sectionKey: String, counts: List<Int>) {
    context.getSharedPreferences(PROGRESS_PREFS, Context.MODE_PRIVATE)
        .edit().putString("p_$sectionKey", "${todayKey()};${counts.joinToString(",")}").apply()
}

private fun localizeNum(value: Int, arabic: Boolean): String {
    val s = value.toString()
    if (!arabic) return s
    return s.map { c -> if (c in '0'..'9') ('٠' + (c - '0')) else c }.joinToString("")
}
