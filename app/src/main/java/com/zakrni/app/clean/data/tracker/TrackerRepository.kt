package com.zakrni.app.clean.data.tracker

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Which of the five daily prayers a row represents. Order matches the canonical daily order.
 */
enum class Prayer { FAJR, DHUHR, ASR, MAGHRIB, ISHA }

/**
 * Single point of access for the worship tracker data. Wraps [TrackerDao] and owns the
 * date<->String conversion plus the streak computation so the ViewModel stays thin.
 */
@Singleton
class TrackerRepository @Inject constructor(
    private val dao: TrackerDao,
) {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun keyOf(date: LocalDate): String = date.format(formatter)

    fun today(): LocalDate = LocalDate.now()

    // ---- Prayers ----

    fun observePrayerLog(date: LocalDate): Flow<PrayerLogEntity?> =
        dao.observePrayerLog(keyOf(date))

    fun observeLogsSince(fromDate: LocalDate): Flow<List<PrayerLogEntity>> =
        dao.observeLogsSince(keyOf(fromDate))

    fun observeAllLogs(): Flow<List<PrayerLogEntity>> = dao.observeAllLogs()

    /** Toggle a single prayer for [date], creating the row if it does not yet exist. */
    suspend fun togglePrayer(date: LocalDate, prayer: Prayer) {
        val key = keyOf(date)
        val current = dao.getPrayerLog(key) ?: PrayerLogEntity(date = key)
        val updated = when (prayer) {
            Prayer.FAJR -> current.copy(fajr = !current.fajr)
            Prayer.DHUHR -> current.copy(dhuhr = !current.dhuhr)
            Prayer.ASR -> current.copy(asr = !current.asr)
            Prayer.MAGHRIB -> current.copy(maghrib = !current.maghrib)
            Prayer.ISHA -> current.copy(isha = !current.isha)
        }
        dao.upsertPrayerLog(updated)
    }

    // ---- Dhikr ----

    fun observeDhikrCount(date: LocalDate): Flow<DhikrCountEntity?> =
        dao.observeDhikrCount(keyOf(date))

    suspend fun incrementDhikr(date: LocalDate, by: Int = 1) {
        val key = keyOf(date)
        val current = dao.getDhikrCount(key)?.count ?: 0
        dao.upsertDhikrCount(DhikrCountEntity(date = key, count = (current + by).coerceAtLeast(0)))
    }

    suspend fun resetDhikr(date: LocalDate) {
        dao.upsertDhikrCount(DhikrCountEntity(date = keyOf(date), count = 0))
    }

    companion object {
        /**
         * Compute the current daily streak: the number of consecutive complete days
         * (all five prayers prayed) ending today, or — if today is not yet complete — ending
         * yesterday. Today being incomplete does not break a streak that is still "in progress".
         *
         * Pure function (no I/O) so it can be unit-tested and reused by the ViewModel.
         *
         * @param logs any collection of logs (order independent).
         * @param today reference "today" date.
         */
        fun currentStreak(logs: Collection<PrayerLogEntity>, today: LocalDate): Int {
            val completeDays: Set<LocalDate> = logs
                .asSequence()
                .filter { it.isComplete }
                .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
                .toSet()
            if (completeDays.isEmpty()) return 0

            // Start counting from today if complete, otherwise from yesterday (today still open).
            var cursor = if (today in completeDays) today else today.minusDays(1)
            var streak = 0
            while (cursor in completeDays) {
                streak++
                cursor = cursor.minusDays(1)
            }
            return streak
        }
    }
}
