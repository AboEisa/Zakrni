
package com.example.zakrni.clean.data.models

data class QuranAudioResponse(
    val code: Int,
    val status: String,
    val data: QuranAudioData
)

data class QuranAudioData(
    val number: Int,
    val audio: String,
    val audioSecondary: List<String>?,
    val text: String,
    val edition: AudioEdition,
    val surah: SurahInfo,
    val numberInSurah: Int
)

data class AudioEdition(
    val identifier: String,
    val language: String,
    val name: String,
    val englishName: String,
    val type: String
)

data class SurahInfo(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String
)

data class AudioEditionsResponse(
    val code: Int,
    val status: String,
    val data: List<AudioEdition>
)