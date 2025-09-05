package com.example.zakrni.clean.data.local

import com.example.zakrni.clean.domain.models.DomainAyah
import com.example.zakrni.clean.domain.models.DomainSurah

interface   ILocalDataSource {
    suspend fun getAllSurahs(): List<DomainSurah>
    suspend fun getAyahsBySurah(surahNumber: Int): List<DomainAyah>
    suspend fun saveSurahs(surahs: List<DomainSurah>)
    suspend fun saveAyahs(surahNumber: Int, ayahs: List<DomainAyah>)
    suspend fun isQuranDataCached(): Boolean
    suspend fun clearOldCache(daysOld: Int = 7)
}