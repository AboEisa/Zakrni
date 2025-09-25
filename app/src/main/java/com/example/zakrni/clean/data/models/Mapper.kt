package com.example.zakrni.clean.data.models

import com.example.zakrni.clean.domain.models.*
import com.example.zakrni.clean.ui.models.PresentationArticle
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

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
        data = data.mapToDomain()
    )
}

fun QuranData.mapToDomain(): DomainQuranData {
    return DomainQuranData(
        edition = edition.mapToDomain(),
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
// data/models/Mapper.kt (add to existing)
fun ArticlesResponse.mapToDomain(): DomainArticlesResponse {
    return DomainArticlesResponse(
        code = code,
        status = status,
        articles = data.articles.map { it.mapToDomain() },
        pagination = DomainPagination(
            total = data.total,
            currentPage = data.current_page,
            lastPage = data.last_page
        )
    )
}

fun Article.mapToDomain(): DomainArticle {
    return DomainArticle(
        id = id,
        title = title,
        content = content,
        author = author,
        category = category,
        imageUrl = image_url,
        publishedDate = published_date,
        viewsCount = views_count,
        readingTime = reading_time
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
        viewsCount = formatViewCount(viewsCount),
        readingTime = "$readingTime دقيقة قراءة",
        formattedDate = formatArabicDate(publishedDate)
    )
}

private fun formatViewCount(count: Int): String {
    return when {
        count >= 1000 -> "${count / 1000}k مشاهدة"
        else -> "$count مشاهدة"
    }
}

private fun formatArabicDate(date: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = sdf.parse(date)
        val diffInDays = TimeUnit.MILLISECONDS.toDays(
            System.currentTimeMillis() - parsedDate.time
        )
        when {
            diffInDays == 0L -> "اليوم"
            diffInDays == 1L -> "أمس"
            diffInDays < 7 -> "منذ $diffInDays أيام"
            diffInDays < 30 -> "منذ ${diffInDays / 7} أسابيع"
            else -> SimpleDateFormat("dd MMM yyyy", Locale("ar")).format(parsedDate)
        }
    } catch (e: Exception) {
        date
    }
}

// data/models/Mapper.kt (add missing mappers)

// Audio/Lectures Mappers
fun AudioResponse.mapToDomain(): DomainAudioResponse {
    return DomainAudioResponse(
        code = code,
        status = status,
        lectures = data.lectures.map { it.mapToDomain() }
    )
}

fun AudioLecture.mapToDomain(): DomainAudioLecture {
    return DomainAudioLecture(
        id = id,
        title = title,
        sheikh = sheikh,
        description = description,
        audioUrl = audio_url,
        duration = duration,
        seriesName = series_name,
        episodeNumber = episode_number,
        publishedDate = published_date,
        playsCount = plays_count
    )
}

// Videos Mappers
fun VideosResponse.mapToDomain(): DomainVideosResponse {
    return DomainVideosResponse(
        code = code,
        status = status,
        videos = data.videos.map { it.mapToDomain() },
        total = data.total
    )
}

fun Video.mapToDomain(): DomainVideo {
    return DomainVideo(
        id = id,
        title = title,
        description = description,
        videoUrl = video_url,
        thumbnailUrl = thumbnail_url,
        duration = duration,
        views = views,
        channelName = channel_name,
        publishedDate = published_date,
        category = category
    )
}

// Radio Mappers
fun RadioResponse.mapToDomain(): DomainRadioResponse {
    return DomainRadioResponse(
        code = code,
        status = status,
        stations = data.stations.map { it.mapToDomain() }
    )
}

fun RadioStation.mapToDomain(): DomainRadioStation {
    return DomainRadioStation(
        id = id,
        name = name,
        streamUrl = url,
        description = description,
        country = country,
        language = language,
        logoUrl = logo_url,
        isLive = is_live
    )
}

// Reciters Mappers
fun RecitersResponse.mapToDomain(): DomainRecitersResponse {
    return DomainRecitersResponse(
        code = code,
        status = status,
        reciters = data.reciters.map { it.mapToDomain() }
    )
}

fun Reciter.mapToDomain(): DomainReciter {
    return DomainReciter(
        id = id,
        name = name,
        nameAr = name_ar,
        style = style,
        photoUrl = photo_url,
        server = server,
        rewaya = rewaya,
        surahCount = count,
        availableSurahs = suras_list
    )
}

fun ReciterSurahsResponse.mapToDomain(): DomainReciterSurahsResponse {
    return DomainReciterSurahsResponse(
        reciterInfo = data.reciter.mapToDomain(),
        surahs = data.surahs.map { it.mapToDomain() }
    )
}

fun ReciterInfo.mapToDomain(): DomainReciterInfo {
    return DomainReciterInfo(
        id = id,
        name = name,
        server = server
    )
}

fun ReciterSurah.mapToDomain(): DomainReciterSurah {
    return DomainReciterSurah(
        number = number,
        name = name,
        audioUrl = audio_url
    )
}