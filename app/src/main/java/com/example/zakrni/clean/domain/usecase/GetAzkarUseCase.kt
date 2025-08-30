package com.example.zakrni.clean.domain.usecase

import com.example.zakrni.clean.domain.IRepo
import javax.inject.Inject

class GetAzkarUseCase @Inject constructor(
     private val repo: IRepo
) {

    suspend operator fun invoke() = repo.getAzkar()
}