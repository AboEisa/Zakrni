package com.zakrni.app.clean.domain.models

data class DomainPrayerTimesResponse(
    val code: Int,
    val `data`: DomainData,
    val status: String
)

data class DomainData(
    val date: DomainDate,
    val meta: DomainMeta,
    val timings: DomainTimings
)

data class DomainDate(
    val gregorian: DomainGregorian,
    val hijri: DomainHijri,
    val readable: String,
    val timestamp: String
)

data class DomainMeta(
    val latitude: Double,
    val latitudeAdjustmentMethod: String,
    val longitude: Double,
    val method: DomainMethod,
    val midnightMode: String,
    val offset: DomainOffset,
    val school: String,
    val timezone: String
)

data class DomainTimings(
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

data class DomainGregorian(
    val date: String,
    val day: String,
    val designation: DomainDesignation,
    val format: String,
    val lunarSighting: Boolean,
    val month: DomainMonth,
    val weekday: DomainWeekday,
    val year: String
)

data class DomainHijri(
    val adjustedHolidays: List<Any?>,
    val date: String,
    val day: String,
    val designation: DomainDesignation,
    val format: String,
    val holidays: List<Any?>,
    val method: String,
    val month: DomainMonthX,
    val weekday: DomainWeekdayX,
    val year: String
)

data class DomainDesignation(
    val abbreviated: String,
    val expanded: String
)

data class DomainMonth(
    val en: String,
    val number: Int
)

data class DomainWeekday(
    val en: String
)

data class DomainMonthX(
    val ar: String,
    val days: Int,
    val en: String,
    val number: Int
)

data class DomainWeekdayX(
    val ar: String,
    val en: String
)

data class DomainMethod(
    val id: Int,
    val location: DomainLocation,
    val name: String,
    val params: DomainParams
)

data class DomainOffset(
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

data class DomainLocation(
    val latitude: Double,
    val longitude: Double
)

data class DomainParams(
    val Fajr: Double,
    val Isha: Double
)