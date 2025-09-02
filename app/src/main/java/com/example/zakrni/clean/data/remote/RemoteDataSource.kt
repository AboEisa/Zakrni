package com.example.zakrni.clean.data.remote

import com.example.zakrni.clean.data.models.AsmaAlHusnaResponse
import com.example.zakrni.clean.data.models.AzkarResponse
import com.example.zakrni.clean.data.models.DuaResponse
import com.example.zakrni.clean.data.models.HadithResponse
import com.example.zakrni.clean.data.models.PrayerTimesResponse
import com.example.zakrni.clean.data.network.AzkarApiService
import com.example.zakrni.clean.data.network.HadithApiService
import com.example.zakrni.clean.data.network.PrayerApiService
import com.example.zakrni.clean.data.network.QuranApiService
import com.example.zakrni.clean.domain.models.DomainAyah
import com.example.zakrni.clean.ui.utils.Constant.Companion.APIKEY
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val apiPrayerServices: PrayerApiService,
    private val apiHadithsServices: HadithApiService,
    private val apiAzkarServices: AzkarApiService,
    private val quranApiService: QuranApiService
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

    override suspend fun getQuranVerses(surahNumber: Int): List<DomainAyah> {
        return try {
            val response = quranApiService.getSurahDetails(surahNumber)
            if (response.code == 200 && response.status == "OK") {
                // Find the specific surah from the response
                val surah = response.data.surahs.find { it.number == surahNumber }
                surah?.ayahs?.map { ayah ->
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
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getSurahList(): List<com.example.zakrni.clean.domain.models.DomainSurah> {
        return try {
            val response = quranApiService.getSurahList()
            if (response.code == 200 && response.status == "OK") {
                response.data.surahs.map { surah ->
                    com.example.zakrni.clean.domain.models.DomainSurah(
                        ayahs = emptyList(), // Will be populated when needed
                        englishName = surah.englishName,
                        englishNameTranslation = surah.englishNameTranslation,
                        name = surah.name,
                        number = surah.number,
                        revelationType = surah.revelationType
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}