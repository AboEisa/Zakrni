package com.example.zakrni.clean.data

import androidx.annotation.OptIn
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.zakrni.clean.domain.models.DomainSurah
import com.example.zakrni.clean.data.local.ILocalDataSource
import com.example.zakrni.clean.data.models.ArticlesResponse
import com.example.zakrni.clean.data.models.AsmaAlHusnaResponse
import com.example.zakrni.clean.data.models.AudioEdition
import com.example.zakrni.clean.data.models.AudioEditionsResponse
import com.example.zakrni.clean.data.models.PrayerTimesResponse
import com.example.zakrni.clean.data.models.QuranResponse
import com.example.zakrni.clean.data.models.RadioResponse
import com.example.zakrni.clean.data.models.RecitersResponse
import com.example.zakrni.clean.data.models.VideosResponse
import com.example.zakrni.clean.data.models.mapToDomain
import com.example.zakrni.clean.data.network.MediaApiService
import com.example.zakrni.clean.data.network.PrayerApiService
import com.example.zakrni.clean.data.remote.IRemoteDataSource
import com.example.zakrni.clean.domain.IRepo
import com.example.zakrni.clean.domain.models.DomainAllahNameData
import com.example.zakrni.clean.domain.models.DomainArticlesResponse
import com.example.zakrni.clean.domain.models.DomainAsmaAlHusnaResponse
import com.example.zakrni.clean.domain.models.DomainAudioEditionsResponse
import com.example.zakrni.clean.domain.models.DomainAudioResponse
import com.example.zakrni.clean.domain.models.DomainAyah
import com.example.zakrni.clean.domain.models.DomainAzkarResponse
import com.example.zakrni.clean.domain.models.DomainDuaResponse
import com.example.zakrni.clean.domain.models.DomainHadithResponse
import com.example.zakrni.clean.domain.models.DomainPrayerTimesResponse
import com.example.zakrni.clean.domain.models.DomainQuranAudioResponse
import com.example.zakrni.clean.domain.models.DomainRadioResponse
import com.example.zakrni.clean.domain.models.DomainRecitersResponse
import com.example.zakrni.clean.domain.models.DomainVideosResponse
import com.example.zakrni.clean.ui.utils.Constant.Companion.APIKEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class Repo @Inject constructor(
    private val remoteDataSource: IRemoteDataSource,
    private val localDataSource: ILocalDataSource,
    private val mediaApiService: MediaApiService
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

    override suspend fun getAzkarSabah(): Result<DomainAzkarResponse> {
        return try {
            val data = remoteDataSource.getAzkarSabah()
            Result.success(data.getOrThrow().mapToDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAzkarMasaa(): Result<DomainAzkarResponse> {
        return try {
            val data = remoteDataSource.getAzkarMasaa()
            Result.success(data.getOrThrow().mapToDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAzkarPostPlayer(): Result<DomainAzkarResponse> {
        return try {
            val data = remoteDataSource.getAzkarPostPlayer()
            Result.success(data.getOrThrow().mapToDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAzkarNoom(): Result<DomainAzkarResponse> {
        return try {
            val data = remoteDataSource.getAzkarNoom()
            Result.success(data.getOrThrow().mapToDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAzkarWake(): Result<DomainAzkarResponse> {
        return try {
            val data = remoteDataSource.getAzkarWake()
            Result.success(data.getOrThrow().mapToDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAzkarMosque(): Result<DomainAzkarResponse> {
        return try {
            val data = remoteDataSource.getAzkarMosque()
            Result.success(data.getOrThrow().mapToDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAzkarEating(): Result<DomainAzkarResponse> {
        return try {
            val data = remoteDataSource.getAzkarEating()
            Result.success(data.getOrThrow().mapToDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAzkarMisc(): Result<DomainAzkarResponse> {
        return try {
            val data = remoteDataSource.getAzkarMisc()
            Result.success(data.getOrThrow().mapToDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    @OptIn(UnstableApi::class)
    override suspend fun getQuranVerses(suraNumber: Int): List<DomainAyah> =
        withContext(Dispatchers.IO) {
            try {
                // Check local cache first
                val cachedAyahs = localDataSource.getAyahsBySurah(suraNumber)
                if (cachedAyahs.isNotEmpty()) {
                    return@withContext cachedAyahs
                }
                val remoteAyahs = remoteDataSource.getQuranVerses(suraNumber)
                if (remoteAyahs.isNotEmpty()) {
                    localDataSource.saveAyahs(suraNumber, remoteAyahs)
                }

                remoteAyahs
            } catch (e: Exception) {
                println("DEBUG: Repo - Error fetching ayahs: ${e.message}")
                localDataSource.getAyahsBySurah(suraNumber)
            }
        }

    suspend fun preloadAllQuranData(onProgress: (Int, Int) -> Unit = { _, _ -> }) {
        withContext(Dispatchers.IO) {
            try {
                if (localDataSource.isQuranDataCached()) {
                    println("DEBUG: Repo - Quran data already cached")
                    return@withContext
                }

                println("DEBUG: Repo - Starting Quran data preload")

                // Load and cache all surahs
                val surahs = remoteDataSource.getSurahList()
                localDataSource.saveSurahs(surahs)
                // Load verses for each surah
                surahs.forEachIndexed { index, surah ->
                    onProgress(index + 1, 114)

                    val verses = remoteDataSource.getQuranVerses(surah.number)
                    if (verses.isNotEmpty()) {
                        localDataSource.saveAyahs(surah.number, verses)
                    }

                    // Small delay to avoid overwhelming the API
                    kotlinx.coroutines.delay(2000)
                }
            } catch (e: Exception) {
                println("DEBUG: Repo - Error preloading data: ${e.message}")
            }
        }
    }

    override suspend fun getAllSurahs(): List<DomainSurah> = withContext(Dispatchers.IO) {
        try {
            // Check local cache first
            val cachedSurahs = localDataSource.getAllSurahs()
            if (cachedSurahs.isNotEmpty()) {
                return@withContext cachedSurahs
            }

            // If no cache, fetch from API
            val remoteSurahs = remoteDataSource.getSurahList()

            // Save to cache if successful
            if (remoteSurahs.isNotEmpty()) {
                localDataSource.saveSurahs(remoteSurahs)
            }

            remoteSurahs
        } catch (e: Exception) {
            println("DEBUG: Repo - Error fetching surahs: ${e.message}")
            // Try to return cached data on error
            localDataSource.getAllSurahs()
        }
    }

    override suspend fun getSurahAudio(
        surahNumber: Int,
        reciter: String
    ): Result<QuranResponse> {
        return remoteDataSource.getSurahAudio(surahNumber, reciter)
    }

    override suspend fun getAudioEditions(language: String): Result<DomainAudioEditionsResponse> {
        return try {
            val response = remoteDataSource.getAudioEditions(language)
            if (response.isSuccess) {
                val audioEditionsResponse = response.getOrThrow()
                Result.success(audioEditionsResponse.mapToDomain())
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAyahAudio(
        reference: String,
        edition: String
    ): Result<DomainQuranAudioResponse> {
        return try {
            val response = remoteDataSource.getAyahAudio(reference, edition)
            if (response.isSuccess) {
                val quranAudioResponse = response.getOrThrow()
                Result.success(quranAudioResponse.mapToDomain())
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getArticles(page: Int, category: String?): Result<ArticlesResponse> {
        return try {
            val response = mediaApiService.getArticles(page, category = category)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVideos(page: Int, category: String?, query: String?): Result<VideosResponse> {
        return try {
            val response = mediaApiService.getVideos(page, category, query)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    override suspend fun getAudioLectures(
        page: Int,
        sheikh: String?
    ): Result<DomainAudioResponse> {
        return try {
            val response = mediaApiService.getAudioLectures(page, sheikh)
            Result.success(response.mapToDomain()) // implement mapToDomain()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getRadioStations(country: String?, language: String?): Result<RadioResponse> {
        return try {
            val response = mediaApiService.getRadioStations(country, language)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReciters(rewaya: String?): Result<RecitersResponse> {
        return try {
            val response = mediaApiService.getReciters(rewaya)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}