package com.example.zakrni.clean.data.remote

import com.example.zakrni.clean.data.models.PrayerTimesResponse
import com.example.zakrni.clean.data.models.Location

interface IRemoteDataSource {

    // Prayer Times Methods
    suspend fun fetchPrayerTimes(
        latitude: Double,
        longitude: Double,
        method: Int = 5, // Default to Egyptian General Authority of Survey
        school: Int = 0  // Default to Shafi
    ): Result<PrayerTimesResponse>

    // Prayer Times Adjustments
    suspend fun fetchPrayerTimesWithAdjustments(
        latitude: Double,
        longitude: Double,
        adjustments: Map<String, Int>, // Prayer name to minutes adjustment
        method: Int = 5,
        school: Int = 0
    ): Result<PrayerTimesResponse>

    suspend fun fetchPrayerTimesByDate(
        latitude: Double,
        longitude: Double,
        date: String, // Format: DD-MM-YYYY
        method: Int = 5,
        school: Int = 0
    ): Result<PrayerTimesResponse>

    suspend fun fetchPrayerTimesForMonth(
        latitude: Double,
        longitude: Double,
        month: Int,
        year: Int,
        method: Int = 5,
        school: Int = 0
    ): Result<List<PrayerTimesResponse>>

    suspend fun fetchPrayerTimesForYear(
        latitude: Double,
        longitude: Double,
        year: Int,
        method: Int = 5, // Default to Egyptian General Authority of Survey
        school: Int = 0
    ): Result<List<PrayerTimesResponse>>

    // Location Methods
    suspend fun fetchLocationsByCity(city: String): Result<List<Location>>

    suspend fun fetchLocationsByCountry(country: String): Result<List<Location>>
}