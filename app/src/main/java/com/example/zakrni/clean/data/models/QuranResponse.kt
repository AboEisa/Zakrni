package com.example.zakrni.clean.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.zakrni.clean.data.local.Converters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class QuranResponse(
    val code: Int,
    val `data`: QuranData,
    val status: String
)

data class QuranData(
    val edition: Edition,
    val surahs: List<Surah>
)

data class Edition(
    val englishName: String,
    val format: String,
    val identifier: String,
    val language: String,
    val name: String,
    val type: String
)

@Entity(tableName = "cached_surahs")
@TypeConverters(Converters::class)
data class Surah(
    val ayahs: List<Ayah>,
    val englishName: String,
    val englishNameTranslation: String,
    val name: String,
    @PrimaryKey
    val number: Int,
    val revelationType: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "cached_ayahs",
    primaryKeys = ["number"]
)
data class Ayah(
    val hizbQuarter: Int,
    val juz: Int,
    val manzil: Int,
    val number: Int,
    val numberInSurah: Int,
    val page: Int,
    val ruku: Int,
    val sajda: Any? = null,
    val text: String
)