package com.example.zakrni.clean.ui.utils

import com.example.zakrni.clean.ui.models.PresentationTimings
import com.example.zakrni.clean.ui.models.PresentationPrayerTimesResponse
import java.text.SimpleDateFormat
import java.util.*

object PrayerTimeUtils {

    data class PrayerInfo(
        val name: String,
        val nameArabic: String,
        val time: String,
        val timeRemaining: String,
        val timeRemainingInSeconds: Long,
        val isNext: Boolean
    )

    fun getCurrentAndNextPrayer(timings: PresentationTimings): Pair<PrayerInfo?, PrayerInfo?> {
        val currentTime = Calendar.getInstance()
        val currentTimeInSeconds = currentTime.timeInMillis / 1000

        val prayers = listOf(
            Triple("Fajr", "الفجر", timings.Fajr),
            Triple("Dhuhr", "الظهر", timings.Dhuhr),
            Triple("Asr", "العصر", timings.Asr),
            Triple("Maghrib", "المغرب", timings.Maghrib),
            Triple("Isha", "العشاء", timings.Isha)
        )

        val prayerTimes = prayers.map { (name, nameArabic, time) ->
            val cleanTime = time.split(" ")[0]
            val timeParts = cleanTime.split(":")
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()

            val prayerCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val prayerTimeInSeconds = prayerCalendar.timeInMillis / 1000

            // ✅ صيغة 12 ساعة + AM/PM
            val formattedTime = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(prayerCalendar.time)

            Quadruple(name, nameArabic, prayerTimeInSeconds, formattedTime)
        }

        var currentPrayer: PrayerInfo? = null
        var nextPrayer: PrayerInfo? = null

        for (i in prayerTimes.indices) {
            val (name, nameArabic, prayerTimeInSeconds, formattedTime) = prayerTimes[i]

            if (currentTimeInSeconds >= prayerTimeInSeconds) {
                currentPrayer = PrayerInfo(
                    name, nameArabic, formattedTime, "", 0, false
                )
            }

            if (currentTimeInSeconds < prayerTimeInSeconds) {
                val remainingSeconds = prayerTimeInSeconds - currentTimeInSeconds
                nextPrayer = PrayerInfo(
                    name, nameArabic, formattedTime,
                    formatTimeRemaining(remainingSeconds),
                    remainingSeconds, true
                )
                break
            }
        }

        if (nextPrayer == null && prayerTimes.isNotEmpty()) {
            val fajr = prayerTimes[0]
            val tomorrowFajr = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 1)
                val cleanTime = timings.Fajr.split(" ")[0]
                val timeParts = cleanTime.split(":")
                set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                set(Calendar.MINUTE, timeParts[1].toInt())
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val remainingSeconds = (tomorrowFajr.timeInMillis / 1000) - currentTimeInSeconds
            val formattedFajrTime = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(tomorrowFajr.time)

            nextPrayer = PrayerInfo(
                fajr.first, fajr.second, formattedFajrTime,
                formatTimeRemaining(remainingSeconds),
                remainingSeconds, true
            )
        }

        return Pair(currentPrayer, nextPrayer)
    }

    fun formatTimeRemaining(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%02d:%02d", minutes, secs)
        }
    }



    fun formatHijriDateFromApi(prayerTimesResponse: PresentationPrayerTimesResponse): String {
        return with(prayerTimesResponse.data.date) {
            "${hijri.weekday.ar} ${hijri.day} ${hijri.month.ar} ${hijri.year} هـ"
        }
    }

    fun formatGregorianDateFromApi(prayerTimesResponse: PresentationPrayerTimesResponse): String {
        return with(prayerTimesResponse.data.date) {
            "${getCurrentDayNameInArabic()} ${gregorian.day} ${translateMonthToArabic(gregorian.month.en)} ${gregorian.year} م"
        }
    }

    private fun getCurrentDayNameInArabic(): String {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "الأحد"
            Calendar.MONDAY -> "الاثنين"
            Calendar.TUESDAY -> "الثلاثاء"
            Calendar.WEDNESDAY -> "الأربعاء"
            Calendar.THURSDAY -> "الخميس"
            Calendar.FRIDAY -> "الجمعة"
            Calendar.SATURDAY -> "السبت"
            else -> "الأحد"
        }
    }

    private fun translateMonthToArabic(englishMonth: String): String {
        return when (englishMonth.lowercase()) {
            "january" -> "يناير"
            "february" -> "فبراير"
            "march" -> "مارس"
            "april" -> "أبريل"
            "may" -> "مايو"
            "june" -> "يونيو"
            "july" -> "يوليو"
            "august" -> "أغسطس"
            "september" -> "سبتمبر"
            "october" -> "أكتوبر"
            "november" -> "نوفمبر"
            "december" -> "ديسمبر"
            else -> englishMonth
        }
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
