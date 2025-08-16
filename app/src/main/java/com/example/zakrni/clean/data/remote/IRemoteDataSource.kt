package com.example.zakrni.clean.data.remote

import com.example.zakrni.clean.data.models.PrayerTimesResponse
import com.example.zakrni.clean.data.models.Location

interface IRemoteDataSource {

    suspend fun getPrayerTimes(
        latitude: Double,
        longitude: Double,
        method: Int = 5
    ): Result<PrayerTimesResponse>

}