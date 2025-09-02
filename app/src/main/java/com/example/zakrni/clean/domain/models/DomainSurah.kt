package com.example.zakrni.clean.domain.models

data class DomainSurah(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val revelationType: String,
    val numberOfAyahs: Int,
    val ayahs: List<DomainQuranVerseResponse>? = null
)