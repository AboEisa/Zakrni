// domain/models/DomainRadioResponse.kt
package com.example.zakrni.clean.domain.models

data class DomainRadioResponse(
    val code: Int,
    val status: String,
    val stations: List<DomainRadioStation>
)

data class DomainRadioStation(
    val id: Int,
    val name: String,
    val streamUrl: String,
    val description: String,
    val country: String,
    val language: String,
    val logoUrl: String?,
    val isLive: Boolean
)

// domain/models/DomainRadioResponse.kt
data class DomainRecitersResponse(
    val code: Int,
    val status: String,
    val reciters: List<DomainReciter>
)

data class DomainReciter(
    val id: Int,
    val name: String,
    val nameAr: String,
    val style: String,
    val photoUrl: String?,
    val server: String,
    val rewaya: String,
    val surahCount: Int,
    val availableSurahs: List<Int>
)