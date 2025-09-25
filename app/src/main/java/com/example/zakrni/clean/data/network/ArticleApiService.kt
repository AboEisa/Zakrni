package com.example.zakrni.clean.data.network

import com.example.zakrni.clean.data.models.ArticleItem
import retrofit2.http.GET

interface ArticleApiService {

    @GET("categories/showall/ar/json")
    suspend fun getArticles(): List<ArticleItem>
}