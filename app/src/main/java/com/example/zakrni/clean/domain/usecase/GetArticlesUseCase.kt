package com.example.zakrni.clean.domain.usecase

import com.example.zakrni.clean.domain.IRepo
import com.example.zakrni.clean.domain.models.DomainArticleResponse
import javax.inject.Inject

class GetArticlesUseCase @Inject constructor(
    private val repo: IRepo
) {
    suspend operator fun invoke(): Result<DomainArticleResponse> {
        return try {
            repo.getArticles()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}