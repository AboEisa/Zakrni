package com.example.zakrni.clean.domain

import com.example.zakrni.clean.data.models.AudioEditionsResponse
import com.example.zakrni.clean.data.models.QuranAudioResponse
import com.example.zakrni.clean.data.models.QuranResponse
import com.example.zakrni.clean.domain.models.DomainAsmaAlHusnaResponse
import com.example.zakrni.clean.domain.models.DomainAudioEdition
import com.example.zakrni.clean.domain.models.DomainAudioEditionsResponse
import com.example.zakrni.clean.domain.models.DomainAzkarResponse
import com.example.zakrni.clean.domain.models.DomainDuaResponse
import com.example.zakrni.clean.domain.models.DomainHadithResponse
import com.example.zakrni.clean.domain.models.DomainPrayerTimesResponse
import com.example.zakrni.clean.domain.models.DomainQuranAudioResponse
import com.example.zakrni.clean.domain.models.DomainAyah
import com.example.zakrni.clean.domain.models.DomainSurah

interface IRepo {
    suspend fun getPrayerTimes(
        latitude: Double,
        longitude: Double
    ): Result<DomainPrayerTimesResponse>

    suspend fun getAllahNames(): Result<DomainAsmaAlHusnaResponse>

    suspend fun getHadiths(page: Int, limit: Int = 10): Result<DomainHadithResponse>

    suspend fun getAzkar(): Result<DomainAzkarResponse>

    suspend fun getDuas(): Result<DomainDuaResponse>

    suspend fun getAllSurahs(): List<DomainSurah>
    suspend fun getQuranVerses(suraNumber: Int): List<DomainAyah>
    suspend fun getSurahAudio(surahNumber: Int, reciter: String): Result<QuranResponse>
    suspend fun getAudioEditions(language: String): Result<DomainAudioEditionsResponse>
    suspend fun getAyahAudio(reference: String, edition: String): Result<DomainQuranAudioResponse>
}