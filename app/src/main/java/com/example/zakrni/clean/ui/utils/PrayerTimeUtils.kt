package com.example.zakrni.clean.ui.utils

import com.example.zakrni.clean.ui.models.PresentationTimings
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

            Quadruple(name, nameArabic, prayerTimeInSeconds, formatTime(hour * 60 + minute))
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

    private fun formatTime(timeInMinutes: Int): String {
        val hour = timeInMinutes / 60
        val minute = timeInMinutes % 60
        return String.format("%02d:%02d", hour, minute)
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

    fun formatHijriDate(hijriDate: String): String {
        try {
            // Get current day name in Arabic
            val dayName = getCurrentDayNameInArabic()

            // Assuming the API returns hijri date in format "DD-MM-YYYY"
            val parts = hijriDate.split("-")
            if (parts.size == 3) {
                val day = parts[0]
                val month = parts[1].toInt()
                val year = parts[2]

                val hijriMonths = arrayOf(
                    "", "محرم", "صفر", "ربيع الأول", "ربيع الثاني", "جمادى الأولى",
                    "جمادى الثانية", "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
                )

                val monthName = if (month in 1..12) hijriMonths[month] else ""
                return "$dayName $day $monthName $year هـ"
            }
        } catch (e: Exception) {
            // Return original if parsing fails
        }
        return hijriDate
    }

    fun formatGregorianDate(gregorianDate: String): String {
        try {
            // Assuming API returns format "DD-MM-YYYY"
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ar"))
            val date = inputFormat.parse(gregorianDate)

            val formattedDate = outputFormat.format(date ?: Date())
            return "$formattedDate م"
        } catch (e: Exception) {
            // Fallback formatting
            try {
                val parts = gregorianDate.split("-")
                if (parts.size == 3) {
                    val day = parts[0]
                    val month = parts[1].toInt()
                    val year = parts[2]

                    val gregorianMonths = arrayOf(
                        "", "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
                        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
                    )

                    val monthName = if (month in 1..12) gregorianMonths[month] else ""
                    return "$day $monthName $year م"
                }
            } catch (e: Exception) {
                // Return original if all parsing fails
            }
        }
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
            else -> "الأحد" // fallback
        }
    }

    // Helper data class for quadruple
    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}