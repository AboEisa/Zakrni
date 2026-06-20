package com.zakrni.app.clean.ui.compose.prayer

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.zakrni.app.R
import com.zakrni.app.clean.ui.models.PresentationTimings
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * A single row in the prayer-times list (Compose redesign).
 *
 * @param key stable API key ("Fajr", "Dhuhr", …). Matches [com.zakrni.app.clean.ui.utils.PrayerTimeUtils.PrayerInfo.name].
 *            Sunrise is a daylight marker, not a prayer, so it never counts as "current".
 */
internal data class PrayerRowUi(
    val key: String,
    @StringRes val nameRes: Int,
    @DrawableRes val icon: Int,
    val time: String,
    val isPrayer: Boolean,
)

/** Localized 12-hour formatting that mirrors the legacy fragments (e.g. "05:14 AM"). */
internal fun formatPrayerTime(raw: String): String {
    return try {
        val cleanTime = raw.split(" ")[0] // strip any timezone suffix
        val input = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        val output = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        input.parse(cleanTime)?.let { output.format(it) } ?: raw
    } catch (e: Exception) {
        raw
    }
}

/**
 * Builds the full ordered day list: Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha — matching the
 * legacy `fragment_all_prayer_times.xml` order. Sunrise is included but flagged `isPrayer = false`.
 */
internal fun PresentationTimings.toPrayerRows(): List<PrayerRowUi> = listOf(
    PrayerRowUi("Fajr", R.string.prn_fajr, R.drawable.ic_fajr, formatPrayerTime(Fajr), isPrayer = true),
    PrayerRowUi("Sunrise", R.string.prn_sunrise, R.drawable.ic_sunrise, formatPrayerTime(Sunrise), isPrayer = false),
    PrayerRowUi("Dhuhr", R.string.prn_dhuhr, R.drawable.ic_dhuhr, formatPrayerTime(Dhuhr), isPrayer = true),
    PrayerRowUi("Asr", R.string.prn_asr, R.drawable.ic_asr, formatPrayerTime(Asr), isPrayer = true),
    PrayerRowUi("Maghrib", R.string.prn_maghrib, R.drawable.ic_maghrib, formatPrayerTime(Maghrib), isPrayer = true),
    PrayerRowUi("Isha", R.string.prn_isha, R.drawable.ic_isha, formatPrayerTime(Isha), isPrayer = true),
)

/** The five obligatory prayers only (drops Sunrise) — used by the timeline on the main tab. */
internal fun PresentationTimings.toPrayerRowsNoSunrise(): List<PrayerRowUi> =
    toPrayerRows().filter { it.isPrayer }

/** Maps an API prayer key to its localized string resource. */
@StringRes
internal fun prayerNameRes(key: String): Int = when (key) {
    "Fajr" -> R.string.prn_fajr
    "Sunrise" -> R.string.prn_sunrise
    "Dhuhr" -> R.string.prn_dhuhr
    "Asr" -> R.string.prn_asr
    "Maghrib" -> R.string.prn_maghrib
    "Isha" -> R.string.prn_isha
    else -> R.string.prn_fajr
}
