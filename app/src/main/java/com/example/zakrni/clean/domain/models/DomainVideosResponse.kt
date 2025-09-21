// domain/models/DomainVideosResponse.kt
package com.example.zakrni.clean.domain.models

data class DomainVideosResponse(
    val code: Int,
    val status: String,
    val videos: List<DomainVideo>,
    val total: Int
)

data class DomainVideo(
    val id: Int,
    val title: String,
    val description: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val duration: String,
    val views: Int,
    val channelName: String,
    val publishedDate: String,
    val category: String
)