package com.example.zakrni.clean.data.network

import com.example.zakrni.clean.data.models.AzkarResponse
import retrofit2.http.GET

interface AzkarApiService {



    @GET("azkar")
    suspend fun getAzkar(): AzkarResponse

}