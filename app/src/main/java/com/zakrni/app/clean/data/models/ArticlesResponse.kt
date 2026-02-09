package com.zakrni.app.clean.data.models

typealias ArticleResponse = List<ArticleItem>

data class ArticleItem(
    val apiurl: String,
    val id: Int,
    val source_id: Int,
    val shortdescription: String?,
    val title: String
)