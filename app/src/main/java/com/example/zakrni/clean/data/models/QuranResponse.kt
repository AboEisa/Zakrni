package com.example.zakrni.clean.data.models

data class QuranResponse(
    val code: Int,
    val `data`: QuranData, // Renamed from Data to QuranData
    val status: String
)

data class QuranData( // Renamed from Data
    val edition: Edition,
    val surahs: List<Surah>
)

data class Edition(
    val englishName: String,
    val format: String,
    val identifier: String,
    val language: String,
    val name: String,
    val type: String
)

data class Surah(
    val ayahs: List<Ayah>,
    val englishName: String,
    val englishNameTranslation: String,
    val name: String,
    val number: Int,
    val revelationType: String
)

data class Ayah(
    val hizbQuarter: Int,
    val juz: Int,
    val manzil: Int,
    val number: Int,
    val numberInSurah: Int,
    val page: Int,
    val ruku: Int,
    val sajda: Any?,
    val text: String
)