package com.zakrni.app.clean.data.models

import com.zakrni.app.clean.domain.models.*

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
// ---------------- Articles Presentation Mappers ----------------
fun ArticleResponse.mapToDomain(): DomainArticleResponse {
    return this.map { item ->
        DomainArticle(
            apiurl = item.apiurl,
            id = item.id,
            shortdescription = item.shortdescription,
            title = item.title
        )
    }
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
       content = content.map { it.mapToDomain() },
         title = title

    )
}
fun Content.mapToDomain(): DomainContent {
    return DomainContent(
        bless = bless,
        repeat = repeat,
        zekr = zekr
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

// ---------------- Quran Mappers ----------------

fun QuranResponse.mapToDomain(): DomainQuranResponse {
    return DomainQuranResponse(
        code = code,
        status = status,
        data = data?.mapToDomain()!!
    )
}

fun QuranData.mapToDomain(): DomainQuranData {
    return DomainQuranData(
        edition = edition?.mapToDomain()!!,
        surahs = surahs.map { it.mapToDomain() }
    )
}

fun Edition.mapToDomain(): DomainEdition {
    return DomainEdition(
        englishName = englishName,
        format = format,
        identifier = identifier,
        language = language,
        name = name,
        type = type
    )
}

fun Surah.mapToDomain(): DomainSurah {
    return DomainSurah(
        ayahs = ayahs.map { it.mapToDomain() },
        englishName = englishName,
        englishNameTranslation = englishNameTranslation,
        name = name,
        number = number,
        revelationType = revelationType
    )
}

fun Ayah.mapToDomain(): DomainAyah {
    return DomainAyah(
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


// ---------------- Quran Audio Mappers ----------------

fun QuranAudioResponse.mapToDomain(): DomainQuranAudioResponse {
    return DomainQuranAudioResponse(
        code = code,
        status = status,
        data = data.mapToDomain()
    )
}

fun QuranAudioData.mapToDomain(): DomainQuranAudioData {
    return DomainQuranAudioData(
        number = number,
        audio = audio,
        audioSecondary = audioSecondary,
        text = text,
        edition = edition.mapToDomain(),
        surah = surah.mapToDomain(),
        numberInSurah = numberInSurah
    )
}

fun AudioEdition.mapToDomain(): DomainAudioEdition {
    return DomainAudioEdition(
        identifier = identifier,
        language = language,
        name = name,
        englishName = englishName,
        type = type
    )
}

fun SurahInfo.mapToDomain(): DomainSurahInfo {
    return DomainSurahInfo(
        number = number,
        name = name,
        englishName = englishName,
        englishNameTranslation = englishNameTranslation,
        numberOfAyahs = numberOfAyahs,
        revelationType = revelationType
    )
}

fun AudioEditionsResponse.mapToDomain(): DomainAudioEditionsResponse {
    return DomainAudioEditionsResponse(
        code = code,
        status = status,
        data = data.map { it.mapToDomain() }
    )
}

