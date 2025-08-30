package com.example.zakrni.clean.data.remote

import com.example.zakrni.clean.data.models.AsmaAlHusnaResponse
import com.example.zakrni.clean.data.models.AzkarResponse
import com.example.zakrni.clean.data.models.DuaResponse
import com.example.zakrni.clean.data.models.HadithResponse
import com.example.zakrni.clean.data.models.PrayerTimesResponse
import com.example.zakrni.clean.data.models.Location

interface IRemoteDataSource {

    suspend fun getPrayerTimes(
        latitude: Double,
        longitude: Double,
        method: Int = 5
    ): Result<PrayerTimesResponse>

    suspend fun getAllahNames() : Result<AsmaAlHusnaResponse>

    suspend fun getHadiths(page: Int, limit: Int = 20): Result<HadithResponse>


    suspend fun getAzkar(): Result<AzkarResponse>

    suspend fun getDuas(): Result<DuaResponse>


}