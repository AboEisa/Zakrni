package com.zakrni.app.clean.ui.utils

import android.content.Context
import com.zakrni.app.clean.ui.models.PresentationTimings
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrayerStorageManager @Inject constructor(
    private val context: Context
) {

    companion object {
        private const val PREFS_NAME = "prayer_times"
        private const val KEY_FAJR = "fajr"
        private const val KEY_DHUHR = "dhuhr"
        private const val KEY_ASR = "asr"
        private const val KEY_MAGHRIB = "maghrib"
        private const val KEY_ISHA = "isha"
        private const val KEY_LAST_UPDATED = "last_updated"
        private const val VALIDITY_HOURS = 72 // Times valid for 72 hours (offline support)
        private const val TAG = "PrayerStorageManager"
    }

    fun savePrayerTimes(timings: PresentationTimings) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putString(KEY_FAJR, timings.Fajr)
            putString(KEY_DHUHR, timings.Dhuhr)
            putString(KEY_ASR, timings.Asr)
            putString(KEY_MAGHRIB, timings.Maghrib)
            putString(KEY_ISHA, timings.Isha)
            putLong(KEY_LAST_UPDATED, System.currentTimeMillis())
            apply()
        }
    }

    fun getSavedPrayerTimes(): PresentationTimings? {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val fajr = sharedPrefs.getString(KEY_FAJR, null) ?: return null
        val dhuhr = sharedPrefs.getString(KEY_DHUHR, null) ?: return null
        val asr = sharedPrefs.getString(KEY_ASR, null) ?: return null
        val maghrib = sharedPrefs.getString(KEY_MAGHRIB, null) ?: return null
        val isha = sharedPrefs.getString(KEY_ISHA, null) ?: return null

        return PresentationTimings(
            Fajr = fajr,
            Dhuhr = dhuhr,
            Asr = asr,
            Maghrib = maghrib,
            Isha = isha,
            Sunrise = "",
            Sunset = "",
            Midnight = "",
            Imsak = "",
            Firstthird = "",
            Lastthird = ""
        )
    }

    fun areSavedTimesValid(): Boolean {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastUpdated = sharedPrefs.getLong(KEY_LAST_UPDATED, 0)

        if (lastUpdated == 0L) return false

        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - lastUpdated
        val validityDuration = VALIDITY_HOURS * 60 * 60 * 1000 // Convert to milliseconds

        return timeDiff < validityDuration
    }

    fun clearSavedTimes() {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()
    }
}