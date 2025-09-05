package com.example.zakrni.clean.data.network

import com.example.zakrni.clean.data.models.AudioEditionsResponse
import com.example.zakrni.clean.data.models.QuranAudioResponse
import com.example.zakrni.clean.data.models.QuranResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface QuranApiService {

    @GET("quran/quran-uthmani")
    suspend fun getSurahList(): QuranResponse

    @GET("surah/{surah_number}")
    suspend fun getSurahDetails(@Path("surah_number") surahNumber: Int): QuranResponse

    @GET("surah/{surah_number}/editions/quran-uthmani,en.pickthall")
    suspend fun getSurahWithTranslation(@Path("surah_number") surahNumber: Int): QuranResponse

    @GET("ayah/{reference}/{edition}")
    suspend fun getAyahAudio(
        @Path("reference") reference: String, // e.g., "262" or "2:255"
        @Path("edition") edition: String = "ar.abdulsamad"
    ): QuranAudioResponse

    @GET("edition")
    suspend fun getAudioEditions(
        @Query("format") format: String = "audio",
        @Query("language") language: String = "ar",
        @Query("type") type: String = "versebyverse"
    ): AudioEditionsResponse

    @GET("surah/{surah_number}/{edition}")
    suspend fun getSurahAudio(
        @Path("surah_number") surahNumber: Int,
        @Path("edition") edition: String = "ar.abdulsamad"
    ): QuranResponse
}