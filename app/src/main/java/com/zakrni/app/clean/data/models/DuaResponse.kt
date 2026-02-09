package com.zakrni.app.clean.data.models

data class DuaResponse(
    val prophetic_duas: List<PropheticDua>,
    val prophets_duas: List<ProphetsDua>,
    val quran_completion_duas: List<QuranCompletionDua>,
    val quran_duas: List<QuranDua>
)

data class PropheticDua(
    val count: Int,
    val id: Int,
    val text: String
)

data class ProphetsDua(
    val count: Int,
    val id: Int,
    val text: String
)

data class QuranCompletionDua(
    val count: Int,
    val id: Int,
    val text: String
)

data class QuranDua(
    val count: Int,
    val id: Int,
    val text: String
)