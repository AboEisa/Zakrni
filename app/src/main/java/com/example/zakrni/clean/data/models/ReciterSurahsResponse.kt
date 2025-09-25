// data/models/ReciterSurahsResponse.kt
package com.example.zakrni.clean.data.models

data class ReciterSurahsResponse(
    val code: Int,
    val status: String,
    val data: ReciterSurahsData
)

data class ReciterSurahsData(
    val reciter: ReciterInfo,
    val surahs: List<ReciterSurah>
)

data class ReciterInfo(
    val id: Int,
    val name: String,
    val server: String
)

data class ReciterSurah(
    val number: Int,
    val name: String,
    val audio_url: String
)