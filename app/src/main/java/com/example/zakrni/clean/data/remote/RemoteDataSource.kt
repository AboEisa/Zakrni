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
import com.example.zakrni.clean.domain.models.DomainSurah
import com.example.zakrni.clean.ui.utils.Constant.Companion.APIKEY
import javax.inject.Inject
import kotlin.collections.emptyList

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
            println("DEBUG: RemoteDataSource - Fetching verses for surah $surahNumber")

            // Use the getSurahDetails endpoint which returns verses for a specific surah
            val response = quranApiService.getSurahDetails(surahNumber)

            println("DEBUG: RemoteDataSource - Response code: ${response.code}, status: ${response.status}")

            if (response.code == 200 && response.status == "OK") {
                // The API returns the full Quran, so we need to find the specific surah
                val surah = response.data.surahs.find { it.number == surahNumber }
                val verses = surah?.ayahs?.map { ayah ->
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

                println("DEBUG: RemoteDataSource - Mapped ${verses.size} verses")
                verses
            } else {
                println("DEBUG: RemoteDataSource - Invalid response")
                emptyList()
            }
        } catch (e: Exception) {
            println("DEBUG: RemoteDataSource - Error fetching verses: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    // RemoteDataSource.kt
    override suspend fun getSurahList(): List<DomainSurah> {
        return try {
            println("DEBUG: RemoteDataSource - Fetching surah list from API")
            val response = quranApiService.getSurahList() // This gets the full Quran
            println("DEBUG: RemoteDataSource - API Response: code=${response.code}, status=${response.status}")

            if (response.code == 200 && response.status == "OK") {
                val surahs = response.data.surahs.map { surah ->
                    DomainSurah(
                        ayahs = emptyList(), // Don't load verses yet
                        englishName = surah.englishName,
                        englishNameTranslation = surah.englishNameTranslation,
                        name = surah.name,
                        number = surah.number,
                        revelationType = surah.revelationType
                    )
                }
                println("DEBUG: RemoteDataSource - Mapped ${surahs.size} surahs")
                surahs
            } else {
                println("DEBUG: RemoteDataSource - Invalid response: ${response.status}")
                emptyList()
            }
        } catch (e: Exception) {
            println("DEBUG: RemoteDataSource - Exception fetching surahs: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
}