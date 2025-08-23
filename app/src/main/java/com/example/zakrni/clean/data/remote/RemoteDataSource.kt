package com.example.zakrni.clean.data.remote

import com.example.zakrni.clean.data.models.AsmaAlHusnaResponse
import com.example.zakrni.clean.data.models.HadithResponse
import com.example.zakrni.clean.data.models.Location
import com.example.zakrni.clean.data.models.PrayerTimesResponse
import com.example.zakrni.clean.data.network.HadithApiService
import com.example.zakrni.clean.data.network.PrayerApiService
import com.example.zakrni.clean.ui.utils.Constant.Companion.APIKEY
import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val apiPrayerServices: PrayerApiService ,private val apiHadithsServices: HadithApiService): IRemoteDataSource {
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


}