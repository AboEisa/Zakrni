package com.example.zakrni.clean.ui.utils

import android.content.Context
import android.util.Log
import com.example.zakrni.clean.ui.models.PresentationTimings

class PrayerStorageManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "prayer_times"
        private const val KEY_FAJR = "fajr"
        private const val KEY_DHUHR = "dhuhr"
        private const val KEY_ASR = "asr"
        private const val KEY_MAGHRIB = "maghrib"
        private const val KEY_ISHA = "isha"
        private const val KEY_LAST_UPDATED = "last_updated"
        private const val KEY_LOCATION_NAME = "location_name"

        private const val TAG = "PrayerStorageManager"
    }

    private val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Save prayer times to SharedPreferences
     */
    fun savePrayerTimes(timings: PresentationTimings, locationName: String = "") {
        try {
            with(sharedPrefs.edit()) {
                putString(KEY_FAJR, timings.Fajr)
                putString(KEY_DHUHR, timings.Dhuhr)
                putString(KEY_ASR, timings.Asr)
                putString(KEY_MAGHRIB, timings.Maghrib)
                putString(KEY_ISHA, timings.Isha)
                putString(KEY_LOCATION_NAME, locationName)
                putLong(KEY_LAST_UPDATED, System.currentTimeMillis())
                apply()
            }
            Log.d(TAG, "Prayer times saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving prayer times", e)
        }
    }

    /**
     * Get saved prayer times from SharedPreferences
     */
    fun getSavedPrayerTimes(): PresentationTimings? {
        return try {
            val fajr = sharedPrefs.getString(KEY_FAJR, null)
            val dhuhr = sharedPrefs.getString(KEY_DHUHR, null)
            val asr = sharedPrefs.getString(KEY_ASR, null)
            val maghrib = sharedPrefs.getString(KEY_MAGHRIB, null)
            val isha = sharedPrefs.getString(KEY_ISHA, null)

            if (fajr != null && dhuhr != null && asr != null && maghrib != null && isha != null) {
                PresentationTimings(
                    Fajr = fajr,
                    Sunrise = "", // Not needed for alarms
                    Dhuhr = dhuhr,
                    Asr = asr,
                    Sunset = "", // Not needed for alarms
                    Maghrib = maghrib,
                    Isha = isha,
                    Imsak = "", // Not needed for alarms
                    Midnight = "", // Not needed for alarms
                    Firstthird = "", // Not needed for alarms
                    Lastthird = "" // Not needed for alarms
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving saved prayer times", e)
            null
        }
    }

    /**
     * Get saved location name
     */
    fun getSavedLocationName(): String {
        return sharedPrefs.getString(KEY_LOCATION_NAME, "") ?: ""
    }

    /**
     * Get last update timestamp
     */
    fun getLastUpdateTime(): Long {
        return sharedPrefs.getLong(KEY_LAST_UPDATED, 0)
    }

    /**
     * Check if saved prayer times are still valid (not older than 24 hours)
     */
    fun areSavedTimesValid(): Boolean {
        val lastUpdate = getLastUpdateTime()
        val now = System.currentTimeMillis()
        val oneDayInMillis = 24 * 60 * 60 * 1000L

        return (now - lastUpdate) < oneDayInMillis && getSavedPrayerTimes() != null
    }

    /**
     * Clear all saved prayer times
     */
    fun clearSavedTimes() {
        try {
            sharedPrefs.edit().clear().apply()
            Log.d(TAG, "Saved prayer times cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing saved times", e)
        }
    }
}