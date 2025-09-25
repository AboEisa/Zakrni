// domain/models/DomainArticlesResponse.kt
package com.example.zakrni.clean.domain.models

data class DomainArticlesResponse(
    val code: Int,
    val status: String,
    val articles: List<DomainArticle>,
    val pagination: DomainPagination
)

data class DomainArticle(
    val id: Int,
    val title: String,
    val content: String,
    val author: String,
    val category: String,
    val imageUrl: String?,
    val publishedDate: String,
    val viewsCount: Int,
    val readingTime: Int
)

data class DomainPagination(
    val total: Int,
    val currentPage: Int,
    val lastPage: Int
)