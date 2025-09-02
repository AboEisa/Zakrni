package com.example.zakrni.clean.data.network

import com.example.zakrni.clean.domain.models.SurahListResponse
import com.example.zakrni.clean.domain.models.SurahResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface QuranApiService {
    @GET("surah")
    suspend fun getSurahList(): SurahListResponse

    @GET("surah/{surah_number}/editions/quran-uthmani,en.pickthall,ar.alafasy")
    suspend fun getSurahDetails(@Path("surah_number") surahNumber: Int): SurahResponse
}