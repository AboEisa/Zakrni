package com.zakrni.app.clean.data.tracker

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per calendar day, recording which of the five daily prayers were marked as prayed.
 *
 * [date] is the ISO local date (yyyy-MM-dd) and acts as the natural primary key so check-offs
 * for a given day always upsert the same row. Each prayer is a simple boolean flag — the tracker
 * is a manual check-off and deliberately has no dependency on real prayer-time calculation.
 */
@Entity(tableName = "tracker_prayer_logs")
data class PrayerLogEntity(
    @PrimaryKey val date: String,
    val fajr: Boolean = false,
    val dhuhr: Boolean = false,
    val asr: Boolean = false,
    val maghrib: Boolean = false,
    val isha: Boolean = false,
) {
    /** How many of the five prayers were prayed on this day (0..5). */
    val prayedCount: Int
        get() = listOf(fajr, dhuhr, asr, maghrib, isha).count { it }

    /** All five prayers prayed → the day counts towards the streak. */
    val isComplete: Boolean
        get() = prayedCount == 5
}

/**
 * One row per calendar day holding the running dhikr (tasbeeh) count for that day.
 * [date] is the ISO local date (yyyy-MM-dd) primary key, matching [PrayerLogEntity].
 */
@Entity(tableName = "tracker_dhikr_counts")
data class DhikrCountEntity(
    @PrimaryKey val date: String,
    val count: Int = 0,
)
