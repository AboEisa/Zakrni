// data/local/LocalDataSource.kt
package com.example.zakrni.clean.data.local

import com.example.zakrni.clean.data.local.dao.QuranDao
import com.example.zakrni.clean.data.local.entities.*
import com.example.zakrni.clean.domain.models.*
import com.example.zakrni.clean.ui.utils.QuranUtils
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val quranDao: QuranDao
) : ILocalDataSource {

    override suspend fun getAllSurahs(): List<DomainSurah> {
        return quranDao.getAllSurahs().map { entity ->
            DomainSurah(
                number = entity.number,
                name = entity.name,
                englishName = entity.englishName,
                englishNameTranslation = entity.englishNameTranslation,
                revelationType = entity.revelationType,
                ayahs = emptyList()
            )
        }
    }

    override suspend fun getAyahsBySurah(surahNumber: Int): List<DomainAyah> {
        return quranDao.getAyahsBySurah(surahNumber).map { entity ->
            DomainAyah(
                number = entity.number,
                numberInSurah = entity.numberInSurah,
                text = entity.text,
                juz = entity.juz,
                manzil = entity.manzil,
                page = entity.page,
                ruku = entity.ruku,
                hizbQuarter = entity.hizbQuarter,
                sajda = null   // 👈 only remote will fill this
            )
        }
    }


    override suspend fun saveSurahs(surahs: List<DomainSurah>) {
        val entities = surahs.map { surah ->
            CachedSurahEntity(
                number = surah.number,
                name = surah.name,
                englishName = surah.englishName,
                englishNameTranslation = surah.englishNameTranslation,
                revelationType = surah.revelationType
            )
        }
        quranDao.insertSurahs(entities)
    }

    override suspend fun saveAyahs(surahNumber: Int, ayahs: List<DomainAyah>) {
        val entities = ayahs.map { ayah ->
            CachedAyahEntity(
                number = ayah.number,
                surahNumber = surahNumber,
                numberInSurah = ayah.numberInSurah,
                text = ayah.text,
                juz = ayah.juz,
                manzil = ayah.manzil,
                page = ayah.page,
                ruku = ayah.ruku,
                hizbQuarter = ayah.hizbQuarter
            )
        }
        quranDao.insertAyahs(entities)
    }


    override suspend fun isQuranDataCached(): Boolean {
        return quranDao.getSurahCount() == 114
    }

    override suspend fun clearOldCache(daysOld: Int) {
        val cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
        quranDao.deleteOldSurahs(cutoffTime)
        quranDao.deleteOldAyahs(cutoffTime)
    }
}