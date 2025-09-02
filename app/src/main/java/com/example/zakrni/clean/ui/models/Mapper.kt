package com.example.zakrni.clean.ui.models

import com.example.zakrni.clean.domain.models.*

// ---------------- Prayer Times Presentation Mappers ----------------

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

// ---------------- Allah Names Presentation Mappers ----------------

fun DomainAsmaAlHusnaResponse.mapToPresentation(): PresentationAsmaAlHusnaResponse {
    return PresentationAsmaAlHusnaResponse(
        code = code,
        status = status,
        data = data.map { it.mapToPresentation() }
    )
}

fun DomainAllahNameData.mapToPresentation(): PresentationAllahNameData {
    return PresentationAllahNameData(
        name = name,
        transliteration = transliteration,
        number = number,
        en = en.mapToPresentation()
    )
}

fun DomainEn.mapToPresentation(): PresentationEn {
    return PresentationEn(
        meaning = meaning
    )
}

// ---------------- Hadith Presentation Mappers ----------------

fun DomainHadithResponse.mapToPresentation(): PresentationHadithResponse {
    return PresentationHadithResponse(
        status = status,
        message = message,
        hadiths = hadiths.mapToPresentation()
    )
}

fun DomainHadiths.mapToPresentation(): PresentationHadiths {
    return PresentationHadiths(
        currentPage = currentPage,
        data = data.map { it.mapToPresentation() },
        total = total,
        lastPage = lastPage,
        nextPageUrl = nextPageUrl,
        prevPageUrl = prevPageUrl
    )
}

fun DomainHadith.mapToPresentation(): PresentationHadith {
    return PresentationHadith(
        id = id,
        book = book?.mapToPresentation(),
        chapter = chapter?.mapToPresentation(),
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

fun DomainBook.mapToPresentation(): PresentationBook {
    return PresentationBook(
        id = id,
        bookName = bookName,
        bookSlug = bookSlug,
        writerName = writerName,
        writerDeath = writerDeath,
        aboutWriter = aboutWriter
    )
}

fun DomainChapter.mapToPresentation(): PresentationChapter {
    return PresentationChapter(
        id = id,
        bookSlug = bookSlug,
        chapterArabic = chapterArabic,
        chapterEnglish = chapterEnglish,
        chapterUrdu = chapterUrdu,
        chapterNumber = chapterNumber
    )
}

fun DomainLink.mapToPresentation(): PresentationLink {
    return PresentationLink(
        label = label,
        url = url,
        active = active
    )
}

// ---------------- Azkar Presentation Mappers ----------------

fun DomainAzkarResponse.mapToPresentation(): PresentationAzkarResponse {
    return PresentationAzkarResponse(
        adhan_azkar = adhan_azkar.map { it.mapToPresentation() },
        evening_azkar = evening_azkar.map { it.mapToPresentation() },
        food_azkar = food_azkar.map { it.mapToPresentation() },
        hajj_and_umrah_azkar = hajj_and_umrah_azkar.map { it.mapToPresentation() },
        home_azkar = home_azkar.map { it.mapToPresentation() },
        khala_azkar = khala_azkar.map { it.mapToPresentation() },
        miscellaneous_azkar = miscellaneous_azkar.map { it.mapToPresentation() },
        morning_azkar = morning_azkar.map { it.mapToPresentation() },
        mosque_azkar = mosque_azkar.map { it.mapToPresentation() },
        prayer_azkar = prayer_azkar.map { it.mapToPresentation() },
        prayer_later_azkar = prayer_later_azkar.map { it.mapToPresentation() },
        sleep_azkar = sleep_azkar.map { it.mapToPresentation() },
        wake_up_azkar = wake_up_azkar.map { it.mapToPresentation() },
        wudu_azkar = wudu_azkar.map { it.mapToPresentation() }
    )
}

fun DomainAdhanAzkar.mapToPresentation(): PresentationAdhanAzkar {
    return PresentationAdhanAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun DomainEveningAzkar.mapToPresentation(): PresentationEveningAzkar {
    return PresentationEveningAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun DomainFoodAzkar.mapToPresentation(): PresentationFoodAzkar {
    return PresentationFoodAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun DomainHajjAndUmrahAzkar.mapToPresentation(): PresentationHajjAndUmrahAzkar {
    return PresentationHajjAndUmrahAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun DomainHomeAzkar.mapToPresentation(): PresentationHomeAzkar {
    return PresentationHomeAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun DomainKhalaAzkar.mapToPresentation(): PresentationKhalaAzkar {
    return PresentationKhalaAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun DomainMiscellaneousAzkar.mapToPresentation(): PresentationMiscellaneousAzkar {
    return PresentationMiscellaneousAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun DomainMorningAzkar.mapToPresentation(): PresentationMorningAzkar {
    return PresentationMorningAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun DomainMosqueAzkar.mapToPresentation(): PresentationMosqueAzkar {
    return PresentationMosqueAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun DomainPrayerAzkar.mapToPresentation(): PresentationPrayerAzkar {
    return PresentationPrayerAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun DomainPrayerLaterAzkar.mapToPresentation(): PresentationPrayerLaterAzkar {
    return PresentationPrayerLaterAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun DomainSleepAzkar.mapToPresentation(): PresentationSleepAzkar {
    return PresentationSleepAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun DomainWakeUpAzkar.mapToPresentation(): PresentationWakeUpAzkar {
    return PresentationWakeUpAzkar(
        count = count,
        id = id,
        text = text
    )
}

fun DomainWuduAzkar.mapToPresentation(): PresentationWuduAzkar {
    return PresentationWuduAzkar(
        count = count,
        id = id,
        text = text
    )
}

// ---------------- Dua Presentation Mappers ----------------

fun DomainDuaResponse.mapToPresentation(): PresentationDuaResponse {
    return PresentationDuaResponse(
        prophetic_duas = prophetic_duas.map { it.mapToPresentation() },
        prophets_duas = prophets_duas.map { it.mapToPresentation() },
        quran_completion_duas = quran_completion_duas.map { it.mapToPresentation() },
        quran_duas = quran_duas.map { it.mapToPresentation() }
    )
}

fun DomainPropheticDua.mapToPresentation(): PresentationPropheticDua {
    return PresentationPropheticDua(
        count = count,
        id = id,
        text = text
    )
}

fun DomainProphetsDua.mapToPresentation(): PresentationProphetsDua {
    return PresentationProphetsDua(
        count = count,
        id = id,
        text = text
    )
}

fun DomainQuranCompletionDua.mapToPresentation(): PresentationQuranCompletionDua {
    return PresentationQuranCompletionDua(
        count = count,
        id = id,
        text = text
    )
}

fun DomainQuranDua.mapToPresentation(): PresentationQuranDua {
    return PresentationQuranDua(
        count = count,
        id = id,
        text = text
    )
}

// ---------------- Quran Presentation Mappers ----------------

fun DomainQuranResponse.mapToPresentation(): PresentationQuranResponse {
    return PresentationQuranResponse(
        code = code,
        status = status,
        data = data.mapToPresentation()
    )
}

fun DomainQuranData.mapToPresentation(): PresentationQuranData {
    return PresentationQuranData(
        edition = edition.mapToPresentation(),
        surahs = surahs.map { it.mapToPresentation() }
    )
}

fun DomainEdition.mapToPresentation(): PresentationEdition {
    return PresentationEdition(
        englishName = englishName,
        format = format,
        identifier = identifier,
        language = language,
        name = name,
        type = type
    )
}

fun DomainSurah.mapToPresentation(): PresentationSurah {
    return PresentationSurah(
        ayahs = ayahs.map { it.mapToPresentation() },
        englishName = englishName,
        englishNameTranslation = englishNameTranslation,
        name = name,
        number = number,
        revelationType = revelationType
    )
}

fun DomainAyah.mapToPresentation(): PresentationAyah {
    return PresentationAyah(
        hizbQuarter = hizbQuarter,
        juz = juz,
        manzil = manzil,
        number = number,
        numberInSurah = numberInSurah,
        page = page,
        ruku = ruku,
        sajda = sajda,
        text = text
    )
}