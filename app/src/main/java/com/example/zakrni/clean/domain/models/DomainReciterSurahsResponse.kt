// domain/models/DomainReciterSurahsResponse.kt
package com.example.zakrni.clean.domain.models

data class DomainReciterSurahsResponse(
    val reciterInfo: DomainReciterInfo,
    val surahs: List<DomainReciterSurah>
)

data class DomainReciterInfo(
    val id: Int,
    val name: String,
    val server: String
)

data class DomainReciterSurah(
    val number: Int,
    val name: String,
    val audioUrl: String
)