package com.zakrni.app.clean.domain

import com.zakrni.app.clean.data.models.QuranResponse
import com.zakrni.app.clean.domain.models.DomainArticleResponse
import com.zakrni.app.clean.domain.models.DomainAsmaAlHusnaResponse
import com.zakrni.app.clean.domain.models.DomainAudioEditionsResponse
import com.zakrni.app.clean.domain.models.DomainAyah
import com.zakrni.app.clean.domain.models.DomainAzkarResponse
import com.zakrni.app.clean.domain.models.DomainHadithResponse
import com.zakrni.app.clean.domain.models.DomainHisnDuasResponse
import com.zakrni.app.clean.domain.models.DomainHisnSection
import com.zakrni.app.clean.domain.models.DomainPrayerTimesResponse
import com.zakrni.app.clean.domain.models.DomainQuranAudioResponse
import com.zakrni.app.clean.domain.models.DomainSurah

interface IRepo {
    suspend fun getPrayerTimes(
        latitude: Double,
        longitude: Double,
        method: Int = 5,
        school: Int = 0
    ): Result<DomainPrayerTimesResponse>

    suspend fun getAllahNames(): Result<DomainAsmaAlHusnaResponse>

    suspend fun getHadiths(page: Int, limit: Int = 10): Result<DomainHadithResponse>

    suspend fun getAzkarSabah(): Result<DomainAzkarResponse>
    suspend fun getAzkarMasaa(): Result<DomainAzkarResponse>
    suspend fun getAzkarPostPlayer(): Result<DomainAzkarResponse>
    suspend fun getAzkarNoom(): Result<DomainAzkarResponse>
    suspend fun getAzkarWake(): Result<DomainAzkarResponse>
    suspend fun getAzkarMosque(): Result<DomainAzkarResponse>
    suspend fun getAzkarEating(): Result<DomainAzkarResponse>
    suspend fun getAzkarMisc(): Result<DomainAzkarResponse>

    // Hisn Duas
    suspend fun getHisnSections(): Result<List<DomainHisnSection>>
    suspend fun getHisnDuas(section: String?, query: String?, page: Int, limit: Int): Result<DomainHisnDuasResponse>

    suspend fun getAllSurahs(): List<DomainSurah>
    suspend fun getQuranVerses(suraNumber: Int): List<DomainAyah>
    suspend fun getSurahAudio(surahNumber: Int, reciter: String): Result<QuranResponse>
    suspend fun getAudioEditions(language: String): Result<DomainAudioEditionsResponse>
    suspend fun getAyahAudio(reference: String, edition: String): Result<DomainQuranAudioResponse>


    // Articles
    suspend fun getArticles(): Result<DomainArticleResponse>

    // Get ayah with translation
    suspend fun getAyahWithTranslation(surahNumber: Int, ayahNumber: Int): Result<QuranResponse>
}