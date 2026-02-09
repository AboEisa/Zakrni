package com.zakrni.app.clean.domain.models

data class DomainAsmaAlHusnaResponse(
    val code: Int,
    val data: List<DomainAllahNameData>,
    val status: String
)

data class DomainAllahNameData(
    val en: DomainEn,
    val name: String,
    val number: Int,
    val transliteration: String
)

data class DomainEn(
    val meaning: String
)