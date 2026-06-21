package com.zakrni.app.clean.ui.compose.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zakrni.app.R
import com.zakrni.app.clean.ui.theme.AyahTextStyle
import com.zakrni.app.clean.ui.theme.components.ZCard
import com.zakrni.app.clean.ui.utils.PrayerTimeUtils
import kotlinx.coroutines.delay

/** Stable route constant for the Home landing destination. */
object HomeRoutes {
    const val HOME = "home"
}

private data class QuickAction(val labelRes: Int, val icon: ImageVector, val route: String)

// Six most-used shortcuts (the full set lives in the Categories tab).
private val homeQuickActions = listOf(
    QuickAction(R.string.home_action_quran, Icons.AutoMirrored.Filled.MenuBook, "quran_list"),
    QuickAction(R.string.home_action_azkar, Icons.Filled.Spa, "azkar"),
    QuickAction(R.string.home_action_dua, Icons.Filled.VolunteerActivism, "dua"),
    QuickAction(R.string.home_action_hadith, Icons.Filled.FormatQuote, "hadith"),
    QuickAction(R.string.home_action_qibla, Icons.Filled.Explore, "qibla"),
    QuickAction(R.string.home_action_tasbih, Icons.Filled.TouchApp, "tasbih"),
)

/**
 * Fixed (non-scrolling) Home dashboard: a compact greeting, a prominent next-prayer card,
 * the daily ayah, and a 2x3 grid of the key shortcuts — all sized to fit one screen.
 */
@Composable
fun HomeScreen(
    onOpen: (route: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        GreetingHeader(onOpenSettings = { onOpen("settings") })
        NextPrayerCard(onClick = { onOpen("prayer") })
        DailyAyahCard(modifier = Modifier.weight(1f))
        QuickActionsGrid(onOpen = onOpen)
    }
}

@Composable
private fun GreetingHeader(onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    val dateText = remember { android.text.format.DateFormat.getLongDateFormat(context).format(java.util.Date()) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_greeting),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        // Solid circular settings button — quick access from the dashboard.
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onOpenSettings),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.rd_cat_settings),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

@Composable
private fun NextPrayerCard(onClick: () -> Unit) {
    val context = LocalContext.current
    var prayer by remember { mutableStateOf<PrayerTimeUtils.PrayerInfo?>(readNextPrayer(context)) }
    LaunchedEffect(Unit) {
        while (true) {
            prayer = readNextPrayer(context)
            delay(1000L)
        }
    }

    ZCard(
        onClick = onClick,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        contentPadding = 18.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.AccessTime, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_next_prayer),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = prayer?.nameArabic ?: stringResource(R.string.home_prayer_unavailable),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                if (prayer != null) {
                    Text(
                        text = prayer!!.time,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    )
                }
            }
            if (prayer != null) {
                Column(horizontalAlignment = Alignment.End) {
                    AnimatedContent(
                        targetState = prayer!!.timeRemaining,
                        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(180)) },
                        label = "countdown",
                    ) { remaining ->
                        Text(remaining, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Text(
                        text = stringResource(R.string.home_remaining),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyAyahCard(modifier: Modifier = Modifier) {
    val ayahs = remember { dailyAyahCarousel }
    if (ayahs.isEmpty()) {
        ZCard(contentPadding = 20.dp, modifier = modifier.fillMaxWidth()) {}
        return
    }
    var index by remember { mutableIntStateOf(dailyAyahStartIndex().coerceIn(0, ayahs.lastIndex)) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(7000L)
            index = (index + 1) % ayahs.size
        }
    }
    ZCard(contentPadding = 20.dp, modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.home_daily_ayah),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(10.dp))
            AnimatedContent(
                targetState = index,
                transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
                label = "daily-ayah",
            ) { i ->
                val ayah = ayahs[i]
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = ayah.arabic,
                        style = AyahTextStyle,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "${ayah.surahName} • ${ayah.ayahNumber}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsGrid(onOpen: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        homeQuickActions.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { action ->
                    QuickActionTile(action = action, onClick = { onOpen(action.route) }, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickActionTile(action: QuickAction, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ZCard(onClick = onClick, contentPadding = 12.dp, modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(action.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(action.labelRes),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
