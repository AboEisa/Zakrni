package com.zakrni.app.clean.ui.utils

import android.content.Context
import android.content.SharedPreferences

class AudioPreferences(context: Context) {

    private val appContext = context.applicationContext

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "audio_preferences"
        private const val KEY_AUTO_PLAY = "auto_play_enabled"
        private const val KEY_SELECTED_RECITER = "selected_reciter"
        private const val KEY_SELECTED_RECITER_NAME = "selected_reciter_name"
        private const val KEY_REPEAT_MODE = "repeat_mode"
        private const val KEY_PLAYBACK_SPEED = "playback_speed"
        private const val KEY_LAST_PLAYED_SURAH = "last_played_surah"
        private const val KEY_LAST_POSITION = "last_position"

        // Repeat modes
        const val REPEAT_OFF = 0
        const val REPEAT_ONE = 1
        const val REPEAT_ALL = 2
    }

    // Auto-play settings - Default OFF for professional behavior (play only on click)
    var isAutoPlayEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_PLAY, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_PLAY, value).apply()

    // Selected reciter
    var selectedReciter: String
        get() = ReciterCatalog.normalize(prefs.getString(KEY_SELECTED_RECITER, ReciterCatalog.DEFAULT_RECITER_ID))
        set(value) = prefs.edit().putString(KEY_SELECTED_RECITER, ReciterCatalog.normalize(value)).apply()

    var selectedReciterName: String
        get() = prefs.getString(KEY_SELECTED_RECITER_NAME, null)
            ?: getReciterDisplayName(selectedReciter, isArabicUi())
        set(value) = prefs.edit().putString(KEY_SELECTED_RECITER_NAME, value).apply()

    // Repeat mode
    var repeatMode: Int
        get() = prefs.getInt(KEY_REPEAT_MODE, REPEAT_OFF)
        set(value) = prefs.edit().putInt(KEY_REPEAT_MODE, value).apply()

    // Playback speed (1.0 = normal)
    var playbackSpeed: Float
        get() = prefs.getFloat(KEY_PLAYBACK_SPEED, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_PLAYBACK_SPEED, value).apply()

    // Last played surah for resuming
    var lastPlayedSurah: Int
        get() = prefs.getInt(KEY_LAST_PLAYED_SURAH, -1)
        set(value) = prefs.edit().putInt(KEY_LAST_PLAYED_SURAH, value).apply()

    var lastPosition: Int
        get() = prefs.getInt(KEY_LAST_POSITION, 0)
        set(value) = prefs.edit().putInt(KEY_LAST_POSITION, value).apply()

    // Toggle auto-play
    fun toggleAutoPlay(): Boolean {
        val newValue = !isAutoPlayEnabled
        isAutoPlayEnabled = newValue
        return newValue
    }

    // Cycle through repeat modes
    fun cycleRepeatMode(): Int {
        val newMode = when (repeatMode) {
            REPEAT_OFF -> REPEAT_ONE
            REPEAT_ONE -> REPEAT_ALL
            else -> REPEAT_OFF
        }
        repeatMode = newMode
        return newMode
    }

    // Save current playback position
    fun savePlaybackState(surahNumber: Int, position: Int) {
        prefs.edit()
            .putInt(KEY_LAST_PLAYED_SURAH, surahNumber)
            .putInt(KEY_LAST_POSITION, position)
            .apply()
    }

    // Clear playback state
    fun clearPlaybackState() {
        prefs.edit()
            .remove(KEY_LAST_PLAYED_SURAH)
            .remove(KEY_LAST_POSITION)
            .apply()
    }

    // Get reciter display name from identifier
    fun getReciterDisplayName(identifier: String, isArabic: Boolean = isArabicUi()): String {
        return ReciterCatalog.getDisplayName(identifier, isArabic)
    }

    private fun isArabicUi(): Boolean {
        val appLocale = appContext.resources.configuration.locales[0]
        val language = appLocale?.language ?: "en"
        return language.equals("ar", ignoreCase = true)
    }
}
