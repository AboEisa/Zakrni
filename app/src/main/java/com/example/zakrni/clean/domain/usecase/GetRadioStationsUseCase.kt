// domain/usecase/GetRadioStationsUseCase.kt
package com.example.zakrni.clean.domain.usecase

import com.example.zakrni.clean.domain.IRepo
import com.example.zakrni.clean.domain.models.DomainRadioResponse
import com.example.zakrni.clean.domain.models.DomainRecitersResponse
import javax.inject.Inject

class GetRadioStationsUseCase @Inject constructor(
    private val repository: IRepo
) {
    suspend fun execute(
        country: String? = null,
        language: String? = null
    ): Result<DomainRadioResponse> {
        return repository.getRadioStations(country, language)
    }
}

