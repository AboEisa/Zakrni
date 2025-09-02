package com.example.zakrni.clean.ui.models

data class PresentationQuranResponse(
    val code: Int,
    val `data`: PresentationQuranData, // Fixed reference
    val status: String
)

data class PresentationQuranData( // Renamed from PresentationData
    val edition: PresentationEdition,
    val surahs: List<PresentationSurah>
)

data class PresentationEdition(
    val englishName: String,
    val format: String,
    val identifier: String,
    val language: String,
    val name: String,
    val type: String
)

data class PresentationSurah(
    val ayahs: List<PresentationAyah>,
    val englishName: String,
    val englishNameTranslation: String,
    val name: String,
    val number: Int,
    val revelationType: String
)

data class PresentationAyah(
    val hizbQuarter: Int,
    val juz: Int,
    val manzil: Int,
    val number: Int,
    val numberInSurah: Int,
    val page: Int,
    val ruku: Int,
    val sajda: Boolean,
    val text: String
)