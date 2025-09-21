// data/models/RadioResponse.kt
package com.example.zakrni.clean.data.models

data class RadioResponse(
    val code: Int,
    val status: String,
    val data: RadioData
)

data class RadioData(
    val stations: List<RadioStation>
)

data class RadioStation(
    val id: Int,
    val name: String,
    val url: String,
    val description: String,
    val country: String,
    val language: String,
    val logo_url: String?,
    val is_live: Boolean
)

// data/models/RecitersResponse.kt
data class RecitersResponse(
    val code: Int,
    val status: String,
    val data: RecitersData
)

data class RecitersData(
    val reciters: List<Reciter>
)

data class Reciter(
    val id: Int,
    val name: String,
    val name_ar: String,
    val style: String,
    val photo_url: String?,
    val server: String,
    val rewaya: String,
    val count: Int,
    val suras_list: List<Int>
)