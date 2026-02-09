package com.zakrni.app.clean.data.local

import androidx.room.TypeConverter
import com.zakrni.app.clean.data.models.Ayah
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromAyahList(ayahs: List<Ayah>): String {
        return gson.toJson(ayahs)
    }

    @TypeConverter
    fun toAyahList(ayahsJson: String): List<Ayah> {
        val listType = object : TypeToken<List<Ayah>>() {}.type
        return gson.fromJson(ayahsJson, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromAny(value: Any?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toAny(value: String?): Any? {
        return value
    }
}