package com.example.zakrni.clean.domain.models

typealias DomainArticleResponse = List<DomainArticle>

data class DomainArticle(
    val apiurl: String,
    val id: Int,
    val shortdescription: String?,
    val title: String
)