// data/local/QuranDatabase.kt
package com.example.zakrni.clean.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.zakrni.clean.data.local.dao.QuranDao
import com.example.zakrni.clean.data.local.entities.*

@Database(
    entities = [
        CachedSurahEntity::class,
        CachedAyahEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao
}