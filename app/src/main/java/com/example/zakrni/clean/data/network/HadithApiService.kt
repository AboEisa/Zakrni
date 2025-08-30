package com.example.zakrni.clean.data.network

import com.example.zakrni.clean.data.models.HadithResponse
import com.example.zakrni.clean.data.models.PrayerTimesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface HadithApiService {

    @GET("api/hadiths/")
    suspend fun getHadiths(
        @Query("apiKey") apiKey: String,
        @Query("page") page: Int? = 2,
        @Query("limit") limit: Int = 20
    ): HadithResponse

}