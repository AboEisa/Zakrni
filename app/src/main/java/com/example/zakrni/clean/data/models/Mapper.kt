package com.example.zakrni.clean.data.models

import com.example.zakrni.clean.domain.models.*

// ---------------- Prayer Mappers ----------------

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

// ---------------- Hadith Mappers ----------------

fun HadithResponse.mapToDomain(): DomainHadithResponse {
    return DomainHadithResponse(
        status = status,
        message = message,
        hadiths = hadiths.mapToDomain()
    )
}

fun Hadiths.mapToDomain(): DomainHadiths {
    return DomainHadiths(
        currentPage = current_page,
        data = `data`.map { it.mapToDomain() },
        total = total,
        lastPage = last_page,
        nextPageUrl = next_page_url,
        prevPageUrl = prev_page_url?.toString()
    )
}

fun Dataa.mapToDomain(): DomainHadith {
    return DomainHadith(
        id = id,
        book = book?.mapToDomain(),
        chapter = chapter?.mapToDomain(),
        bookSlug = bookSlug,
        chapterId = chapterId,
        hadithArabic = hadithArabic,
        hadithEnglish = hadithEnglish,
        hadithUrdu = hadithUrdu,
        englishNarrator = englishNarrator,
        urduNarrator = urduNarrator,
        status = status,
        volume = volume,
        headingArabic = headingArabic,
        headingEnglish = headingEnglish,
        headingUrdu = headingUrdu,
        hadithNumber = hadithNumber
    )
}

fun Book.mapToDomain(): DomainBook {
    return DomainBook(
        id = id,
        bookName = bookName,
        bookSlug = bookSlug,
        writerName = writerName,
        writerDeath = writerDeath,
        aboutWriter = aboutWriter?.toString()
    )
}

fun Chapter.mapToDomain(): DomainChapter {
    return DomainChapter(
        id = id,
        bookSlug = bookSlug,
        chapterArabic = chapterArabic,
        chapterEnglish = chapterEnglish,
        chapterUrdu = chapterUrdu,
        chapterNumber = chapterNumber
    )
}

fun Link.mapToDomain(): DomainLink {
    return DomainLink(
        label = label,
        url = url,
        active = active
    )
}
