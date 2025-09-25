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
import com.example.zakrni.clean.data.network.ArticleApiService
import com.example.zakrni.clean.data.network.AzkarApiService
import com.example.zakrni.clean.data.network.HadithApiService
import com.example.zakrni.clean.data.network.PrayerApiService
import com.example.zakrni.clean.data.network.QuranApiService
import com.example.zakrni.clean.domain.models.DomainAyah
import com.example.zakrni.clean.domain.models.DomainSurah
import com.example.zakrni.clean.ui.utils.Constant.Companion.APIKEY
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val apiPrayerServices: PrayerApiService,
    private val apiHadithsServices: HadithApiService,
    private val apiAzkarServices: AzkarApiService,
    private val quranApiService: QuranApiService,
    private val articleApiService: ArticleApiService
): IRemoteDataSource {

    override suspend fun getPrayerTimes(
        latitude: Double,
        longitude: Double,
        method: Int
    ): Result<PrayerTimesResponse> {
        return try {
            val response = apiPrayerServices.getPrayerTimes(latitude, longitude)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllahNames(): Result<AsmaAlHusnaResponse> {
        return try {
            val response = apiPrayerServices.getAllahNames()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getHadiths(page: Int, limit: Int): Result<HadithResponse> {
        return try {
            val response = apiHadithsServices.getHadiths(APIKEY, page, limit)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAzkarSabah(): Result<AzkarResponse> {
        return try {
            val response = apiAzkarServices.getMorningAzkar()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAzkarMasaa(): Result<AzkarResponse> {
        return try {
            val response = apiAzkarServices.getEveningAzkar()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAzkarPostPlayer(): Result<AzkarResponse> {
        return try {
            val response = apiAzkarServices.getAfterPrayerAzkar()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAzkarNoom(): Result<AzkarResponse> {
        return try {
            val response = apiAzkarServices.getSleepAzkar()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAzkarWake(): Result<AzkarResponse> {
        return try {
            val response = apiAzkarServices.getWakeAzkar()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAzkarMosque(): Result<AzkarResponse> {
        return try {
            val response = apiAzkarServices.getMosqueAzkar()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAzkarEating(): Result<AzkarResponse> {
        return try {
            val response = apiAzkarServices.getEatingAzkar()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAzkarMisc(): Result<AzkarResponse> {
        return try {
            val response = apiAzkarServices.getMiscAzkar()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getQuranVerses(surahNumber: Int): List<DomainAyah> {
        return try {
            val response = quranApiService.getSurahList()
            if (response.code == 200 && response.status == "OK") {
                val targetSurah = response.data.surahs.find { it.number == surahNumber }

                if (targetSurah != null) {
                    val verses = targetSurah.ayahs.map { ayah ->
                        DomainAyah(
                            hizbQuarter = ayah.hizbQuarter,
                            juz = ayah.juz,
                            manzil = ayah.manzil,
                            number = ayah.number,
                            numberInSurah = ayah.numberInSurah,
                            page = ayah.page,
                            ruku = ayah.ruku,
                            sajda = ayah.sajda,
                            text = ayah.text
                        )
                    }
                    verses
                } else {
                    emptyList()
                }
            } else {

                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getSurahList(): List<DomainSurah> {
        return try {
            val response = quranApiService.getSurahList()
            if (response.code == 200 && response.status == "OK") {
                val surahs = response.data.surahs.map { surah ->
                    DomainSurah(
                        ayahs = emptyList(),
                        englishName = surah.englishName,
                        englishNameTranslation = surah.englishNameTranslation,
                        name = surah.name,
                        number = surah.number,
                        revelationType = surah.revelationType
                    )
                }
                surahs
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getSurahAudio(
        surahNumber: Int,
        reciter: String
    ): Result<QuranResponse> {
        return try {
            val response = quranApiService.getSurahAudio(surahNumber, reciter)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAudioEditions(language: String): Result<AudioEditionsResponse> {
        return try {
            val response = quranApiService.getAudioEditions(
                format = "audio",
                language = language,
                type = "versebyverse"
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAyahAudio(reference: String, edition: String): Result<QuranAudioResponse> {
        return try {
            val response = quranApiService.getAyahAudio(reference, edition)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //articles
    override suspend fun getArticles(): Result<ArticleResponse> {
        return try {
            val response = articleApiService.getArticles()
            Result.success(response)  // response is already a List<ArticleItem>
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}