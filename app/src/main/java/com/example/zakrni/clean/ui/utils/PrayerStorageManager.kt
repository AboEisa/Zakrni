package com.example.zakrni.clean.ui.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.zakrni.clean.ui.models.PresentationTimings
import java.text.SimpleDateFormat
import java.util.*

class PrayerStorageManager(private val context: Context) {

    companion object {
        private const val PREF_NAME = "prayer_times_storage"
        private const val KEY_FAJR = "fajr"
        private const val KEY_DHUHR = "dhuhr"
        private const val KEY_ASR = "asr"
        private const val KEY_MAGHRIB = "maghrib"
        private const val KEY_ISHA = "isha"
        private const val KEY_SUNRISE = "sunrise"
        private const val KEY_SUNSET = "sunset"
        private const val KEY_IMSAK = "imsak"
        private const val KEY_MIDNIGHT = "midnight"
        private const val KEY_FIRSTTHIRD = "firstthird"
        private const val KEY_LASTTHIRD = "lastthird"
        private const val KEY_LAST_UPDATED = "last_updated"
        private const val KEY_SAVED_DATE = "saved_date"

        private const val TAG = "PrayerStorageManager"
    }

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Save prayer times to SharedPreferences
     */
    fun savePrayerTimes(timings: PresentationTimings) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        with(sharedPrefs.edit()) {
            putString(KEY_FAJR, timings.Fajr)
            putString(KEY_DHUHR, timings.Dhuhr)
            putString(KEY_ASR, timings.Asr)
            putString(KEY_MAGHRIB, timings.Maghrib)
            putString(KEY_ISHA, timings.Isha)
            putString(KEY_SUNRISE, timings.Sunrise)
            putString(KEY_SUNSET, timings.Sunset)
            putString(KEY_IMSAK, timings.Imsak)
            putString(KEY_MIDNIGHT, timings.Midnight)
            putString(KEY_FIRSTTHIRD, timings.Firstthird)
            putString(KEY_LASTTHIRD, timings.Lastthird)
            putLong(KEY_LAST_UPDATED, System.currentTimeMillis())
            putString(KEY_SAVED_DATE, currentDate)
            apply()
        }

        Log.d(TAG, "✅ Prayer times saved for date: $currentDate")
    }

    /**
     * Get saved prayer times
     */
    fun getSavedPrayerTimes(): PresentationTimings? {
        return try {
            val fajr = sharedPrefs.getString(KEY_FAJR, null) ?: return null
            val dhuhr = sharedPrefs.getString(KEY_DHUHR, null) ?: return null
            val asr = sharedPrefs.getString(KEY_ASR, null) ?: return null
            val maghrib = sharedPrefs.getString(KEY_MAGHRIB, null) ?: return null
            val isha = sharedPrefs.getString(KEY_ISHA, null) ?: return null

            PresentationTimings(
                Fajr = fajr,
                Sunrise = sharedPrefs.getString(KEY_SUNRISE, "06:00") ?: "06:00",
                Dhuhr = dhuhr,
                Asr = asr,
                Sunset = sharedPrefs.getString(KEY_SUNSET, "18:00") ?: "18:00",
                Maghrib = maghrib,
                Isha = isha,
                Imsak = sharedPrefs.getString(KEY_IMSAK, "04:00") ?: "04:00",
                Midnight = sharedPrefs.getString(KEY_MIDNIGHT, "00:00") ?: "00:00",
                Firstthird = sharedPrefs.getString(KEY_FIRSTTHIRD, "22:00") ?: "22:00",
                Lastthird = sharedPrefs.getString(KEY_LASTTHIRD, "02:00") ?: "02:00"
            ).also {
                Log.d(TAG, "✅ Successfully loaded saved prayer times")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading saved prayer times", e)
            null
        }
    }

    /**
     * Check if saved prayer times are from today
     */
    fun areSavedTimesValid(): Boolean {
        val savedDate = sharedPrefs.getString(KEY_SAVED_DATE, null) ?: return false
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val isValid = savedDate == currentDate

        Log.d(TAG, "Prayer times validation - Saved: $savedDate, Current: $currentDate, Valid: $isValid")
        return isValid
    }

    /**
     * Clear saved prayer times
     */
    fun clearSavedTimes() {
        sharedPrefs.edit().clear().apply()
        Log.d(TAG, "🗑️ Cleared all saved prayer times")
    }

    /**
     * Get last update timestamp
     */
    fun getLastUpdated(): Long {
        return sharedPrefs.getLong(KEY_LAST_UPDATED, 0)
    }

    /**
     * Check if prayer times were saved recently (within 24 hours)
     */
    fun areSavedTimesRecent(): Boolean {
        val lastUpdated = getLastUpdated()
        val now = System.currentTimeMillis()
        val oneDayInMillis = 24 * 60 * 60 * 1000L

        val isRecent = (now - lastUpdated) < oneDayInMillis
        Log.d(TAG, "Prayer times recency check - Last updated: ${Date(lastUpdated)}, Recent: $isRecent")
        return isRecent
    }
}