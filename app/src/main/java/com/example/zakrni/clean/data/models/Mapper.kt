package com.example.zakrni.clean.data.models

import com.example.zakrni.clean.domain.models.*

fun PrayerTimesResponse.mapToDomain(): DomainPrayerTimesResponse {
    return DomainPrayerTimesResponse(
        code = code,
        status = status,
        data = data.mapToDomain()
    )
}

fun Data.mapToDomain(): DomainData {
    return DomainData(
        date = date.mapToDomain(),
        meta = meta.mapToDomain(),
        timings = timings.mapToDomain()
    )
}

fun Date.mapToDomain(): DomainDate {
    return DomainDate(
        gregorian = gregorian.mapToDomain(),
        hijri = hijri.mapToDomain(),
        readable = readable,
        timestamp = timestamp
    )
}

fun Meta.mapToDomain(): DomainMeta {
    return DomainMeta(
        latitude = latitude,
        latitudeAdjustmentMethod = latitudeAdjustmentMethod,
        longitude = longitude,
        method = method.mapToDomain(),
        midnightMode = midnightMode,
        offset = offset.mapToDomain(),
        school = school,
        timezone = timezone
    )
}

fun Timings.mapToDomain(): DomainTimings {
    return DomainTimings(
        Asr = Asr,
        Dhuhr = Dhuhr,
        Fajr = Fajr,
        Firstthird = Firstthird,
        Imsak = Imsak,
        Isha = Isha,
        Lastthird = Lastthird,
        Maghrib = Maghrib,
        Midnight = Midnight,
        Sunrise = Sunrise,
        Sunset = Sunset
    )
}

fun Gregorian.mapToDomain(): DomainGregorian {
    return DomainGregorian(
        date = date,
        day = day,
        designation = designation.mapToDomain(),
        format = format,
        lunarSighting = lunarSighting,
        month = month.mapToDomain(),
        weekday = weekday.mapToDomain(),
        year = year
    )
}

fun Hijri.mapToDomain(): DomainHijri {
    return DomainHijri(
        adjustedHolidays = adjustedHolidays,
        date = date,
        day = day,
        designation = designation.mapToDomain(),
        format = format,
        holidays = holidays,
        method = method,
        month = month.mapToDomainX(),
        weekday = weekday.mapToDomainX(),
        year = year
    )
}

fun Designation.mapToDomain(): DomainDesignation {
    return DomainDesignation(
        abbreviated = abbreviated,
        expanded = expanded
    )
}

fun Month.mapToDomain(): DomainMonth {
    return DomainMonth(
        en = en,
        number = number
    )
}

fun Weekday.mapToDomain(): DomainWeekday {
    return DomainWeekday(
        en = en
    )
}

fun MonthX.mapToDomainX(): DomainMonthX {
    return DomainMonthX(
        ar = ar,
        days = days,
        en = en,
        number = number
    )
}

fun WeekdayX.mapToDomainX(): DomainWeekdayX {
    return DomainWeekdayX(
        ar = ar,
        en = en
    )
}

fun Method.mapToDomain(): DomainMethod {
    return DomainMethod(
        id = id,
        location = location.mapToDomain(),
        name = name,
        params = params.mapToDomain()
    )
}

fun Offset.mapToDomain(): DomainOffset {
    return DomainOffset(
        Asr = Asr,
        Dhuhr = Dhuhr,
        Fajr = Fajr,
        Imsak = Imsak,
        Isha = Isha,
        Maghrib = Maghrib,
        Midnight = Midnight,
        Sunrise = Sunrise,
        Sunset = Sunset
    )
}

fun Location.mapToDomain(): DomainLocation {
    return DomainLocation(
        latitude = latitude,
        longitude = longitude
    )
}

fun Params.mapToDomain(): DomainParams {
    return DomainParams(
        Fajr = Fajr,
        Isha = Isha
    )
}