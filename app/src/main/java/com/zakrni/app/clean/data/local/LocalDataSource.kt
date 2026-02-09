// data/local/LocalDataSource.kt
package com.zakrni.app.clean.data.local

import com.zakrni.app.clean.data.models.Ayah
import com.zakrni.app.clean.data.models.Surah
import com.zakrni.app.clean.domain.models.DomainAyah
import com.zakrni.app.clean.domain.models.DomainSurah
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val quranDao: QuranDao
) : ILocalDataSource {

    override suspend fun getAllSurahs(): List<DomainSurah> {
        return quranDao.getAllSurahs().map { surah ->
            DomainSurah(
                number = surah.number,
                name = surah.name,
                englishName = surah.englishName,
                englishNameTranslation = surah.englishNameTranslation,
                revelationType = surah.revelationType,
                ayahs = surah.ayahs.map { ayah ->
                    DomainAyah(
                        number = ayah.number,
                        numberInSurah = ayah.numberInSurah,
                        text = ayah.text,
                        juz = ayah.juz,
                        manzil = ayah.manzil,
                        page = ayah.page,
                        ruku = ayah.ruku,
                        hizbQuarter = ayah.hizbQuarter,
                        sajda = ayah.sajda
                    )
                }
            )
        }
    }

    override suspend fun getAyahsBySurah(surahNumber: Int): List<DomainAyah> {
        val surah = quranDao.getSurahByNumber(surahNumber)
        return surah?.ayahs?.map { ayah ->
            DomainAyah(
                number = ayah.number,
                numberInSurah = ayah.numberInSurah,
                text = ayah.text,
                juz = ayah.juz,
                manzil = ayah.manzil,
                page = ayah.page,
                ruku = ayah.ruku,
                hizbQuarter = ayah.hizbQuarter,
                sajda = ayah.sajda
            )
        } ?: emptyList()
    }

    override suspend fun saveSurahs(surahs: List<DomainSurah>) {
        val surahEntities = surahs.map { domainSurah ->
            Surah(
                ayahs = domainSurah.ayahs.map { domainAyah ->
                    Ayah(
                        hizbQuarter = domainAyah.hizbQuarter,
                        juz = domainAyah.juz,
                        manzil = domainAyah.manzil,
                        number = domainAyah.number,
                        numberInSurah = domainAyah.numberInSurah,
                        page = domainAyah.page,
                        ruku = domainAyah.ruku,
                        sajda = domainAyah.sajda,
                        text = domainAyah.text
                    )
                },
                englishName = domainSurah.englishName,
                englishNameTranslation = domainSurah.englishNameTranslation,
                name = domainSurah.name,
                number = domainSurah.number,
                revelationType = domainSurah.revelationType
            )
        }

        quranDao.insertSurahs(surahEntities)

        // Also insert individual ayahs for direct querying if needed
        val allAyahs = surahEntities.flatMap { it.ayahs }
        quranDao.insertAyahs(allAyahs)
    }

    override suspend fun saveAyahs(surahNumber: Int, ayahs: List<DomainAyah>) {
        val ayahEntities = ayahs.map { domainAyah ->
            Ayah(
                hizbQuarter = domainAyah.hizbQuarter,
                juz = domainAyah.juz,
                manzil = domainAyah.manzil,
                number = domainAyah.number,
                numberInSurah = domainAyah.numberInSurah,
                page = domainAyah.page,
                ruku = domainAyah.ruku,
                sajda = domainAyah.sajda,
                text = domainAyah.text
            )
        }

        // Update or create the Surah entity with embedded ayahs JSON
        val existingSurah = quranDao.getSurahByNumber(surahNumber)
        if (existingSurah != null) {
            val updatedSurah = existingSurah.copy(
                ayahs = ayahEntities,
                timestamp = System.currentTimeMillis()
            )
            quranDao.insertSurahs(listOf(updatedSurah))
        } else {
            // Surah entity doesn't exist yet — create it so getAyahsBySurah works next time
            val newSurah = Surah(
                number = surahNumber,
                name = "",
                englishName = "",
                englishNameTranslation = "",
                revelationType = "",
                ayahs = ayahEntities,
                timestamp = System.currentTimeMillis()
            )
            quranDao.insertSurah(newSurah)
        }

        // Also save individual ayahs table
        quranDao.insertAyahs(ayahEntities)
    }

    override suspend fun isQuranDataCached(): Boolean {
        return quranDao.getSurahCount() == 114
    }

    override suspend fun clearOldCache(daysOld: Int) {
        val cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
        val oldestSurah = quranDao.getAllSurahs().minByOrNull { it.timestamp }
        if (oldestSurah != null && oldestSurah.timestamp < cutoffTime) {
            quranDao.clearAllSurahs()
            quranDao.clearAllAyahs()
        }
    }

    // Additional helper methods
    suspend fun clearAllCache() {
        quranDao.clearAllSurahs()
        quranDao.clearAllAyahs()
    }

    suspend fun getSurahWithAyahs(surahNumber: Int): DomainSurah? {
        val surah = quranDao.getSurahByNumber(surahNumber)
        return surah?.let {
            DomainSurah(
                number = it.number,
                name = it.name,
                englishName = it.englishName,
                englishNameTranslation = it.englishNameTranslation,
                revelationType = it.revelationType,
                ayahs = it.ayahs.map { ayah ->
                    DomainAyah(
                        number = ayah.number,
                        numberInSurah = ayah.numberInSurah,
                        text = ayah.text,
                        juz = ayah.juz,
                        manzil = ayah.manzil,
                        page = ayah.page,
                        ruku = ayah.ruku,
                        hizbQuarter = ayah.hizbQuarter,
                        sajda = ayah.sajda
                    )
                }
            )
        }
    }

    suspend fun isSurahCached(surahNumber: Int): Boolean {
        return quranDao.getSurahByNumber(surahNumber) != null
    }
}