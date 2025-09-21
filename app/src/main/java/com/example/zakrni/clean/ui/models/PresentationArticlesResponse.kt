// ui/models/PresentationArticlesResponse.kt
package com.example.zakrni.clean.ui.models

data class PresentationArticle(
    val id: Int,
    val title: String,
    val content: String,
    val author: String,
    val category: String,
    val imageUrl: String?,
    val publishedDate: String,
    val viewsCount: String,
    val readingTime: String,
    val formattedDate: String
)