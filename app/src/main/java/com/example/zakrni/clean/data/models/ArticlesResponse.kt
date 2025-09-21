// data/models/ArticlesResponse.kt
package com.example.zakrni.clean.data.models

data class ArticlesResponse(
    val code: Int,
    val status: String,
    val data: ArticlesData
)

data class ArticlesData(
    val articles: List<Article>,
    val total: Int,
    val current_page: Int,
    val last_page: Int
)

data class Article(
    val id: Int,
    val title: String,
    val content: String,
    val author: String,
    val category: String,
    val image_url: String?,
    val published_date: String,
    val views_count: Int,
    val reading_time: Int
)