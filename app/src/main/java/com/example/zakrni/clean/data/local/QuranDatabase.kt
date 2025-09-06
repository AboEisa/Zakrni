package com.example.zakrni.clean.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.zakrni.clean.data.models.Ayah
import com.example.zakrni.clean.data.models.Surah

@Database(
    entities = [Surah::class, Ayah::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class QuranDatabase : RoomDatabase() {

    abstract fun quranDao(): QuranDao

}