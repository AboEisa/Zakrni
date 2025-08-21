package com.example.zakrni.clean.ui.models

data class PresentationHadith(
    val id: String?,
    val book: PresentationBook?,
    val chapter: PresentationChapter?,
    val hadithNumber: String?,
    val hadithArabic: String?,
    val hadithEnglish: String?,
    val hadithUrdu: String?,
    val urduNarrator: String?,
    val englishNarrator: String?,
    val headingArabic: String?,
    val headingUrdu: String?,
    val headingEnglish: String?,
    val status: String?
)

data class PresentationBook(
    val bookName: String?,
    val bookNameArabic: String?,
    val bookNameUrdu: String?,
    val bookId: String?
)

data class PresentationChapter(
    val chapterNumber: String?,
    val chapterName: String?,
    val chapterNameArabic: String?,
    val chapterNameUrdu: String?,
    val chapterId: String?
)