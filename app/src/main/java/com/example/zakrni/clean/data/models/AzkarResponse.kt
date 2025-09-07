package com.example.zakrni.clean.data.models

data class AzkarResponse(
    val content: List<Content>,
    val title: String
)

data class Content(
    val bless: String,
    val repeat: Int,
    val zekr: String
)