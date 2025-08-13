package com.example.zakrni.clean.data.remote

import com.example.zakrni.clean.data.models.Location
import com.example.zakrni.clean.data.models.PrayerTimesResponse
import com.example.zakrni.clean.data.network.PrayerApiService
import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val apiServices: PrayerApiService): IRemoteDataSource {
    override suspend fun fetchPrayerTimes(
        latitude: Double,
        longitude: Double,
        method: Int,
        school: Int
    ): Result<PrayerTimesResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchPrayerTimesWithAdjustments(
        latitude: Double,
        longitude: Double,
        adjustments: Map<String, Int>,
        method: Int,
        school: Int
    ): Result<PrayerTimesResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchPrayerTimesByDate(
        latitude: Double,
        longitude: Double,
        date: String,
        method: Int,
        school: Int
    ): Result<PrayerTimesResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchPrayerTimesForMonth(
        latitude: Double,
        longitude: Double,
        month: Int,
        year: Int,
        method: Int,
        school: Int
    ): Result<List<PrayerTimesResponse>> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchPrayerTimesForYear(
        latitude: Double,
        longitude: Double,
        year: Int,
        method: Int,
        school: Int
    ): Result<List<PrayerTimesResponse>> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchLocationsByCity(city: String): Result<List<Location>> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchLocationsByCountry(country: String): Result<List<Location>> {
        TODO("Not yet implemented")
    }
}