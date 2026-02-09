package com.zakrni.app.clean.ui.models

data class PresentationHadithResponse(
    val status: Int,
    val message: String,
    val hadiths: PresentationHadiths
)

data class PresentationHadiths(
    val currentPage: Int,
    val data: List<PresentationHadith>,
    val total: Int,
    val lastPage: Int,
    val nextPageUrl: String?,
    val prevPageUrl: String?
)

data class PresentationHadith(
    val id: Int,
    val book: PresentationBook?,
    val chapter: PresentationChapter?,
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

data class PresentationBook(
    val id: Int,
    val bookName: String?,
    val bookSlug: String?,
    val writerName: String?,
    val writerDeath: String?,
    val aboutWriter: String?
)

data class PresentationChapter(
    val id: Int,
    val bookSlug: String?,
    val chapterArabic: String?,
    val chapterEnglish: String?,
    val chapterUrdu: String?,
    val chapterNumber: String?
)

data class PresentationLink(
    val label: String?,
    val url: String?,
    val active: Boolean
)