// ui/models/PresentationAudioResponse.kt
package com.example.zakrni.clean.ui.models

data class PresentationAudio(
    val id: Int,
    val title: String,
    val sheikh: String,
    val description: String,
    val audioUrl: String,
    val duration: String,
    val seriesName: String?,
    val episodeNumber: Int?,
    val publishedDate: String,
    val playsCount: String,
    val progress: Int = 0,
    val isPlaying: Boolean = false
)

data class PresentationReciterSurah(
    val number: Int,
    val name: String,
    val audioUrl: String,
    val reciterName: String
)