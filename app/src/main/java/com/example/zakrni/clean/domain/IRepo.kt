package com.example.zakrni.clean.domain

import com.example.zakrni.clean.data.models.AudioEditionsResponse
import com.example.zakrni.clean.data.models.AzkarResponse
import com.example.zakrni.clean.data.models.QuranAudioResponse
import com.example.zakrni.clean.data.models.QuranResponse
import com.example.zakrni.clean.domain.models.DomainArticlesResponse
import com.example.zakrni.clean.domain.models.DomainAsmaAlHusnaResponse
import com.example.zakrni.clean.domain.models.DomainAudioEdition
import com.example.zakrni.clean.domain.models.DomainAudioEditionsResponse
import com.example.zakrni.clean.domain.models.DomainAudioResponse
import com.example.zakrni.clean.domain.models.DomainAzkarResponse
import com.example.zakrni.clean.domain.models.DomainDuaResponse
import com.example.zakrni.clean.domain.models.DomainHadithResponse
import com.example.zakrni.clean.domain.models.DomainPrayerTimesResponse
import com.example.zakrni.clean.domain.models.DomainQuranAudioResponse
import com.example.zakrni.clean.domain.models.DomainAyah
import com.example.zakrni.clean.domain.models.DomainRadioResponse
import com.example.zakrni.clean.domain.models.DomainRecitersResponse
import com.example.zakrni.clean.domain.models.DomainSurah
import com.example.zakrni.clean.domain.models.DomainVideosResponse

interface IRepo {
    suspend fun getPrayerTimes(
        latitude: Double,
        longitude: Double
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

//    suspend fun getDuas(): Result<DomainDuaResponse>

    suspend fun getAllSurahs(): List<DomainSurah>
    suspend fun getQuranVerses(suraNumber: Int): List<DomainAyah>
    suspend fun getSurahAudio(surahNumber: Int, reciter: String): Result<QuranResponse>
    suspend fun getAudioEditions(language: String): Result<DomainAudioEditionsResponse>
    suspend fun getAyahAudio(reference: String, edition: String): Result<DomainQuranAudioResponse>

    suspend fun getArticles(page: Int, category: String?): Result<DomainArticlesResponse>
    suspend fun getVideos(page: Int, category: String?, query: String?): Result<DomainVideosResponse>
    suspend fun getAudioLectures(page: Int, sheikh: String?): Result<DomainAudioResponse>
    suspend fun getRadioStations(country: String?, language: String?): Result<DomainRadioResponse>
    suspend fun getReciters(rewaya: String?): Result<DomainRecitersResponse>

}