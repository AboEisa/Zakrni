package com.example.zakrni.clean.domain.usecase

import com.example.zakrni.clean.domain.IRepo
import com.example.zakrni.clean.domain.models.DomainAsmaAlHusnaResponse
import com.example.zakrni.clean.domain.models.DomainHadithResponse
import javax.inject.Inject

class GetAllahNamesUseCase @Inject constructor (
    private val repo: IRepo
) {


    suspend operator fun invoke(): Result<DomainAsmaAlHusnaResponse> {
        return repo.getAllahNames()
    }
}