package com.zakrni.app.widget

import android.content.Context
import com.zakrni.app.clean.ui.models.PresentationTimings
import com.zakrni.app.clean.ui.utils.PrayerTimeUtils

/**
 * Snapshot of everything the home-screen widget renders.
 *
 * It is assembled by [loadWidgetData] from the same `prayer_times` SharedPreferences the rest of
 * the app writes (see [com.zakrni.app.clean.ui.utils.PrayerStorageManager]), so the widget stays in
 * sync without any extra wiring. The daily-ayah part is a tasteful placeholder for now — see the
 * note on [WidgetData.ayahText].
 */
data class WidgetData(
    /** Localised display name of the next prayer (Arabic or English depending on app locale). */
    val nextPrayerName: String,
    /** Formatted clock time of the next prayer, e.g. "05:14 AM". Empty when unavailable. */
    val nextPrayerTime: String,
    /** Pre-formatted countdown label, e.g. "2:13:40" or "07:25". Empty when unavailable. */
    val countdown: String,
    /** True when real prayer times were available; false means we are showing fallbacks. */
    val hasPrayerData: Boolean,
    /** Daily ayah body (Arabic). Currently a static placeholder — see class doc. */
    val ayahText: String,
    /** Ayah reference / surah name. Currently a static placeholder. */
    val ayahReference: String,
)

/**
 * SharedPreferences that [com.zakrni.app.clean.ui.utils.PrayerStorageManager] writes to.
 * Kept in sync intentionally; Glance widgets run in a lightweight context where injecting the
 * Hilt-scoped manager is overkill, so we read the same raw prefs directly.
 */
private const val PREFS_PRAYER_TIMES = "prayer_times"
private const val KEY_FAJR = "fajr"
private const val KEY_DHUHR = "dhuhr"
private const val KEY_ASR = "asr"
private const val KEY_MAGHRIB = "maghrib"
private const val KEY_ISHA = "isha"

/**
 * Reads saved prayer times and derives the next prayer + countdown using the very same
 * [PrayerTimeUtils.getCurrentAndNextPrayer] logic the in-app UI uses. Falls back to placeholders
 * when no times have been cached yet (e.g. fresh install before the first fetch).
 */
fun loadWidgetData(context: Context): WidgetData {
    val timings = readSavedTimings(context)

    var name = ""
    var time = ""
    var countdown = ""
    var hasData = false

    if (timings != null) {
        runCatching {
            val (_, next) = PrayerTimeUtils.getCurrentAndNextPrayer(timings)
            if (next != null) {
                name = if (isArabic()) next.nameArabic else next.name
                time = next.time
                countdown = next.timeRemaining
                hasData = true
            }
        }
    }

    return WidgetData(
        nextPrayerName = name,
        nextPrayerTime = time,
        countdown = countdown,
        hasPrayerData = hasData,
        // TODO(real-data): wire the cached daily ayah here. It lives as JSON in CacheManager under
        // "cached_daily_ayahs"; parsing it into the widget is a follow-up. Placeholder for now.
        ayahText = PLACEHOLDER_AYAH,
        ayahReference = PLACEHOLDER_AYAH_REF,
    )
}

private fun readSavedTimings(context: Context): PresentationTimings? {
    val prefs = context.getSharedPreferences(PREFS_PRAYER_TIMES, Context.MODE_PRIVATE)
    val fajr = prefs.getString(KEY_FAJR, null) ?: return null
    val dhuhr = prefs.getString(KEY_DHUHR, null) ?: return null
    val asr = prefs.getString(KEY_ASR, null) ?: return null
    val maghrib = prefs.getString(KEY_MAGHRIB, null) ?: return null
    val isha = prefs.getString(KEY_ISHA, null) ?: return null

    return PresentationTimings(
        Fajr = fajr,
        Dhuhr = dhuhr,
        Asr = asr,
        Maghrib = maghrib,
        Isha = isha,
        Sunrise = "",
        Sunset = "",
        Midnight = "",
        Imsak = "",
        Firstthird = "",
        Lastthird = "",
    )
}

private fun isArabic(): Boolean =
    java.util.Locale.getDefault().language.equals("ar", ignoreCase = true)

/** Static placeholder ayah (آية الكرسي excerpt) shown until real daily-ayah data is wired in. */
private const val PLACEHOLDER_AYAH =
    "وَإِلَٰهُكُمْ إِلَٰهٌ وَاحِدٌ ۖ لَّا إِلَٰهَ إِلَّا هُوَ الرَّحْمَٰنُ الرَّحِيمُ"
private const val PLACEHOLDER_AYAH_REF = "البقرة: ١٦٣"
