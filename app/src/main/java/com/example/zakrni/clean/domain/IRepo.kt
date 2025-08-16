package com.example.zakrni.clean.domain

import com.example.zakrni.clean.domain.models.DomainPrayerTimesResponse

interface IRepo {
    suspend fun getPrayerTimes(
        latitude: Double,
        longitude: Double
    ): Result<DomainPrayerTimesResponse>



}