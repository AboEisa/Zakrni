package com.zakrni.app.clean.ui.compose.hijri

import androidx.annotation.StringRes
import com.zakrni.app.R

/**
 * A fixed-date Islamic event, pinned to a Hijri (month, day). [titleRes] / [subtitleRes] are
 * localized via strings_hijri.xml so the list is bilingual with the rest of the screen.
 */
data class IslamicEvent(
    val month: Int,
    val day: Int,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
) {
    fun matches(date: HijriDate): Boolean = date.month == month && date.day == day
}

/** The set of well-known fixed Hijri-date observances marked on the calendar. */
object IslamicEvents {
    val ALL: List<IslamicEvent> = listOf(
        IslamicEvent(1, 1, R.string.hijri_event_new_year, R.string.hijri_event_new_year_sub),
        IslamicEvent(1, 10, R.string.hijri_event_ashura, R.string.hijri_event_ashura_sub),
        IslamicEvent(3, 12, R.string.hijri_event_mawlid, R.string.hijri_event_mawlid_sub),
        IslamicEvent(7, 27, R.string.hijri_event_isra_miraj, R.string.hijri_event_isra_miraj_sub),
        IslamicEvent(9, 1, R.string.hijri_event_ramadan, R.string.hijri_event_ramadan_sub),
        IslamicEvent(10, 1, R.string.hijri_event_eid_fitr, R.string.hijri_event_eid_fitr_sub),
        IslamicEvent(12, 10, R.string.hijri_event_eid_adha, R.string.hijri_event_eid_adha_sub),
    )

    /** Events that fall within the given Hijri [month], ordered by day. */
    fun forMonth(month: Int): List<IslamicEvent> =
        ALL.filter { it.month == month }.sortedBy { it.day }

    /** The event (if any) on a specific Hijri [date]. */
    fun forDate(date: HijriDate): IslamicEvent? = ALL.firstOrNull { it.matches(date) }
}
