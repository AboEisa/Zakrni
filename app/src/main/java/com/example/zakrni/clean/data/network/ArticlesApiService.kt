// data/network/ArticlesApiService.kt
package com.example.zakrni.clean.data.network

import com.example.zakrni.clean.data.models.ArticlesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ArticlesApiService {
    @GET("articles")
    suspend fun getArticles(
        @Query("page") page: Int = 1,
        @Query("category") category: String? = null
    ): ArticlesResponse
}