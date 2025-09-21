// domain/models/DomainAudioResponse.kt
package com.example.zakrni.clean.domain.models

data class DomainAudioResponse(
    val code: Int,
    val status: String,
    val lectures: List<DomainAudioLecture>
)

data class DomainAudioLecture(
    val id: Int,
    val title: String,
    val sheikh: String,
    val description: String,
    val audioUrl: String,
    val duration: Int,
    val seriesName: String?,
    val episodeNumber: Int?,
    val publishedDate: String,
    val playsCount: Int
)