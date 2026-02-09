package com.zakrni.app.clean.data.models

data class AsmaAlHusnaResponse(
    val code: Int,
    val `data`: List<AllahNameData>,
    val status: String
)

data class AllahNameData(
    val en: En,
    val name: String,
    val number: Int,
    val transliteration: String
)

data class En(
    val meaning: String
)