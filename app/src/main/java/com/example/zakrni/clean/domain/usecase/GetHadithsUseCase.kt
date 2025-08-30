package com.example.zakrni.clean.domain.usecase

import com.example.zakrni.clean.domain.IRepo
import com.example.zakrni.clean.domain.models.DomainHadithResponse
import javax.inject.Inject

class GetHadithsUseCase @Inject constructor (
    private val repo: IRepo
) {

    suspend operator fun invoke(page: Int, limit: Int = 10): Result<DomainHadithResponse> {
        return repo.getHadiths(page, limit)
    }
}