package com.example.zakrni.clean.domain.models

data class SurahListResponse(
    val code: Int,
    val status: String,
    val data: List<DomainSurah>
)

data class SurahResponse(
    val code: Int,
    val status: String,
    val data: SurahData
)

data class SurahData(
    val number: Int,
    val ayahs: List<DomainQuranVerseResponse>
)