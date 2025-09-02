package com.example.zakrni.clean.data.remote

import com.example.zakrni.clean.data.models.AsmaAlHusnaResponse
import com.example.zakrni.clean.data.models.AzkarResponse
import com.example.zakrni.clean.data.models.DuaResponse
import com.example.zakrni.clean.data.models.HadithResponse
import com.example.zakrni.clean.data.models.Location
import com.example.zakrni.clean.data.models.PrayerTimesResponse
import com.example.zakrni.clean.data.network.AzkarApiService
import com.example.zakrni.clean.data.network.HadithApiService
import com.example.zakrni.clean.data.network.PrayerApiService
import com.example.zakrni.clean.data.network.QuranApiService
import com.example.zakrni.clean.domain.models.DomainQuranVerseResponse
import com.example.zakrni.clean.domain.models.DomainSurah
import com.example.zakrni.clean.ui.utils.Constant.Companion.APIKEY
import javax.inject.Inject
import kotlin.collections.emptyList

class RemoteDataSource @Inject constructor(private val apiPrayerServices: PrayerApiService ,private val apiHadithsServices: HadithApiService,private val apiAzkarServices: AzkarApiService ,private val quranApiService: QuranApiService): IRemoteDataSource {
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

    override suspend fun getAzkar(): Result<AzkarResponse> {
        return try {
            val response = apiAzkarServices.getAzkar()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDuas(): Result<DuaResponse> {
        return try {
           val response = apiAzkarServices.getDua()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



        override suspend fun getQuranVerses(surahNumber: Int): List<DomainQuranVerseResponse> {
            val response = quranApiService.getSurahDetails(surahNumber)
            return if (response.code == 200 && response.status == "OK") {
                response.data.ayahs.map { verse ->
                    val editionMap = mapOf(
                        "quran-uthmani" to verse.text,
                        "en.pickthall" to (response.data.ayahs.find { it.number == verse.number }?.editions?.get("en.pickthall") ?: ""),
                        "ar.alafasy" to (response.data.ayahs.find { it.number == verse.number }?.editions?.get("ar.alafasy") ?: "")
                    )
                    DomainQuranVerseResponse(
                        number = verse.number,
                        text = verse.text,
                        numberInSurah = verse.numberInSurah ?: (verse.number % 100), // Fallback logic
                        juz = verse.juz ?: 1,
                        manzil = verse.manzil ?: 1,
                        page = verse.page ?: 1,
                        ruku = verse.ruku ?: 1,
                        hizbQuarter = verse.hizbQuarter ?: 1,
                        sajda = verse.sajda ?: false,
                        editions = editionMap.filterValues { it.isNotEmpty() },
                        audioUrl = editionMap["ar.alafasy"] // Audio URL from ar.alafasy
                    )
                }
            } else {
                emptyList()
            }
        }

        override suspend fun getSurahList(): List<DomainSurah> {
            val response = quranApiService.getSurahList()
            return if (response.code == 200 && response.status == "OK") {
                response.data
            } else {
                emptyList()
            }
        }
    }