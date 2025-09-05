
package com.example.zakrni.clean.ui.models

data class PresentationQuranAudioResponse(
    val code: Int,
    val status: String,
    val data: PresentationQuranAudioData
)

data class PresentationQuranAudioData(
    val number: Int,
    val audio: String,
    val audioSecondary: List<String>?,
    val text: String,
    val edition: PresentationAudioEdition,
    val surah: PresentationSurahInfo,
    val numberInSurah: Int
)

data class PresentationAudioEdition(
    val identifier: String,
    val language: String,
    val name: String,
    val englishName: String,
    val type: String
)

data class PresentationSurahInfo(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String
)

data class PresentationAudioEditionsResponse(
    val code: Int,
    val status: String,
    val data: List<PresentationAudioEdition>
)