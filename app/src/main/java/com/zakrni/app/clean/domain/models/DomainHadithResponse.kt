package com.zakrni.app.clean.domain.models

data class DomainHadithResponse(
    val status: Int,
    val message: String,
    val hadiths: DomainHadiths
)

data class DomainHadiths(
    val currentPage: Int,
    val data: List<DomainHadith>,
    val total: Int,
    val lastPage: Int,
    val nextPageUrl: String?,
    val prevPageUrl: String?
)

data class DomainHadith(
    val id: Int,
    val book: DomainBook?,
    val chapter: DomainChapter?,
    val bookSlug: String?,
    val chapterId: String?,
    val hadithArabic: String?,
    val hadithEnglish: String?,
    val hadithUrdu: String?,
    val englishNarrator: String?,
    val urduNarrator: String?,
    val status: String?,
    val volume: String?,
    val headingArabic: String?, // Keep nullable
    val headingEnglish: String?,
    val headingUrdu: String?,
    val hadithNumber: String?
)

data class DomainBook(
    val id: Int,
    val bookName: String?,
    val bookSlug: String?,
    val writerName: String?,
    val writerDeath: String?,
    val aboutWriter: String?
)

data class DomainChapter(
    val id: Int,
    val bookSlug: String?,
    val chapterArabic: String?,
    val chapterEnglish: String?,
    val chapterUrdu: String?,
    val chapterNumber: String?
)

data class DomainLink(
    val label: String?,
    val url: String?,
    val active: Boolean
)