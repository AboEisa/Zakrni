package com.zakrni.app.clean.domain.usecase

import com.zakrni.app.clean.domain.IRepo
import com.zakrni.app.clean.domain.models.DomainHadithResponse
import javax.inject.Inject

class GetHadithsUseCase @Inject constructor (
    private val repo: IRepo
) {

    suspend operator fun invoke(page: Int, limit: Int = 10): Result<DomainHadithResponse> {
        return repo.getHadiths(page, limit)
    }
}