package com.zakrni.app.clean.data.local

import com.zakrni.app.clean.domain.models.DomainAyah
import com.zakrni.app.clean.domain.models.DomainSurah

interface   ILocalDataSource {
    suspend fun getAllSurahs(): List<DomainSurah>
    suspend fun getAyahsBySurah(surahNumber: Int): List<DomainAyah>
    suspend fun saveSurahs(surahs: List<DomainSurah>)
    suspend fun saveAyahs(surahNumber: Int, ayahs: List<DomainAyah>)
    suspend fun isQuranDataCached(): Boolean
    suspend fun clearOldCache(daysOld: Int = 7)
}