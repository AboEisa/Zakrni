package com.zakrni.app.clean.data.network

import com.zakrni.app.clean.data.models.AudioEditionsResponse
import com.zakrni.app.clean.data.models.QuranAudioResponse
import com.zakrni.app.clean.data.models.QuranResponse
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

    // Get single ayah with translation
    @GET("ayah/{surah_number}:{ayah_number}/editions/quran-uthmani,en.pickthall")
    suspend fun getAyahWithTranslation(
        @Path("surah_number") surahNumber: Int,
        @Path("ayah_number") ayahNumber: Int
    ): QuranResponse

    // FIXED: Proper audio endpoints
    @GET("ayah/{reference}/{edition}")
    suspend fun getAyahAudio(
        @Path("reference") reference: String,
        @Path("edition") edition: String = "ar.alafasy"
    ): QuranAudioResponse

    @GET("edition")
    suspend fun getAudioEditions(
        @Query("format") format: String = "audio",
        @Query("language") language: String = "ar",
        @Query("type") type: String = "versebyverse"
    ): AudioEditionsResponse

    // FIXED: This should return audio metadata, not actual audio files
    @GET("surah/{surah_number}/{edition}")
    suspend fun getSurahAudio(
        @Path("surah_number") surahNumber: Int,
        @Path("edition") edition: String = "ar.alafasy"
    ): QuranResponse
}