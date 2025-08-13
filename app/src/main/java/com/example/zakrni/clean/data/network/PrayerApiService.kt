package com.example.zakrni.clean.data.network

import com.example.zakrni.clean.data.models.PrayerTimesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PrayerApiService {

        @GET("v1/timings")
        suspend fun getPrayerTimes(
            @Query("latitude") latitude: Double,
            @Query("longitude") longitude: Double,
            @Query("method") method: Int = 5
        ): PrayerTimesResponse

}