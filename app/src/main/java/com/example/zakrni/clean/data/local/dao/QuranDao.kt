// data/local/dao/QuranDao.kt
package com.example.zakrni.clean.data.local.dao

import androidx.room.*
import com.example.zakrni.clean.data.local.entities.*

@Dao
interface QuranDao {
    @Query("SELECT * FROM cached_surahs ORDER BY number")
    suspend fun getAllSurahs(): List<CachedSurahEntity>

    @Query("SELECT * FROM cached_ayahs WHERE surahNumber = :surahNumber ORDER BY numberInSurah")
    suspend fun getAyahsBySurah(surahNumber: Int): List<CachedAyahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurahs(surahs: List<CachedSurahEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<CachedAyahEntity>)

    @Query("SELECT COUNT(*) FROM cached_surahs")
    suspend fun getSurahCount(): Int

    @Query("DELETE FROM cached_surahs WHERE timestamp < :timestamp")
    suspend fun deleteOldSurahs(timestamp: Long)

    @Query("DELETE FROM cached_ayahs WHERE timestamp < :timestamp")
    suspend fun deleteOldAyahs(timestamp: Long)
}