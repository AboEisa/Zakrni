package com.example.zakrni.clean.data.network

import com.example.zakrni.clean.data.models.AzkarResponse
import com.example.zakrni.clean.data.models.DuaResponse
import retrofit2.http.GET

interface AzkarApiService {



    @GET("azkar")
    suspend fun getAzkar(): AzkarResponse


    @GET("duas")
    suspend fun getDua(): DuaResponse

}