package com.zakrni.app.clean.domain.usecase

import com.zakrni.app.clean.domain.IRepo
import com.zakrni.app.clean.domain.models.DomainAsmaAlHusnaResponse
import com.zakrni.app.clean.domain.models.DomainHadithResponse
import javax.inject.Inject

class GetAllahNamesUseCase @Inject constructor (
    private val repo: IRepo
) {


    suspend operator fun invoke(): Result<DomainAsmaAlHusnaResponse> {
        return repo.getAllahNames()
    }
}