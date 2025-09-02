package com.example.zakrni.clean.domain

import com.example.zakrni.clean.data.models.AsmaAlHusnaResponse
import com.example.zakrni.clean.data.models.PrayerTimesResponse
import com.example.zakrni.clean.domain.models.DomainAsmaAlHusnaResponse
import com.example.zakrni.clean.domain.models.DomainAzkarResponse
import com.example.zakrni.clean.domain.models.DomainDuaResponse
import com.example.zakrni.clean.domain.models.DomainHadithResponse
import com.example.zakrni.clean.domain.models.DomainPrayerTimesResponse
import com.example.zakrni.clean.domain.models.DomainAyah
import com.example.zakrni.clean.domain.models.DomainSurah

interface IRepo {
    suspend fun getPrayerTimes(
        latitude: Double,
        longitude: Double
    ): Result<DomainPrayerTimesResponse>

    suspend fun getAllahNames(): Result<DomainAsmaAlHusnaResponse>

    suspend fun getHadiths(page: Int, limit: Int = 10): Result<DomainHadithResponse>

    suspend fun getAzkar(): Result<DomainAzkarResponse>

    suspend fun getDuas(): Result<DomainDuaResponse>

    suspend fun getQuranVerses(suraNumber: Int): List<DomainAyah>

    suspend fun getAllSurahs(): List<DomainSurah>
}