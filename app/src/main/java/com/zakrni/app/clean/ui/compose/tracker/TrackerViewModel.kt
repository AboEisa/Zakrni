package com.zakrni.app.clean.ui.compose.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zakrni.app.clean.data.tracker.DhikrCountEntity
import com.zakrni.app.clean.data.tracker.Prayer
import com.zakrni.app.clean.data.tracker.PrayerLogEntity
import com.zakrni.app.clean.data.tracker.TrackerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** Number of trailing days shown in the heatmap (5 weeks). */
private const val HEATMAP_DAYS = 35

/** A single day cell for the Canvas heatmap. */
data class DayCompletion(
    val date: LocalDate,
    val prayedCount: Int, // 0..5
) {
    val fraction: Float get() = prayedCount / 5f
    val isComplete: Boolean get() = prayedCount == 5
}

/** Immutable snapshot of everything the dashboard renders. */
data class TrackerUiState(
    val today: LocalDate = LocalDate.now(),
    val fajr: Boolean = false,
    val dhuhr: Boolean = false,
    val asr: Boolean = false,
    val maghrib: Boolean = false,
    val isha: Boolean = false,
    val currentStreak: Int = 0,
    val dhikrCount: Int = 0,
    val heatmap: List<DayCompletion> = emptyList(),
) {
    val todayPrayedCount: Int
        get() = listOf(fajr, dhuhr, asr, maghrib, isha).count { it }

    /** 0f..1f progress for today's prayers — drives the animated ring. */
    val todayProgress: Float
        get() = todayPrayedCount / 5f

    fun isPrayed(prayer: Prayer): Boolean = when (prayer) {
        Prayer.FAJR -> fajr
        Prayer.DHUHR -> dhuhr
        Prayer.ASR -> asr
        Prayer.MAGHRIB -> maghrib
        Prayer.ISHA -> isha
    }

    /** Completed days within the heatmap window (used for the weekly/monthly summary). */
    val completedInWindow: Int
        get() = heatmap.count { it.isComplete }
}

@HiltViewModel
class TrackerViewModel @Inject constructor(
    private val repository: TrackerRepository,
) : ViewModel() {

    private val today: LocalDate = repository.today()
    private val windowStart: LocalDate = today.minusDays((HEATMAP_DAYS - 1).toLong())

    val uiState: StateFlow<TrackerUiState> = combine(
        repository.observePrayerLog(today),
        repository.observeAllLogs(),
        repository.observeLogsSince(windowStart),
        repository.observeDhikrCount(today),
    ) { todayLog, allLogs, windowLogs, dhikr ->
        buildState(todayLog, allLogs, windowLogs, dhikr)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrackerUiState(today = today),
    )

    private fun buildState(
        todayLog: PrayerLogEntity?,
        allLogs: List<PrayerLogEntity>,
        windowLogs: List<PrayerLogEntity>,
        dhikr: DhikrCountEntity?,
    ): TrackerUiState {
        val log = todayLog ?: PrayerLogEntity(date = repository.keyOf(today))
        val countsByDate: Map<LocalDate, Int> = windowLogs
            .mapNotNull { entity ->
                runCatching { LocalDate.parse(entity.date) }.getOrNull()?.let { it to entity.prayedCount }
            }
            .toMap()

        val heatmap = (0 until HEATMAP_DAYS).map { offset ->
            val date = windowStart.plusDays(offset.toLong())
            DayCompletion(date = date, prayedCount = countsByDate[date] ?: 0)
        }

        return TrackerUiState(
            today = today,
            fajr = log.fajr,
            dhuhr = log.dhuhr,
            asr = log.asr,
            maghrib = log.maghrib,
            isha = log.isha,
            currentStreak = TrackerRepository.currentStreak(allLogs, today),
            dhikrCount = dhikr?.count ?: 0,
            heatmap = heatmap,
        )
    }

    fun togglePrayer(prayer: Prayer) {
        viewModelScope.launch { repository.togglePrayer(today, prayer) }
    }

    fun incrementDhikr() {
        viewModelScope.launch { repository.incrementDhikr(today) }
    }

    fun resetDhikr() {
        viewModelScope.launch { repository.resetDhikr(today) }
    }
}
