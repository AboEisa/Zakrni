package com.example.zakrni.clean.domain.usecases

import com.example.zakrni.clean.data.Repo
import com.example.zakrni.clean.domain.models.DomainPrayerTimesResponse

import javax.inject.Inject

class GetPrayerTimesUseCase @Inject constructor(
    private val repo: Repo
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double
    ): Result<DomainPrayerTimesResponse> {
        return repo.getPrayerTimes(latitude, longitude)
    }
}