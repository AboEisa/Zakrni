package com.zakrni.app.clean.domain.models

data class DomainQuranResponse(
    val code: Int,
    val data: DomainQuranData,
    val status: String
)

data class DomainQuranData(
    val edition: DomainEdition,
    val surahs: List<DomainSurah>
)

data class DomainEdition(
    val englishName: String,
    val format: String,
    val identifier: String,
    val language: String,
    val name: String,
    val type: String
)

data class DomainSurah(
    val ayahs: List<DomainAyah>,
    val englishName: String,
    val englishNameTranslation: String,
    val name: String,
    val number: Int,
    val revelationType: String
)


data class DomainAyah(
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