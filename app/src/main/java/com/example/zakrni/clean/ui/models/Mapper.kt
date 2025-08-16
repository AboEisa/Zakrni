package com.example.zakrni.clean.ui.models

import com.example.zakrni.clean.domain.models.*

fun DomainPrayerTimesResponse.mapToPresentation(): PresentationPrayerTimesResponse {
    return PresentationPrayerTimesResponse(
        code = code,
        status = status,
        data = data.mapToPresentation()
    )
}

fun DomainData.mapToPresentation(): PresentationData {
    return PresentationData(
        date = date.mapToPresentation(),
        meta = meta.mapToPresentation(),
        timings = timings.mapToPresentation()
    )
}

fun DomainDate.mapToPresentation(): PresentationDate {
    return PresentationDate(
        gregorian = gregorian.mapToPresentation(),
        hijri = hijri.mapToPresentation(),
        readable = readable,
        timestamp = timestamp
    )
}

fun DomainMeta.mapToPresentation(): PresentationMeta {
    return PresentationMeta(
        latitude = latitude,
        latitudeAdjustmentMethod = latitudeAdjustmentMethod,
        longitude = longitude,
        method = method.mapToPresentation(),
        midnightMode = midnightMode,
        offset = offset.mapToPresentation(),
        school = school,
        timezone = timezone
    )
}

fun DomainTimings.mapToPresentation(): PresentationTimings {
    return PresentationTimings(
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

fun DomainGregorian.mapToPresentation(): PresentationGregorian {
    return PresentationGregorian(
        date = date,
        day = day,
        designation = designation.mapToPresentation(),
        format = format,
        lunarSighting = lunarSighting,
        month = month.mapToPresentation(),
        weekday = weekday.mapToPresentation(),
        year = year
    )
}

fun DomainHijri.mapToPresentation(): PresentationHijri {
    return PresentationHijri(
        adjustedHolidays = adjustedHolidays,
        date = date,
        day = day,
        designation = designation.mapToPresentation(),
        format = format,
        holidays = holidays,
        method = method,
        month = month.mapToPresentationX(),
        weekday = weekday.mapToPresentationX(),
        year = year
    )
}

fun DomainDesignation.mapToPresentation(): PresentationDesignation {
    return PresentationDesignation(
        abbreviated = abbreviated,
        expanded = expanded
    )
}

fun DomainMonth.mapToPresentation(): PresentationMonth {
    return PresentationMonth(
        en = en,
        number = number
    )
}

fun DomainWeekday.mapToPresentation(): PresentationWeekday {
    return PresentationWeekday(
        en = en
    )
}

fun DomainMonthX.mapToPresentationX(): PresentationMonthX {
    return PresentationMonthX(
        ar = ar,
        days = days,
        en = en,
        number = number
    )
}

fun DomainWeekdayX.mapToPresentationX(): PresentationWeekdayX {
    return PresentationWeekdayX(
        ar = ar,
        en = en
    )
}

fun DomainMethod.mapToPresentation(): PresentationMethod {
    return PresentationMethod(
        id = id,
        location = location.mapToPresentation(),
        name = name,
        params = params.mapToPresentation()
    )
}

fun DomainOffset.mapToPresentation(): PresentationOffset {
    return PresentationOffset(
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

fun DomainLocation.mapToPresentation(): PresentationLocation {
    return PresentationLocation(
        latitude = latitude,
        longitude = longitude
    )
}

fun DomainParams.mapToPresentation(): PresentationParams {
    return PresentationParams(
        Fajr = Fajr,
        Isha = Isha
    )



}