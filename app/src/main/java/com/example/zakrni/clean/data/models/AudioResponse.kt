// data/models/AudioResponse.kt
package com.example.zakrni.clean.data.models

data class AudioResponse(
    val code: Int,
    val status: String,
    val data: AudioData
)

data class AudioData(
    val lectures: List<AudioLecture>,
    val total: Int
)

data class AudioLecture(
    val id: Int,
    val title: String,
    val sheikh: String,
    val description: String,
    val audio_url: String,
    val duration: Int,
    val series_name: String?,
    val episode_number: Int?,
    val published_date: String,
    val plays_count: Int
)