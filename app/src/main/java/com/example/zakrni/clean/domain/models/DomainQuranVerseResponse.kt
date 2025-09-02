package com.example.zakrni.clean.domain.models

data class DomainQuranVerseResponse(
    val number: Int,
    val text: String,
    val numberInSurah: Int,
    val juz: Int,
    val manzil: Int,
    val page: Int,
    val ruku: Int,
    val hizbQuarter: Int,
    val sajda: Boolean,
    val editions: Map<String, String>? = null, // e.g., "quran-uthmani" -> Arabic, "en.pickthall" -> translation, "ar.alafasy" -> audio URL
    val audioUrl: String? = null // Direct audio URL from ar.alafasy
)