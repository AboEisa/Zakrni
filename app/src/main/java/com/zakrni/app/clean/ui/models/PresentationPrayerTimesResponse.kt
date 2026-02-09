package com.zakrni.app.clean.ui.models

data class PresentationPrayerTimesResponse(
    val code: Int,
    val `data`: PresentationData,
    val status: String
)

data class PresentationData(
    val date: PresentationDate,
    val meta: PresentationMeta,
    val timings: PresentationTimings
)

data class PresentationDate(
    val gregorian: PresentationGregorian,
    val hijri: PresentationHijri,
    val readable: String,
    val timestamp: String
)

data class PresentationMeta(
    val latitude: Double,
    val latitudeAdjustmentMethod: String,
    val longitude: Double,
    val method: PresentationMethod,
    val midnightMode: String,
    val offset: PresentationOffset,
    val school: String,
    val timezone: String
)

data class PresentationTimings(
    val Asr: String,
    val Dhuhr: String,
    val Fajr: String,
    val Firstthird: String,
    val Imsak: String,
    val Isha: String,
    val Lastthird: String,
    val Maghrib: String,
    val Midnight: String,
    val Sunrise: String,
    val Sunset: String
)

data class PresentationGregorian(
    val date: String,
    val day: String,
    val designation: PresentationDesignation,
    val format: String,
    val lunarSighting: Boolean,
    val month: PresentationMonth,
    val weekday: PresentationWeekday,
    val year: String
)

data class PresentationHijri(
    val adjustedHolidays: List<Any?>,
    val date: String,
    val day: String,
    val designation: PresentationDesignation,
    val format: String,
    val holidays: List<Any?>,
    val method: String,
    val month: PresentationMonthX,
    val weekday: PresentationWeekdayX,
    val year: String
)

data class PresentationDesignation(
    val abbreviated: String,
    val expanded: String
)

data class PresentationMonth(
    val en: String,
    val number: Int
)

data class PresentationWeekday(
    val en: String
)

data class PresentationMonthX(
    val ar: String,
    val days: Int,
    val en: String,
    val number: Int
)

data class PresentationWeekdayX(
    val ar: String,
    val en: String
)

data class PresentationMethod(
    val id: Int,
    val location: PresentationLocation,
    val name: String,
    val params: PresentationParams
)

data class PresentationOffset(
    val Asr: Int,
    val Dhuhr: Int,
    val Fajr: Int,
    val Imsak: Int,
    val Isha: Int,
    val Maghrib: Int,
    val Midnight: Int,
    val Sunrise: Int,
    val Sunset: Int
)

data class PresentationLocation(
    val latitude: Double,
    val longitude: Double
)

data class PresentationParams(
    val Fajr: Double,
    val Isha: Double
)