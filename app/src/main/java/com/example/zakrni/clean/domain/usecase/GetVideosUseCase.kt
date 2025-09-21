// domain/usecase/GetVideosUseCase.kt
package com.example.zakrni.clean.domain.usecase

import com.example.zakrni.clean.domain.IRepo
import com.example.zakrni.clean.domain.models.DomainVideosResponse
import javax.inject.Inject

class GetVideosUseCase @Inject constructor(
    private val repository: IRepo
) {
    suspend fun execute(
        page: Int = 1,
        category: String? = null,
        query: String? = null
    ): Result<DomainVideosResponse> {
        return repository.getVideos(page, category, query)
    }
}