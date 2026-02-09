package com.zakrni.app.clean.ui.models

data class PresentationAsmaAlHusnaResponse(
    val code: Int,
    val data: List<PresentationAllahNameData>,
    val status: String
)

data class PresentationAllahNameData(
    val en: PresentationEn,
    val name: String,
    val number: Int,
    val transliteration: String
)

data class PresentationEn(
    val meaning: String
)