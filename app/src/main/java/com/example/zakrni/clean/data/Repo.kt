package com.example.zakrni.clean.data

import androidx.annotation.OptIn
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.zakrni.clean.domain.models.DomainSurah

import com.example.zakrni.clean.data.local.ILocalDataSource
import com.example.zakrni.clean.data.models.AsmaAlHusnaResponse
import com.example.zakrni.clean.data.models.AudioEdition
import com.example.zakrni.clean.data.models.AudioEditionsResponse
import com.example.zakrni.clean.data.models.PrayerTimesResponse
import com.example.zakrni.clean.data.models.QuranResponse
import com.example.zakrni.clean.data.models.mapToDomain
import com.example.zakrni.clean.data.network.PrayerApiService
import com.example.zakrni.clean.data.remote.IRemoteDataSource
import com.example.zakrni.clean.domain.IRepo
import com.example.zakrni.clean.domain.models.DomainAllahNameData
import com.example.zakrni.clean.domain.models.DomainAsmaAlHusnaResponse
import com.example.zakrni.clean.domain.models.DomainAyah
import com.example.zakrni.clean.domain.models.DomainAzkarResponse
import com.example.zakrni.clean.domain.models.DomainDuaResponse
import com.example.zakrni.clean.domain.models.DomainHadithResponse
import com.example.zakrni.clean.domain.models.DomainPrayerTimesResponse
import com.example.zakrni.clean.ui.utils.Constant.Companion.APIKEY
import javax.inject.Inject

class Repo @Inject constructor(
    private val remoteDataSource: IRemoteDataSource,
) : IRepo {
    override suspend fun getPrayerTimes(
        latitude: Double,
        longitude: Double
    ): Result<DomainPrayerTimesResponse> {
        return try {
            val data = remoteDataSource.getPrayerTimes(latitude, longitude)
            Result.success(data.getOrThrow().mapToDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllahNames(): Result<DomainAsmaAlHusnaResponse> {
        return try {
            val data = remoteDataSource.getAllahNames()
            Result.success(data.getOrThrow().mapToDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getHadiths(page: Int, limit: Int): Result<DomainHadithResponse> {
        return try {
            val data = remoteDataSource.getHadiths(page, limit)
            Result.success(data.getOrThrow().mapToDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAzkar(): Result<DomainAzkarResponse> {
        return try {
            val data = remoteDataSource.getAzkar()
            Result.success(data.getOrThrow().mapToDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDuas(): Result<DomainDuaResponse> {
        return try {
            val data = remoteDataSource.getDuas()
            Result.success(data.getOrThrow().mapToDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @OptIn(UnstableApi::class)
    override suspend fun getQuranVerses(suraNumber: Int): List<DomainAyah> {
        return try {
            val ayahs = remoteDataSource.getQuranVerses(suraNumber) // <-- get from remote
            Log.d("QURAN_API", "Repo - Surah $suraNumber has ${ayahs.size} ayahs")
            ayahs
        } catch (e: Exception) {
            Log.e("QURAN_API", "Repo - Error fetching verses: ${e.message}", e)
            emptyList()
        }
    }


    override suspend fun getAllSurahs(): List<DomainSurah> {
        return try {
            println("DEBUG: Repo - Getting all surahs")
            val surahs = remoteDataSource.getSurahList()
            println("DEBUG: Repo - Got ${surahs.size} surahs from remote source")
            surahs
        } catch (e: Exception) {
            println("DEBUG: Repo - Error getting surahs: ${e.message}")
            emptyList()
        }
    }


    override suspend fun getSurahAudio(
        surahNumber: Int,
        reciter: String
    ): Result<QuranResponse> {
        return remoteDataSource.getSurahAudio(surahNumber, reciter)
    }

    override suspend fun getAudioEditions(language: String): Result<List<AudioEdition>> {
        return try {
            val response = remoteDataSource.getAudioEditions(language)
            if (response.isSuccess) {
                val editions = response.getOrThrow()
                Result.success(editions)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}




