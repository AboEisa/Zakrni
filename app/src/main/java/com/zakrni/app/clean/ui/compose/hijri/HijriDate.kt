package com.zakrni.app.clean.ui.compose.hijri

import java.util.Calendar
import java.util.GregorianCalendar

/**
 * Self-contained Gregorian <-> Hijri conversion.
 *
 * minSdk is 24, so we cannot use [java.time.chrono.HijrahDate] (API 26+) nor rely on
 * core-library desugaring. This implements the *tabular* (arithmetic / "Kuwaiti") Islamic
 * calendar using the standard astronomical-civil epoch and a 30-year leap cycle. The result
 * is the widely-used arithmetic Hijri calendar; it can differ from official Umm al-Qura
 * sightings by +/-1 day, which is expected for any offline calculation and is acceptable here.
 *
 * Algorithm based on the classic Julian Day Number conversions
 * (Kazimierz M. Borkowski / Fliegel-Van Flandern), kept entirely in Kotlin.
 */

/** Immutable Hijri (Islamic) calendar date. [month] is 1..12, [day] is 1..30. */
data class HijriDate(
    val year: Int,
    val month: Int,
    val day: Int,
) {
    /** Bilingual month metadata for [month]. */
    val monthInfo: HijriMonth get() = HijriMonth.entries[month - 1]
}

/** The 12 Hijri months with Arabic names and a Latin transliteration. */
enum class HijriMonth(val arabic: String, val transliteration: String) {
    MUHARRAM("محرم", "Muharram"),
    SAFAR("صفر", "Safar"),
    RABI_AL_AWWAL("ربيع الأول", "Rabiʿ al-Awwal"),
    RABI_AL_THANI("ربيع الآخر", "Rabiʿ al-Thani"),
    JUMADA_AL_AWWAL("جمادى الأولى", "Jumada al-Awwal"),
    JUMADA_AL_THANI("جمادى الآخرة", "Jumada al-Thani"),
    RAJAB("رجب", "Rajab"),
    SHABAN("شعبان", "Shaʿban"),
    RAMADAN("رمضان", "Ramadan"),
    SHAWWAL("شوال", "Shawwal"),
    DHU_AL_QIDAH("ذو القعدة", "Dhul-Qiʿdah"),
    DHU_AL_HIJJAH("ذو الحجة", "Dhul-Hijjah"),
}

/**
 * Stateless converter between the Gregorian (proleptic) and tabular Hijri calendars,
 * routed through the Julian Day Number (JDN).
 */
object HijriCalendarConverter {

    private const val ISLAMIC_EPOCH = 1948440L // JDN of 1 Muharram, 1 AH (16 July 622 CE, civil epoch)

    /** Gregorian (year, 1-based month, day) -> Julian Day Number. */
    private fun gregorianToJdn(year: Int, month: Int, day: Int): Long {
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        return (day + (153 * m + 2) / 5 + 365L * y + y / 4 - y / 100 + y / 400 - 32045).toLong()
    }

    /** Julian Day Number -> Triple(year, 1-based month, day) in the Gregorian calendar. */
    private fun jdnToGregorian(jdn: Long): Triple<Int, Int, Int> {
        val a = jdn + 32044
        val b = (4 * a + 3) / 146097
        val c = a - (146097 * b) / 4
        val d = (4 * c + 3) / 1461
        val e = c - (1461 * d) / 4
        val m = (5 * e + 2) / 153
        val day = (e - (153 * m + 2) / 5 + 1).toInt()
        val month = (m + 3 - 12 * (m / 10)).toInt()
        val year = (100 * b + d - 4800 + m / 10).toInt()
        return Triple(year, month, day)
    }

    /** Tabular Hijri (year, 1-based month, day) -> Julian Day Number. */
    private fun hijriToJdn(year: Int, month: Int, day: Int): Long {
        return (day +
            Math.ceil(29.5 * (month - 1)).toLong() +
            (year - 1) * 354L +
            (3 + 11 * year) / 30L +
            ISLAMIC_EPOCH - 1)
    }

    /** Julian Day Number -> tabular Hijri date. */
    private fun jdnToHijri(jdn: Long): HijriDate {
        val daysSinceEpoch = jdn - ISLAMIC_EPOCH
        var year = ((30 * daysSinceEpoch + 10646) / 10631).toInt()
        if (year < 1) year = 1
        // Correct for boundary rounding.
        var month = Math.min(
            12,
            Math.ceil((jdn - (29 + hijriToJdn(year, 1, 1))) / 29.5).toInt() + 1,
        )
        if (month < 1) month = 1
        val day = (jdn - hijriToJdn(year, month, 1) + 1).toInt()
        return HijriDate(year, month, day)
    }

    /** Convert a Gregorian [Calendar] to a [HijriDate]. */
    fun toHijri(calendar: Calendar): HijriDate {
        val jdn = gregorianToJdn(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
        )
        return jdnToHijri(jdn)
    }

    /** Convert a [HijriDate] to a Gregorian [Calendar] (time set to local midnight). */
    fun toGregorian(hijri: HijriDate): Calendar {
        val jdn = hijriToJdn(hijri.year, hijri.month, hijri.day)
        val (y, m, d) = jdnToGregorian(jdn)
        return GregorianCalendar(y, m - 1, d)
    }

    /** Number of days (29 or 30) in the given tabular Hijri month. */
    fun lengthOfMonth(year: Int, month: Int): Int {
        val start = hijriToJdn(year, month, 1)
        val nextYear = if (month == 12) year + 1 else year
        val nextMonth = if (month == 12) 1 else month + 1
        val next = hijriToJdn(nextYear, nextMonth, 1)
        return (next - start).toInt()
    }

    /** Java [Calendar.DAY_OF_WEEK] (1=Sun..7=Sat) for day 1 of the given Hijri month. */
    fun firstDayOfWeek(year: Int, month: Int): Int {
        val cal = toGregorian(HijriDate(year, month, 1))
        return cal.get(Calendar.DAY_OF_WEEK)
    }
}
