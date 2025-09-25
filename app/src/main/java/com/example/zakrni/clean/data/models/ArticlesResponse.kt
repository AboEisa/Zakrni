package com.example.zakrni.clean.data.models

typealias ArticleResponse = List<ArticleItem>

data class ArticleItem(
    val apiurl: String,
    val id: Int,
    val source_id: Int,  // Added this field from API response
    val shortdescription: String?,
    val title: String
)