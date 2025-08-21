package com.example.zakrni.clean.domain

import com.example.zakrni.clean.domain.models.DomainHadithResponse
import com.example.zakrni.clean.domain.models.DomainPrayerTimesResponse

interface IRepo {
    suspend fun getPrayerTimes(
        latitude: Double,
        longitude: Double
    ): Result<DomainPrayerTimesResponse>


    suspend fun getHadiths(page: Int, limit: Int = 10): Result<DomainHadithResponse>


}