package com.example.zakrni.clean.ui.models

data class PresentationArticleResponse(
    val articles: List<PresentationArticle>
)

data class PresentationArticle(
    val shortdescription: String?,
    val title: String,
    val id: Int
)