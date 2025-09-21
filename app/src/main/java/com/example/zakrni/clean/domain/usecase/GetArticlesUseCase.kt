// domain/usecase/GetArticlesUseCase.kt
package com.example.zakrni.clean.domain.usecase

import com.example.zakrni.clean.domain.IRepo
import com.example.zakrni.clean.domain.models.DomainArticlesResponse
import javax.inject.Inject

class GetArticlesUseCase @Inject constructor(
    private val repository: IRepo
) {
    suspend fun execute(
        page: Int = 1,
        category: String? = null
    ): Result<DomainArticlesResponse> {
        return repository.getArticles(page, category)
    }
}