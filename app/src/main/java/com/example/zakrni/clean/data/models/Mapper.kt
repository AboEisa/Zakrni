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

// ---------------- Allah Names Mappers ----------------

fun AsmaAlHusnaResponse.mapToDomain(): DomainAsmaAlHusnaResponse {
    return DomainAsmaAlHusnaResponse(
        code = code,
        status = status,
        data = data.map { it.mapToDomain() }
    )
}

fun AllahNameData.mapToDomain(): DomainAllahNameData {
    return DomainAllahNameData(
        name = name,
        transliteration = transliteration,
        number = number,
        en = en.mapToDomain()
    )
}

fun En.mapToDomain(): DomainEn {
    return DomainEn(
        meaning = meaning
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
        prevPageUrl = prev_page_url
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
        aboutWriter = aboutWriter
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

// ---------------- Azkar Mappers ----------------


fun AzkarResponse.mapToDomain(): DomainAzkarResponse {
    return DomainAzkarResponse(
        adhan_azkar = adhan_azkar.map { it.mapToDomain() },
        evening_azkar = evening_azkar.map { it.mapToDomain() },
        food_azkar = food_azkar.map { it.mapToDomain() },
        hajj_and_umrah_azkar = hajj_and_umrah_azkar.map { it.mapToDomain() },
        home_azkar = home_azkar.map { it.mapToDomain() },
        khala_azkar = khala_azkar.map { it.mapToDomain() },
        miscellaneous_azkar = miscellaneous_azkar.map { it.mapToDomain() },
        morning_azkar = morning_azkar.map { it.mapToDomain() },
        mosque_azkar = mosque_azkar.map { it.mapToDomain() },
        prayer_azkar = prayer_azkar.map { it.mapToDomain() },
        prayer_later_azkar = prayer_later_azkar.map { it.mapToDomain() },
        sleep_azkar = sleep_azkar.map { it.mapToDomain() },
        wake_up_azkar = wake_up_azkar.map { it.mapToDomain() },
        wudu_azkar = wudu_azkar.map { it.mapToDomain() }
    )
}

fun AdhanAzkar.mapToDomain(): DomainAdhanAzkar {
    return DomainAdhanAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun EveningAzkar.mapToDomain(): DomainEveningAzkar {
    return DomainEveningAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun FoodAzkar.mapToDomain(): DomainFoodAzkar {
    return DomainFoodAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun HajjAndUmrahAzkar.mapToDomain(): DomainHajjAndUmrahAzkar {
    return DomainHajjAndUmrahAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun HomeAzkar.mapToDomain(): DomainHomeAzkar {
    return DomainHomeAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun KhalaAzkar.mapToDomain(): DomainKhalaAzkar {
    return DomainKhalaAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun MiscellaneousAzkar.mapToDomain(): DomainMiscellaneousAzkar {
    return DomainMiscellaneousAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun MorningAzkar.mapToDomain(): DomainMorningAzkar {
    return DomainMorningAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun MosqueAzkar.mapToDomain(): DomainMosqueAzkar {
    return DomainMosqueAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun PrayerAzkar.mapToDomain(): DomainPrayerAzkar {
    return DomainPrayerAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun PrayerLaterAzkar.mapToDomain(): DomainPrayerLaterAzkar {
    return DomainPrayerLaterAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun SleepAzkar.mapToDomain(): DomainSleepAzkar {
    return DomainSleepAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun WakeUpAzkar.mapToDomain(): DomainWakeUpAzkar {
    return DomainWakeUpAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun WuduAzkar.mapToDomain(): DomainWuduAzkar {
    return DomainWuduAzkar(
        count = count,
        id = id,
        text = text
    )
}

// ---------------- Dua Mappers ----------------

fun DuaResponse.mapToDomain(): DomainDuaResponse {
    return DomainDuaResponse(
        prophetic_duas = prophetic_duas.map { it.mapToDomain() },
        prophets_duas = prophets_duas.map { it.mapToDomain() },
        quran_completion_duas = quran_completion_duas.map { it.mapToDomain() },
        quran_duas = quran_duas.map { it.mapToDomain() }
    )
}

fun PropheticDua.mapToDomain(): DomainPropheticDua {
    return DomainPropheticDua(
        count = count,
        id = id,
        text = text
    )
}

fun ProphetsDua.mapToDomain(): DomainProphetsDua {
    return DomainProphetsDua(
        count = count,
        id = id,
        text = text
    )
}

fun QuranCompletionDua.mapToDomain(): DomainQuranCompletionDua {
    return DomainQuranCompletionDua(
        count = count,
        id = id,
        text = text
    )
}

fun QuranDua.mapToDomain(): DomainQuranDua {
    return DomainQuranDua(
        count = count,
        id = id,
        text = text
    )
}
