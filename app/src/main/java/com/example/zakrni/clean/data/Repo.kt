package com.example.zakrni.clean.data

import com.example.zakrni.clean.data.local.ILocalDataSource
import com.example.zakrni.clean.data.models.AsmaAlHusnaResponse
import com.example.zakrni.clean.data.models.PrayerTimesResponse
import com.example.zakrni.clean.data.models.mapToDomain
import com.example.zakrni.clean.data.network.PrayerApiService
import com.example.zakrni.clean.data.remote.IRemoteDataSource
import com.example.zakrni.clean.domain.IRepo
import com.example.zakrni.clean.domain.models.DomainAllahNameData
import com.example.zakrni.clean.domain.models.DomainAsmaAlHusnaResponse
import com.example.zakrni.clean.domain.models.DomainAzkarResponse
import com.example.zakrni.clean.domain.models.DomainDuaResponse
import com.example.zakrni.clean.domain.models.DomainHadithResponse
import com.example.zakrni.clean.domain.models.DomainPrayerTimesResponse
import com.example.zakrni.clean.domain.models.DomainQuranVerseResponse
import com.example.zakrni.clean.domain.models.DomainSurah
import com.example.zakrni.clean.ui.utils.Constant.Companion.APIKEY
import javax.inject.Inject

class Repo @Inject constructor(
    private val remoteDataSource: IRemoteDataSource,
) : IRepo{
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
            val data = remoteDataSource.getHadiths(page,limit)
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

    override suspend fun getQuranVerses(suraNumber: Int): List<DomainQuranVerseResponse> {
        return remoteDataSource.getQuranVerses(suraNumber) // Add caching logic if needed
    }

    override suspend fun getAllSurahs(): List<DomainSurah> {
        return remoteDataSource.getAllSurahs()

    }


}