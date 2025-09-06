package com.example.zakrni.clean.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.zakrni.clean.data.models.Ayah
import com.example.zakrni.clean.data.models.Surah

@Dao
interface QuranDao {

    @Query("SELECT * FROM cached_surahs ORDER BY number")
    suspend fun getAllSurahs(): List<Surah>

    @Query("SELECT * FROM cached_surahs WHERE number = :surahNumber")
    suspend fun getSurahByNumber(surahNumber: Int): Surah?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurahs(surahs: List<Surah>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurah(surah: Surah)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<Ayah>)

    @Query("SELECT COUNT(*) FROM cached_surahs")
    suspend fun getSurahCount(): Int

    @Query("DELETE FROM cached_surahs WHERE timestamp < :timestamp")
    suspend fun deleteOldSurahs(timestamp: Long)

    @Query("DELETE FROM cached_ayahs WHERE number IN (SELECT number FROM cached_ayahs WHERE number < :timestamp)")
    suspend fun deleteOldAyahs(timestamp: Long)

    @Query("DELETE FROM cached_surahs")
    suspend fun clearAllSurahs()

    @Query("DELETE FROM cached_ayahs")
    suspend fun clearAllAyahs()

    // Additional utility methods
    @Query("SELECT EXISTS(SELECT 1 FROM cached_surahs WHERE number = :surahNumber)")
    suspend fun isSurahCached(surahNumber: Int): Boolean

    @Query("SELECT MIN(timestamp) FROM cached_surahs")
    suspend fun getOldestCacheTimestamp(): Long?
}