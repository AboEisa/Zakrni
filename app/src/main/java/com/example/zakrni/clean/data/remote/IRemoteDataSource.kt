package com.example.zakrni.clean.data.remote

import com.example.zakrni.clean.data.models.ArticleResponse
import com.example.zakrni.clean.data.models.AsmaAlHusnaResponse
import com.example.zakrni.clean.data.models.AudioEdition
import com.example.zakrni.clean.data.models.AudioEditionsResponse
import com.example.zakrni.clean.data.models.AzkarResponse
import com.example.zakrni.clean.data.models.DuaResponse
import com.example.zakrni.clean.data.models.HadithResponse
import com.example.zakrni.clean.data.models.PrayerTimesResponse
import com.example.zakrni.clean.data.models.QuranAudioResponse
import com.example.zakrni.clean.data.models.QuranResponse
import com.example.zakrni.clean.domain.models.DomainAyah
import com.example.zakrni.clean.domain.models.DomainSurah

interface IRemoteDataSource {

    suspend fun getPrayerTimes(
        latitude: Double,
        longitude: Double,
        method: Int = 5
    ): Result<PrayerTimesResponse>

    suspend fun getAllahNames(): Result<AsmaAlHusnaResponse>

    suspend fun getHadiths(page: Int, limit: Int = 20): Result<HadithResponse>

    suspend fun getAzkarSabah(): Result<AzkarResponse>
    suspend fun getAzkarMasaa(): Result<AzkarResponse>
    suspend fun getAzkarPostPlayer(): Result<AzkarResponse>
    suspend fun getAzkarNoom(): Result<AzkarResponse>
    suspend fun getAzkarWake(): Result<AzkarResponse>
    suspend fun getAzkarMosque(): Result<AzkarResponse>
    suspend fun getAzkarEating(): Result<AzkarResponse>
    suspend fun getAzkarMisc(): Result<AzkarResponse>

//    suspend fun getDuas(): Result<DuaResponse>

    suspend fun getQuranVerses(surahNumber: Int): List<DomainAyah>

    suspend fun getSurahList(): List<DomainSurah>

    suspend fun getSurahAudio(surahNumber: Int, reciter: String): Result<QuranResponse>

    suspend fun getAudioEditions(language: String): Result<AudioEditionsResponse>

    suspend fun getAyahAudio(reference: String, edition: String): Result<QuranAudioResponse>

    //articles
    suspend fun getArticles(): Result<ArticleResponse>
}