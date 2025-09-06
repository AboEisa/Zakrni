package com.example.zakrni.clean.data.models

data class HadithResponse(
    val hadiths: Hadiths,
    val message: String,
    val status: Int
)

data class Hadiths(
    val current_page: Int,
    val `data`: List<Dataa>,
    val first_page_url: String?,
    val from: Int?,
    val last_page: Int,
    val last_page_url: String?,
    val links: List<Link>?,
    val next_page_url: String?,
    val path: String?,
    val per_page: Int?,
    val prev_page_url: String?,
    val to: Int?,
    val total: Int
)

data class Dataa(
    val book: Book?,
    val bookSlug: String?,
    val chapter: Chapter?,
    val chapterId: String?,
    val englishNarrator: String?,
    val hadithArabic: String?,
    val hadithEnglish: String?,
    val hadithNumber: String?,
    val hadithUrdu: String?,
    val headingArabic: String?,
    val headingEnglish: String?,
    val headingUrdu: String?,
    val id: Int,
    val status: String?,
    val urduNarrator: String?,
    val volume: String?
)

data class Link(
    val active: Boolean,
    val label: String?,
    val url: String?
)

data class Book(
    val aboutWriter: String?,
    val bookName: String?,
    val bookSlug: String?,
    val id: Int,
    val writerDeath: String?,
    val writerName: String?
)

data class Chapter(
    val bookSlug: String?,
    val chapterArabic: String?,
    val chapterEnglish: String?,
    val chapterNumber: String?,
    val chapterUrdu: String?,
    val id: Int
)