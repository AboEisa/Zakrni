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
            val cleanTime = time.split(" ")[0] // Remove timezone
            val timeParts = cleanTime.split(":")
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()

            // Create Calendar for today with prayer time
            val prayerCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val prayerTimeInSeconds = prayerCalendar.timeInMillis / 1000

            // Convert to 12-hour format without AM/PM
            val formattedTime = formatTimeTo12Hour(hour, minute)

            Quadruple(name, nameArabic, prayerTimeInSeconds, formattedTime)
        }

        var currentPrayer: PrayerInfo? = null
        var nextPrayer: PrayerInfo? = null

        // Find current and next prayer
        for (i in prayerTimes.indices) {
            val (name, nameArabic, prayerTimeInSeconds, formattedTime) = prayerTimes[i]

            if (currentTimeInSeconds >= prayerTimeInSeconds) {
                currentPrayer = PrayerInfo(
                    name = name,
                    nameArabic = nameArabic,
                    time = formattedTime,
                    timeRemaining = "",
                    timeRemainingInSeconds = 0,
                    isNext = false
                )
            }

            if (currentTimeInSeconds < prayerTimeInSeconds) {
                val remainingSeconds = prayerTimeInSeconds - currentTimeInSeconds
                nextPrayer = PrayerInfo(
                    name = name,
                    nameArabic = nameArabic,
                    time = formattedTime,
                    timeRemaining = formatTimeRemaining(remainingSeconds),
                    timeRemainingInSeconds = remainingSeconds,
                    isNext = true
                )
                break
            }
        }

        // If no next prayer found (past Isha), next prayer is Fajr tomorrow
        if (nextPrayer == null && prayerTimes.isNotEmpty()) {
            val fajr = prayerTimes[0]

            // Calculate Fajr for tomorrow
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

            nextPrayer = PrayerInfo(
                name = fajr.first,
                nameArabic = fajr.second,
                time = fajr.fourth,
                timeRemaining = formatTimeRemaining(remainingSeconds),
                timeRemainingInSeconds = remainingSeconds,
                isNext = true
            )
        }

        return Pair(currentPrayer, nextPrayer)
    }

    // Convert 24-hour format to 12-hour format without AM/PM
    private fun formatTimeTo12Hour(hour: Int, minute: Int): String {
        val hour12 = when {
            hour == 0 -> 12 // Midnight -> 12
            hour > 12 -> hour - 12 // PM hours
            else -> hour // AM hours (1-12)
        }

        return String.format("%d:%02d", hour12, minute)
    }

    // DEPRECATED: Keep for backward compatibility
    private fun formatTime(timeInMinutes: Int): String {
        val hour = timeInMinutes / 60
        val minute = timeInMinutes % 60
        return formatTimeTo12Hour(hour, minute)
    }

    fun formatTimeRemaining(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, secs)
            else -> String.format("%02d:%02d", minutes, secs)
        }
    }

    // NEW: Use API data for Hijri date formatting
    fun formatHijriDateFromApi(prayerTimesResponse: PresentationPrayerTimesResponse): String {
        return with(prayerTimesResponse.data.date) {
            val dayName = hijri.weekday.ar
            val day = hijri.day
            val monthName = hijri.month.ar
            val year = hijri.year

            "$dayName $day $monthName $year هـ"
        }
    }

    // NEW: Use API data for Gregorian date formatting
    fun formatGregorianDateFromApi(prayerTimesResponse: PresentationPrayerTimesResponse): String {
        return with(prayerTimesResponse.data.date) {
            val dayName = getCurrentDayNameInArabic() // You can also use gregorian.weekday.en and translate
            val day = gregorian.day
            val monthName = translateMonthToArabic(gregorian.month.en)
            val year = gregorian.year

            "$dayName $day $monthName $year م"
        }
    }

    // DEPRECATED: Keep these for backward compatibility but they should be replaced
    fun formatHijriDate(hijriDate: String): String {
        // This method should be replaced with formatHijriDateFromApi
        val parts = hijriDate.split(" ")
        if (parts.size < 4) return hijriDate // Invalid format
        val dayName = parts[0] // e.g., "الأحد"
        val day = parts[1] // e.g., "1"
        val monthName = parts[2] // e.g., "محرم"
        val year = parts[3] // e.g., "1445"
        val hijriDate = "$dayName $day $monthName $year هـ"
        return hijriDate
    }

    fun formatGregorianDate(gregorianDate: String): String {
        // This method should be replaced with formatGregorianDateFromApi
        return gregorianDate
    }

    private fun getCurrentDayNameInArabic(): String {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        return when (dayOfWeek) {
            Calendar.SUNDAY -> "الأحد"
            Calendar.MONDAY -> "الاثنين"
            Calendar.TUESDAY -> "الثلاثاء"
            Calendar.WEDNESDAY -> "الأربعاء"
            Calendar.THURSDAY -> "الخميس"
            Calendar.FRIDAY -> "الجمعة"
            Calendar.SATURDAY -> "السبت"
            else -> "الأحد" // default case
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

    // Helper data class for quadruple
    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}