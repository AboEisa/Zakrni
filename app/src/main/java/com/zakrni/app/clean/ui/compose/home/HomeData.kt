package com.zakrni.app.clean.ui.compose.home

import android.content.Context
import com.zakrni.app.clean.ui.models.PresentationTimings
import com.zakrni.app.clean.ui.utils.PrayerTimeUtils
import java.util.Calendar

/**
 * Lightweight, dependency-free data sourcing for the Compose Home landing.
 *
 * This module is intentionally self-contained (no Hilt / ViewModel coupling) so the pre-shell
 * Home screen can render real cached data when it exists and degrade gracefully otherwise:
 *
 *  - [readNextPrayer] reads the same `prayer_times` SharedPreferences that
 *    `PrayerStorageManager` writes, then reuses [PrayerTimeUtils] for the next-prayer countdown.
 *  - [dailyAyah] / [DailyAyah] mirror the curated short-ayah set used by the legacy HomeActivity
 *    (`QuranViewModel.shortMeaningfulAyahs`, which is private and not reachable from here).
 *    Swap [dailyAyahCarousel] for a real feed later without touching the screen.
 */

/** A single verse shown in the Home "verse of the day" carousel. */
data class DailyAyah(
    val arabic: String,
    val translation: String,
    val surahName: String,
    val ayahNumber: Int,
)

/**
 * Curated short, meaningful verses (Arabic + English) — a tasteful placeholder mirroring the
 * legacy home carousel. Replace this list (or the call site) with a real data source when wired.
 */
val dailyAyahCarousel: List<DailyAyah> = listOf(
    DailyAyah("فَاذْكُرُونِي أَذْكُرْكُمْ", "So remember Me; I will remember you.", "البقرة", 152),
    DailyAyah("إِنَّ اللَّهَ مَعَ الصَّابِرِينَ", "Indeed, Allah is with the patient.", "البقرة", 153),
    DailyAyah("وَاللَّهُ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ", "And Allah is over all things competent.", "البقرة", 20),
    DailyAyah("حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ", "Sufficient for us is Allah, and He is the best Disposer of affairs.", "آل عمران", 173),
    DailyAyah("إِنَّ مَعَ الْعُسْرِ يُسْرًا", "Indeed, with hardship comes ease.", "الشرح", 6),
    DailyAyah("وَبَشِّرِ الصَّابِرِينَ", "And give good tidings to the patient.", "البقرة", 155),
)

/**
 * A stable "verse of the day" index derived from the day of the year, so the lead card is the
 * same all day but rotates daily. The carousel itself stays freely swipeable.
 */
fun dailyAyahStartIndex(): Int {
    if (dailyAyahCarousel.isEmpty()) return 0
    val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    return dayOfYear % dailyAyahCarousel.size
}

/** Reads cached prayer times (written by PrayerStorageManager) straight from SharedPreferences. */
private fun readCachedTimings(context: Context): PresentationTimings? {
    val prefs = context.getSharedPreferences("prayer_times", Context.MODE_PRIVATE)
    val fajr = prefs.getString("fajr", null) ?: return null
    val dhuhr = prefs.getString("dhuhr", null) ?: return null
    val asr = prefs.getString("asr", null) ?: return null
    val maghrib = prefs.getString("maghrib", null) ?: return null
    val isha = prefs.getString("isha", null) ?: return null
    return PresentationTimings(
        Fajr = fajr, Dhuhr = dhuhr, Asr = asr, Maghrib = maghrib, Isha = isha,
        Sunrise = "", Sunset = "", Midnight = "", Imsak = "", Firstthird = "", Lastthird = "",
    )
}

/**
 * The next upcoming prayer from cached times, or `null` when no times are cached yet.
 * Recomputing this on a ticking clock yields the live countdown on the Home card.
 */
fun readNextPrayer(context: Context): PrayerTimeUtils.PrayerInfo? {
    val timings = readCachedTimings(context) ?: return null
    return PrayerTimeUtils.getCurrentAndNextPrayer(timings).second
}
