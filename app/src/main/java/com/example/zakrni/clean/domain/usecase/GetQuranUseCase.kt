package com.example.zakrni.clean.domain.usecase

import com.example.zakrni.clean.domain.IRepo
import com.example.zakrni.clean.domain.models.DomainAyah
import com.example.zakrni.clean.domain.models.DomainSurah
import javax.inject.Inject

class GetQuranUseCase @Inject constructor(
    private val repository: IRepo
) {
    suspend fun getQuranVerses(surahNumber: Int): List<DomainAyah> {
        return repository.getQuranVerses(surahNumber)
    }

    suspend fun getAllSurahs(): List<DomainSurah> {
        return repository.getAllSurahs()
    }

    suspend fun getSurahByNumber(surahNumber: Int): DomainSurah? {
        return repository.getAllSurahs().find { it.number == surahNumber }
    }
}