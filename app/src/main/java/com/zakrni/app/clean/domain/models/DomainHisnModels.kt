package com.zakrni.app.clean.domain.models

/**
 * Domain models for Hisn (حصن المسلم) Duas API
 */

data class DomainHisnSection(
    val name: String,
    val count: Int,
    val englishName: String? = null
)

data class DomainHisnSectionsResponse(
    val sections: List<DomainHisnSection>
)

data class DomainHisnDua(
    val id: Int,
    val section: String,
    val arabic: String,
    val transliteration: String?,
    val translation: String?,
    val reference: String?,
    val count: Int,
    val sectionEnglish: String? = null
)

data class DomainHisnDuasResponse(
    val duas: List<DomainHisnDua>,
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
)
