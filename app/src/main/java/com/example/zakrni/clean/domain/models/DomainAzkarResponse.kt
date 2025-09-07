package com.example.zakrni.clean.domain.models

data class DomainAzkarResponse(
    val content: List<DomainContent>,
    val title: String
)

data class DomainContent(
    val bless: String,
    val repeat: Int,
    val zekr: String
)