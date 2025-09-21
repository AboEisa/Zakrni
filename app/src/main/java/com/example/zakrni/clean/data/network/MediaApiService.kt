// data/network/MediaApiService.kt
package com.example.zakrni.clean.data.network

import com.example.zakrni.clean.data.models.*
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

interface MediaApiService {

    @GET("api/articles")
    suspend fun getArticles(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("category") category: String? = null
    ): ArticlesResponse

    @GET("api/audio/lectures")
    suspend fun getAudioLectures(
        @Query("page") page: Int = 1,
        @Query("sheikh") sheikh: String? = null
    ): RadioResponse

    @GET("api/videos")
    suspend fun getVideos(
        @Query("page") page: Int = 1,
        @Query("category") category: String? = null,
        @Query("search") query: String? = null
    ): VideosResponse

    @GET("api/radio/stations")
    suspend fun getRadioStations(
        @Query("country") country: String? = null,
        @Query("language") language: String? = null
    ): RadioResponse

    @GET("api/reciters")
    suspend fun getReciters(
        @Query("rewaya") rewaya: String? = null
    ): RecitersResponse

    @GET("api/reciter/{id}/surahs")
    suspend fun getReciterSurahs(
        @Path("id") reciterId: Int
    ): ReciterSurahsResponse
}