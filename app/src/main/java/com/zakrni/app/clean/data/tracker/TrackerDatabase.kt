package com.zakrni.app.clean.data.tracker

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Standalone Room database for the worship streaks / habit tracker.
 *
 * Deliberately separate from the shared [com.zakrni.app.clean.data.local.QuranDatabase] so this
 * feature stays fully self-contained and owns its own schema + migrations.
 */
@Database(
    entities = [PrayerLogEntity::class, DhikrCountEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class TrackerDatabase : RoomDatabase() {
    abstract fun trackerDao(): TrackerDao
}
