// data/models/VideosResponse.kt
package com.example.zakrni.clean.data.models

data class VideosResponse(
    val code: Int,
    val status: String,
    val data: VideosData
)

data class VideosData(
    val videos: List<Video>,
    val total: Int,
    val current_page: Int
)

data class Video(
    val id: Int,
    val title: String,
    val description: String,
    val video_url: String,
    val thumbnail_url: String,
    val duration: String,
    val views: Int,
    val channel_name: String,
    val published_date: String,
    val category: String
)