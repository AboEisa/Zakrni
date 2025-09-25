// domain/usecase/GetAudioLecturesUseCase.kt
package com.example.zakrni.clean.domain.usecase

import com.example.zakrni.clean.domain.IRepo
import com.example.zakrni.clean.domain.models.DomainAudioResponse
import javax.inject.Inject

class GetAudioLecturesUseCase @Inject constructor(
    private val repository: IRepo
) {
    suspend fun execute(
        page: Int = 1,
        sheikh: String? = null
    ): Result<DomainAudioResponse> {
        return repository.getAudioLectures(page, sheikh)
    }
}