// data/local/entities/CachedSurahEntity.kt
package com.example.zakrni.clean.data.local.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "cached_surahs")
data class CachedSurahEntity(
    @PrimaryKey val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val revelationType: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_ayahs")
data class CachedAyahEntity(
    @PrimaryKey val number: Int,
    val surahNumber: Int,
    val numberInSurah: Int,
    val text: String,
    val juz: Int,
    val manzil: Int,
    val page: Int,
    val ruku: Int,
    val hizbQuarter: Int,
    val timestamp: Long = System.currentTimeMillis()
)

