package com.zakrni.app.clean.ui.compose.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zakrni.app.R
import com.zakrni.app.clean.ui.theme.AyahTextStyle
import com.zakrni.app.clean.ui.theme.BrandGold
import com.zakrni.app.clean.ui.theme.components.ZCard
import com.zakrni.app.clean.ui.theme.components.ZSectionHeader
import com.zakrni.app.clean.ui.utils.PrayerTimeUtils
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

/** Stable route constant for the Home landing destination. */
object HomeRoutes {
    const val HOME = "home"
}

private data class QuickAction(
    val labelRes: Int,
    val icon: ImageVector,
    val route: String,
)

private val quickActions = listOf(
    QuickAction(R.string.home_action_quran, Icons.AutoMirrored.Filled.MenuBook, "quran_list"),
    QuickAction(R.string.home_action_azkar, Icons.Filled.AutoAwesome, "azkar"),
    QuickAction(R.string.home_action_dua, Icons.Filled.Favorite, "dua"),
    QuickAction(R.string.home_action_hadith, Icons.Filled.FormatQuote, "hadith"),
    QuickAction(R.string.home_action_allah_names, Icons.Filled.WbSunny, "allah_names"),
    QuickAction(R.string.home_action_tasbih, Icons.Filled.Fingerprint, "tasbih"),
    QuickAction(R.string.home_action_qibla, Icons.Filled.Explore, "qibla"),
    QuickAction(R.string.home_action_hijri, Icons.Filled.CalendarMonth, "hijri"),
    QuickAction(R.string.home_action_tracker, Icons.Filled.CheckCircle, "tracker"),
    QuickAction(R.string.home_action_prayer, Icons.Filled.Mosque, "prayer"),
    QuickAction(R.string.home_action_media, Icons.Filled.PlayCircle, "media"),
)

/**
 * The polished Home landing.
 *
 * Single scrolling [LazyVerticalGrid] (2-column) hosting:
 *  - a greeting header,
 *  - the next-prayer card with a live ticking countdown,
 *  - a swipeable daily-ayah carousel (parallax + page dots),
 *  - and the quick-actions grid that opens app sections via [onOpen].
 */
@Composable
fun HomeScreen(
    onOpen: (route: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        fullSpan { GreetingHeader() }
        fullSpan { NextPrayerCard(onOpenPrayer = { onOpen("prayer") }) }
        fullSpan { DailyAyahCarousel() }
        fullSpan {
            ZSectionHeader(
                title = stringResource(R.string.home_quick_actions),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        itemsIndexed(quickActions) { index, action ->
            QuickActionTile(action = action, index = index, onClick = { onOpen(action.route) })
        }
    }
}

/** Helper for a full-width row inside the 2-column grid. */
private fun androidx.compose.foundation.lazy.grid.LazyGridScope.fullSpan(
    content: @Composable () -> Unit,
) {
    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) { content() }
}

@Composable
private fun GreetingHeader() {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
        Text(
            text = stringResource(R.string.home_greeting),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.home_app_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NextPrayerCard(onOpenPrayer: () -> Unit) {
    val context = LocalContext.current
    // Recomputed every second to drive the live countdown; null until times are cached.
    var nextPrayer by remember { mutableStateOf<PrayerTimeUtils.PrayerInfo?>(readNextPrayer(context)) }
    LaunchedEffect(Unit) {
        while (true) {
            nextPrayer = readNextPrayer(context)
            delay(1000L)
        }
    }

    ZCard(
        onClick = onOpenPrayer,
        color = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary,
        contentPadding = 20.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        val prayer = nextPrayer
        if (prayer == null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(BrandGold.copy(alpha = 0.20f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Mosque, contentDescription = null, tint = BrandGold, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.size(14.dp))
                Column {
                    Text(
                        text = stringResource(R.string.home_prayer_unavailable),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onTertiary,
                    )
                    Text(
                        text = stringResource(R.string.home_prayer_open),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.8f),
                    )
                }
            }
            return@ZCard
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(BrandGold.copy(alpha = 0.20f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.AccessTime, contentDescription = null, tint = BrandGold, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_next_prayer),
                    style = MaterialTheme.typography.labelMedium,
                    color = BrandGold,
                )
                Text(
                    text = prayer.nameArabic,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onTertiary,
                )
                Text(
                    text = prayer.time,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.85f),
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                // AnimatedContent makes the ticking number feel alive without a layout jump.
                AnimatedContent(
                    targetState = prayer.timeRemaining,
                    transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(180)) },
                    label = "countdown",
                ) { remaining ->
                    Text(
                        text = remaining,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onTertiary,
                    )
                }
                Text(
                    text = stringResource(R.string.home_remaining),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun DailyAyahCarousel() {
    val ayahs = remember { dailyAyahCarousel }
    if (ayahs.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = dailyAyahStartIndex(),
        pageCount = { ayahs.size },
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        ZSectionHeader(title = stringResource(R.string.home_daily_ayah))
        Spacer(Modifier.size(8.dp))
        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            contentPadding = PaddingValues(horizontal = 4.dp),
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            // Parallax: pages drift and fade slightly as they leave the viewport.
            val offset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            AyahCard(ayah = ayahs[page], pageOffset = offset)
        }
        Spacer(Modifier.size(12.dp))
        PageDots(count = ayahs.size, selected = pagerState.currentPage)
    }
}

@Composable
private fun AyahCard(ayah: DailyAyah, pageOffset: Float) {
    val fraction = pageOffset.absoluteValue.coerceIn(0f, 1f)
    ZCard(
        contentPadding = 22.dp,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                // Subtle parallax + scale + fade for off-center pages.
                translationX = pageOffset * size.width * 0.10f
                val scale = 1f - 0.06f * fraction
                scaleX = scale
                scaleY = scale
                alpha = 1f - 0.35f * fraction
            },
    ) {
        Icon(
            imageVector = Icons.Filled.FormatQuote,
            contentDescription = null,
            tint = BrandGold.copy(alpha = 0.6f),
            modifier = Modifier.size(28.dp),
        )
        Spacer(Modifier.size(10.dp))
        Text(
            text = ayah.arabic,
            style = AyahTextStyle,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = ayah.translation,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.size(10.dp))
        Text(
            text = "${ayah.surahName} • ${ayah.ayahNumber}",
            style = MaterialTheme.typography.labelMedium,
            color = BrandGold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PageDots(count: Int, selected: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(count) { index ->
            val isSelected = index == selected
            val width by animateDpAsState(if (isSelected) 22.dp else 8.dp, tween(250), label = "dot-width")
            val alpha by animateFloatAsState(if (isSelected) 1f else 0.35f, tween(250), label = "dot-alpha")
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .height(8.dp)
                    .width(width)
                    .graphicsLayer { this.alpha = alpha }
                    .background(BrandGold, CircleShape),
            )
        }
    }
}

@Composable
private fun QuickActionTile(action: QuickAction, index: Int, onClick: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 45L)
        visible = true
    }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(320), label = "qa-alpha")
    val translate by animateFloatAsState(if (visible) 0f else 30f, tween(320), label = "qa-translate")

    ZCard(
        onClick = onClick,
        contentPadding = 16.dp,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.alpha = alpha
                translationY = translate
            },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(BrandGold.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
            }
            Text(
                text = stringResource(action.labelRes),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}
