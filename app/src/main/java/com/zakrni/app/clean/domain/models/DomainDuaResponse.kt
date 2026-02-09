package com.zakrni.app.clean.domain.models

data class DomainDuaResponse(
    val prophetic_duas: List<DomainPropheticDua>,
    val prophets_duas: List<DomainProphetsDua>,
    val quran_completion_duas: List<DomainQuranCompletionDua>,
    val quran_duas: List<DomainQuranDua>
)

data class DomainPropheticDua(
    val count: Int,
    val id: Int,
    val text: String
)

data class DomainProphetsDua(
    val count: Int,
    val id: Int,
    val text: String
)

data class DomainQuranCompletionDua(
    val count: Int,
    val id: Int,
    val text: String
)

data class DomainQuranDua(
    val count: Int,
    val id: Int,
    val text: String
)