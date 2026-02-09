
package com.zakrni.app.clean.domain.models

data class DomainQuranAudioResponse(
    val code: Int,
    val status: String,
    val data: DomainQuranAudioData
)

data class DomainQuranAudioData(
    val number: Int,
    val audio: String,
    val audioSecondary: List<String>?,
    val text: String,
    val edition: DomainAudioEdition,
    val surah: DomainSurahInfo,
    val numberInSurah: Int
)

data class DomainAudioEdition(
    val identifier: String,
    val language: String,
    val name: String,
    val englishName: String,
    val type: String
)

data class DomainSurahInfo(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String
)

data class DomainAudioEditionsResponse(
    val code: Int,
    val status: String,
    val data: List<DomainAudioEdition>
)