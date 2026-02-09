package com.zakrni.app.clean.data.network

import com.zakrni.app.clean.data.models.ArticleItem
import retrofit2.http.GET

interface ArticleApiService {

    @GET("categories/showall/ar/json")
    suspend fun getArticles(): List<ArticleItem>
}