package com.zakrni.app.widget

import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.zakrni.app.R
import com.zakrni.app.clean.ui.theme.BrandGold
import com.zakrni.app.clean.ui.theme.BrandTeal

/**
 * Home-screen widget for ذكرني / Zakrni.
 *
 * Shows the next prayer (name + time + a countdown-style label) and the daily ayah, in the app's
 * brand palette. Built with Glance + [GlanceTheme] (Material3) so neutral roles follow the system
 * light/dark theme. Tapping the widget opens the app.
 *
 * Real prayer data is read from SharedPreferences in [loadWidgetData]; the ayah is a placeholder
 * until the cached daily-ayah JSON is wired in (see [WidgetData]).
 */
class ZakrniGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = loadWidgetData(context)
        // Resolve user-facing strings here, where we have a real Context.
        val strings = WidgetStrings(
            appName = context.getString(R.string.widget_app_name),
            nextPrayerLabel = context.getString(R.string.widget_next_prayer_label),
            remainingPrefix = context.getString(R.string.widget_remaining_prefix),
            dailyAyahLabel = context.getString(R.string.widget_daily_ayah_label),
            noData = context.getString(R.string.widget_no_data),
        )
        provideContent {
            GlanceTheme {
                WidgetContent(data, strings)
            }
        }
    }
}

/** User-facing strings, resolved once against a real Context. */
private data class WidgetStrings(
    val appName: String,
    val nextPrayerLabel: String,
    val remainingPrefix: String,
    val dailyAyahLabel: String,
    val noData: String,
)

@Composable
private fun WidgetContent(data: WidgetData, strings: WidgetStrings) {
    // Brand tokens: a deep-teal card with gold accents reads well on any wallpaper.
    val tealBg = ColorProvider(BrandTeal)
    val gold = ColorProvider(BrandGold)
    // Neutral text roles come from GlanceTheme so contrast adapts to light/dark.
    val onCard = GlanceTheme.colors.inverseOnSurface
    val muted = GlanceTheme.colors.outline

    val launchApp = actionStartActivity(
        ComponentName("com.zakrni.app", "com.zakrni.app.clean.ui.views.MainActivity"),
    )

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(tealBg)
            .cornerRadius(20.dp())
            .padding(16.dp())
            .clickable(launchApp),
        verticalAlignment = Alignment.Top,
    ) {
        // ---- Header: app name + "Next prayer" label ----
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = strings.appName,
                style = TextStyle(color = gold, fontSize = 14.sp, fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = strings.nextPrayerLabel,
                style = TextStyle(color = muted, fontSize = 11.sp),
            )
        }

        Spacer(modifier = GlanceModifier.height(10.dp()))

        // ---- Next prayer: name + time + countdown ----
        if (data.hasPrayerData) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = data.nextPrayerName,
                    style = TextStyle(color = onCard, fontSize = 22.sp, fontWeight = FontWeight.Bold),
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = data.nextPrayerTime,
                    style = TextStyle(color = gold, fontSize = 18.sp, fontWeight = FontWeight.Medium),
                )
            }
            Spacer(modifier = GlanceModifier.height(6.dp()))
            // Countdown chip
            Text(
                text = strings.remainingPrefix + " " + data.countdown,
                style = TextStyle(color = onCard, fontSize = 13.sp, fontWeight = FontWeight.Medium),
                modifier = GlanceModifier
                    .background(ColorProvider(BrandGold.copy(alpha = 0.18f)))
                    .cornerRadius(10.dp())
                    .padding(horizontal = 10.dp(), vertical = 4.dp()),
            )
        } else {
            Text(
                text = strings.noData,
                style = TextStyle(color = muted, fontSize = 14.sp),
            )
        }

        Spacer(modifier = GlanceModifier.height(12.dp()))

        // ---- Gold divider ----
        Spacer(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(1.dp())
                .background(ColorProvider(BrandGold.copy(alpha = 0.5f))),
        )

        Spacer(modifier = GlanceModifier.height(10.dp()))

        // ---- Daily ayah ----
        Text(
            text = strings.dailyAyahLabel,
            style = TextStyle(color = gold, fontSize = 11.sp, fontWeight = FontWeight.Medium),
        )
        Spacer(modifier = GlanceModifier.height(4.dp()))
        Text(
            text = data.ayahText,
            maxLines = 3,
            style = TextStyle(color = onCard, fontSize = 15.sp, textAlign = TextAlign.Center),
            modifier = GlanceModifier.fillMaxWidth(),
        )
        Spacer(modifier = GlanceModifier.height(4.dp()))
        Text(
            text = data.ayahReference,
            style = TextStyle(color = muted, fontSize = 11.sp, textAlign = TextAlign.End),
            modifier = GlanceModifier.fillMaxWidth(),
        )
    }
}

/** Local `Int.dp` / `Float.dp` helper so we don't import the Compose `Dp` extension by name. */
private fun Int.dp() = androidx.compose.ui.unit.Dp(this.toFloat())
