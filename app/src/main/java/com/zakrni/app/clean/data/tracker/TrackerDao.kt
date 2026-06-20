package com.zakrni.app.clean.data.tracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for the worship tracker. Reads are exposed as [Flow] so the UI recomposes automatically
 * whenever a prayer is toggled or a dhikr count changes.
 */
@Dao
interface TrackerDao {

    // ---- Prayer logs ----

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPrayerLog(log: PrayerLogEntity)

    @Query("SELECT * FROM tracker_prayer_logs WHERE date = :date LIMIT 1")
    suspend fun getPrayerLog(date: String): PrayerLogEntity?

    @Query("SELECT * FROM tracker_prayer_logs WHERE date = :date LIMIT 1")
    fun observePrayerLog(date: String): Flow<PrayerLogEntity?>

    /** All logs on or after [fromDate] (inclusive), newest first — used for streak + heatmap. */
    @Query("SELECT * FROM tracker_prayer_logs WHERE date >= :fromDate ORDER BY date DESC")
    fun observeLogsSince(fromDate: String): Flow<List<PrayerLogEntity>>

    /** Every log, newest first. Used to compute the current streak across all history. */
    @Query("SELECT * FROM tracker_prayer_logs ORDER BY date DESC")
    fun observeAllLogs(): Flow<List<PrayerLogEntity>>

    // ---- Dhikr counts ----

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDhikrCount(entity: DhikrCountEntity)

    @Query("SELECT * FROM tracker_dhikr_counts WHERE date = :date LIMIT 1")
    suspend fun getDhikrCount(date: String): DhikrCountEntity?

    @Query("SELECT * FROM tracker_dhikr_counts WHERE date = :date LIMIT 1")
    fun observeDhikrCount(date: String): Flow<DhikrCountEntity?>
}
