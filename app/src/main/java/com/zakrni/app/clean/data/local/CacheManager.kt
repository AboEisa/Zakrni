package com.zakrni.app.clean.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CacheManager - Manages API response caching using SharedPreferences
 * Data is cached once and served from cache on subsequent requests
 */
@Singleton
class CacheManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(CACHE_PREFS_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val CACHE_PREFS_NAME = "api_cache_prefs"
        private const val KEY_PRAYER_TIMES = "cached_prayer_times"
        private const val KEY_ALLAH_NAMES = "cached_allah_names"
        private const val KEY_HADITHS = "cached_hadiths"
        private const val KEY_AZKAR_SABAH = "cached_azkar_sabah"
        private const val KEY_AZKAR_MASAA = "cached_azkar_masaa"
        private const val KEY_AZKAR_POST_PRAYER = "cached_azkar_post_prayer"
        private const val KEY_AZKAR_NOOM = "cached_azkar_noom"
        private const val KEY_AZKAR_WAKE = "cached_azkar_wake"
        private const val KEY_AZKAR_MOSQUE = "cached_azkar_mosque"
        private const val KEY_AZKAR_EATING = "cached_azkar_eating"
        private const val KEY_AZKAR_MISC = "cached_azkar_misc"
        private const val KEY_ARTICLES = "cached_articles"
        private const val KEY_HISN_SECTIONS = "cached_hisn_sections"
        private const val KEY_DAILY_AYAHS = "cached_daily_ayahs"
        private const val KEY_CACHE_TIMESTAMP = "cache_timestamp_"
        
        // Cache validity period (7 days in milliseconds)
        private const val CACHE_VALIDITY_MS = 7 * 24 * 60 * 60 * 1000L
    }

    // Generic cache methods
    fun <T> saveToCache(key: String, data: T) {
        val json = gson.toJson(data)
        prefs.edit().apply {
            putString(key, json)
            putLong(KEY_CACHE_TIMESTAMP + key, System.currentTimeMillis())
            apply()
        }
    }

    @PublishedApi
    internal val gson: Gson = Gson()

    @PublishedApi
    internal fun sharedPrefs(): SharedPreferences = prefs

    inline fun <reified T> getFromCache(key: String): T? {
        val json = sharedPrefs().getString(key, null) ?: return null
        return try {
            gson.fromJson(json, T::class.java)
        } catch (e: Exception) {
            null
        }
    }

    inline fun <reified T> getListFromCache(key: String): List<T>? {
        val json = sharedPrefs().getString(key, null) ?: return null
        return try {
            val type = object : TypeToken<List<T>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    fun isCacheValid(key: String): Boolean {
        val timestamp = prefs.getLong(KEY_CACHE_TIMESTAMP + key, 0)
        return System.currentTimeMillis() - timestamp < CACHE_VALIDITY_MS
    }

    fun hasCache(key: String): Boolean {
        return prefs.contains(key) && isCacheValid(key)
    }

    fun clearCache(key: String) {
        prefs.edit().apply {
            remove(key)
            remove(KEY_CACHE_TIMESTAMP + key)
            apply()
        }
    }

    fun clearAllCache() {
        prefs.edit().clear().apply()
    }

    // Specific cache keys getters
    fun getPrayerTimesKey(lat: Double, lng: Double): String {
        // Round to 2 decimal places so nearby locations share cache
        val roundedLat = String.format(Locale.US, "%.2f", lat)
        val roundedLng = String.format(Locale.US, "%.2f", lng)
        // Include today's date so cache refreshes daily
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return "${KEY_PRAYER_TIMES}_${roundedLat}_${roundedLng}_$today"
    }
    fun getAllahNamesKey(): String = KEY_ALLAH_NAMES
    fun getHadithsKey(page: Int): String = "${KEY_HADITHS}_$page"
    fun getAzkarSabahKey(): String = KEY_AZKAR_SABAH
    fun getAzkarMasaaKey(): String = KEY_AZKAR_MASAA
    fun getAzkarPostPrayerKey(): String = KEY_AZKAR_POST_PRAYER
    fun getAzkarNoomKey(): String = KEY_AZKAR_NOOM
    fun getAzkarWakeKey(): String = KEY_AZKAR_WAKE
    fun getAzkarMosqueKey(): String = KEY_AZKAR_MOSQUE
    fun getAzkarEatingKey(): String = KEY_AZKAR_EATING
    fun getAzkarMiscKey(): String = KEY_AZKAR_MISC
    fun getArticlesKey(): String = KEY_ARTICLES
    fun getHisnSectionsKey(): String = KEY_HISN_SECTIONS
    fun getDailyAyahsKey(): String = KEY_DAILY_AYAHS
}
