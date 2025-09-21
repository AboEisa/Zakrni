package com.example.zakrni.clean.ui.models

import PresentationRadioStation
import PresentationReciter
import PresentationVideo
import com.example.zakrni.clean.domain.models.*
import java.text.SimpleDateFormat
import java.util.Locale


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

fun CombinedAzkarResponse.mapToPresentation(): PresentationAzkarResponse {
    return PresentationAzkarResponse(
        morning_azkar = morning?.content?.map { it.toPresentation() } ?: emptyList(),
        evening_azkar = evening?.content?.map { it.toPresentation() } ?: emptyList(),
        sleep_azkar = sleep?.content?.map { it.toPresentation() } ?: emptyList(),
        wake_up_azkar = wakeUp?.content?.map { it.toPresentation() } ?: emptyList(),
        prayer_azkar = prayer?.content?.map { it.toPresentation() } ?: emptyList(),
        mosque_azkar = mosque?.content?.map { it.toPresentation() } ?: emptyList(),
        food_azkar = eating?.content?.map { it.toPresentation() } ?: emptyList(),
        miscellaneous_azkar = misc?.content?.map { it.toPresentation() } ?: emptyList(),
        // Other categories remain empty as they're not fetched
        wudu_azkar = emptyList(),
        adhan_azkar = emptyList(),
        home_azkar = emptyList(),
        hajj_and_umrah_azkar = emptyList(),
        khala_azkar = emptyList(),
        prayer_later_azkar = emptyList()
    )
}

// Extension function to map DomainContent to PresentationAzkar
fun DomainContent.toPresentation(): PresentationAzkar {
    return PresentationAzkar(
        text = this.zekr,
        count = this.repeat,
        bless = this.bless
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



// ---------------- Quran Audio Presentation Mappers ----------------

fun DomainQuranAudioResponse.mapToPresentation(): PresentationQuranAudioResponse {
    return PresentationQuranAudioResponse(
        code = code,
        status = status,
        data = data.mapToPresentation()
    )
}

fun DomainQuranAudioData.mapToPresentation(): PresentationQuranAudioData {
    return PresentationQuranAudioData(
        number = number,
        audio = audio,
        audioSecondary = audioSecondary,
        text = text,
        edition = edition.mapToPresentation(),
        surah = surah.mapToPresentation(),
        numberInSurah = numberInSurah
    )
}

fun DomainAudioEdition.mapToPresentation(): PresentationAudioEdition {
    return PresentationAudioEdition(
        identifier = identifier,
        language = language,
        name = name,
        englishName = englishName,
        type = type
    )
}

fun DomainSurahInfo.mapToPresentation(): PresentationSurahInfo {
    return PresentationSurahInfo(
        number = number,
        name = name,
        englishName = englishName,
        englishNameTranslation = englishNameTranslation,
        numberOfAyahs = numberOfAyahs,
        revelationType = revelationType
    )
}

fun DomainAudioEditionsResponse.mapToPresentation(): PresentationAudioEditionsResponse {
    return PresentationAudioEditionsResponse(
        code = code,
        status = status,
        data = data.map { it.mapToPresentation() }
    )
}


// ui/models/Mapper.kt (add to existing)

fun DomainArticle.mapToPresentation(): PresentationArticle {
    return PresentationArticle(
        id = id,
        title = title,
        content = content,
        author = author,
        category = category,
        imageUrl = imageUrl,
        publishedDate = publishedDate,
        viewsCount = viewsCount,
        formattedDate = formatDate(publishedDate),
        readingTime = TODO()
    )
}

private fun formatDate(date: String): String {
    // Format date for display
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = sdf.parse(date)
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(parsedDate)
    } catch (e: Exception) {
        date
    }
}
// ui/models/Mapper.kt (add missing presentation mappers)



// Audio Mappers
fun DomainAudioLecture.mapToPresentation(): PresentationAudio {
    return PresentationAudio(
        id = id,
        title = title,
        sheikh = sheikh,
        description = description,
        audioUrl = audioUrl,
        duration = formatDuration(duration),
        seriesName = seriesName,
        episodeNumber = episodeNumber,
        publishedDate = publishedDate,
        playsCount = formatPlaysCount(playsCount)
    )
}

// Video Mappers
fun DomainVideo.mapToPresentation(): PresentationVideo {
    return PresentationVideo(
        id = id,
        title = title,
        description = description,
        videoUrl = videoUrl,
        thumbnailUrl = thumbnailUrl,
        duration = duration,
        views = formatViewCount(views),
        channelName = channelName,
        publishedDate = publishedDate,
        category = category,
        formattedDate = formatArabicDate(publishedDate)
    )
}

// Radio Mappers
fun DomainRadioStation.mapToPresentation(): PresentationRadioStation {
    return PresentationRadioStation(
        id = id,
        name = name,
        streamUrl = streamUrl,
        description = description,
        country = country,
        language = language,
        logoUrl = logoUrl,
        isLive = isLive
    )
}

// Reciters Mappers
fun DomainReciter.mapToPresentation(): PresentationReciter {
    return PresentationReciter(
        id = id,
        name = name,
        nameAr = nameAr,
        style = style,
        photoUrl = photoUrl,
        server = server,
        rewaya = rewaya,
        surahCount = "$surahCount سورة",
        availableSurahs = availableSurahs
    )
}

fun DomainReciterSurah.mapToPresentation(reciterName: String): PresentationReciterSurah {
    return PresentationReciterSurah(
        number = number,
        name = name,
        audioUrl = audioUrl,
        reciterName = reciterName
    )
}

// Helper functions
private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

private fun formatPlaysCount(count: Int): String {
    return when {
        count >= 1000000 -> "${count / 1000000}M استماع"
        count >= 1000 -> "${count / 1000}k استماع"
        else -> "$count استماع"
    }
}

private fun formatViewCount(count: Int): String {
    return when {
        count >= 1000000 -> "${count / 1000000}M مشاهدة"
        count >= 1000 -> "${count / 1000}k مشاهدة"
        else -> "$count مشاهدة"
    }
}

private fun formatArabicDate(date: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = sdf.parse(date) ?: return date
        val arabicFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ar"))
        arabicFormat.format(parsedDate)
    } catch (e: Exception) {
        date
    }
}