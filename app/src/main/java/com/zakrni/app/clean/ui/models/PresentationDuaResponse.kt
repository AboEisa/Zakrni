package com.zakrni.app.clean.ui.models

data class PresentationDuaResponse(
    val prophetic_duas: List<PresentationPropheticDua>,
    val prophets_duas: List<PresentationProphetsDua>,
    val quran_completion_duas: List<PresentationQuranCompletionDua>,
    val quran_duas: List<PresentationQuranDua>
)

data class PresentationPropheticDua(
    val count: Int,
    val id: Int,
    val text: String
)

data class PresentationProphetsDua(
    val count: Int,
    val id: Int,
    val text: String
)

data class PresentationQuranCompletionDua(
    val count: Int,
    val id: Int,
    val text: String
)

data class PresentationQuranDua(
    val count: Int,
    val id: Int,
    val text: String
)