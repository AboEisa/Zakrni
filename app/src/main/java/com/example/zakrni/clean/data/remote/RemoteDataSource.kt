package com.example.zakrni.clean.data.remote

import com.example.zakrni.clean.data.models.Location
import com.example.zakrni.clean.data.models.PrayerTimesResponse
import com.example.zakrni.clean.data.network.PrayerApiService
import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val apiServices: PrayerApiService): IRemoteDataSource {
    override suspend fun getPrayerTimes(
        latitude: Double,
        longitude: Double,
        method: Int
    ): Result<PrayerTimesResponse> {
        return try {
            val response = apiServices.getPrayerTimes(latitude, longitude)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}