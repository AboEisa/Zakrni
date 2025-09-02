package com.example.zakrni.clean.data.network

import com.example.zakrni.clean.data.models.QuranResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface QuranApiService {

    @GET("quran/quran-uthmani")
    suspend fun getSurahList(): QuranResponse

    @GET("surah/{surah_number}")
    suspend fun getSurahDetails(@Path("surah_number") surahNumber: Int): QuranResponse

    @GET("surah/{surah_number}/editions/quran-uthmani,en.pickthall")
    suspend fun getSurahWithTranslation(@Path("surah_number") surahNumber: Int): QuranResponse
}