package com.zakrni.app.clean.ui.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object ThemeManager {
    private const val PREFS_NAME = "zakrni_settings"
    private const val KEY_DARK_MODE = "dark_mode"
    private const val KEY_NOTIFICATIONS = "notifications_enabled"
    private const val KEY_PRAYER_NOTIFICATIONS = "prayer_notifications"
    private const val KEY_AZKAR_REMINDERS = "azkar_reminders"
    private const val KEY_FONT_SIZE = "font_size"
    private const val KEY_AUTO_PLAY = "auto_play"
    private const val KEY_COUNT_SOUND = "count_sound"
    private const val KEY_ADHAN_VOICE = "adhan_voice"
    private const val KEY_APP_LANGUAGE = "app_language"

    const val LANGUAGE_ARABIC = "ar"
    const val LANGUAGE_ENGLISH = "en"

    const val ADHAN_MINSHAWI = "minshawi"
    const val ADHAN_MISHARY = "mishary"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // Dark Mode
    fun isDarkMode(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_DARK_MODE, false)
    }
    
    fun setDarkMode(context: Context, enabled: Boolean) {
        // Use commit() instead of apply() to ensure synchronous save
        getPrefs(context).edit().putBoolean(KEY_DARK_MODE, enabled).commit()
        applyTheme(enabled)
    }
    
    fun applyTheme(darkMode: Boolean) {
        val targetMode = if (darkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }

        if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode)
        }
    }
    
    fun applySavedTheme(context: Context) {
        applyTheme(isDarkMode(context))
    }
    
    // Notifications
    fun isNotificationsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_NOTIFICATIONS, true)
    }
    
    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()
    }
    
    // Prayer Notifications
    fun isPrayerNotificationsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_PRAYER_NOTIFICATIONS, true)
    }
    
    fun setPrayerNotificationsEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_PRAYER_NOTIFICATIONS, enabled).apply()
    }
    
    // Azkar Reminders
    fun isAzkarRemindersEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AZKAR_REMINDERS, true)
    }
    
    fun setAzkarRemindersEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AZKAR_REMINDERS, enabled).apply()
    }
    
    // Font Size (0 = small, 1 = medium, 2 = large)
    fun getFontSize(context: Context): Int {
        return getPrefs(context).getInt(KEY_FONT_SIZE, 1)
    }
    
    fun setFontSize(context: Context, size: Int) {
        getPrefs(context).edit().putInt(KEY_FONT_SIZE, size).apply()
    }

    /**
     * Returns a font scale multiplier based on the saved font size setting.
     * 0 (small) = 0.85, 1 (medium) = 1.0, 2 (large) = 1.2
     */
    fun getFontScale(context: Context): Float {
        return when (getFontSize(context)) {
            0 -> 0.75f
            2 -> 1.45f
            else -> 1.0f
        }
    }
    
    // Auto Play
    fun isAutoPlayEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_PLAY, false)
    }

    fun setAutoPlayEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AUTO_PLAY, enabled).apply()
    }

    // Count tap sound (tasbih / azkar / dua tap-to-count)
    fun isCountSoundEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_COUNT_SOUND, true)
    }

    fun setCountSoundEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_COUNT_SOUND, enabled).apply()
    }

    // Adhan voice used for prayer-time alerts
    fun getAdhanVoice(context: Context): String =
        getPrefs(context).getString(KEY_ADHAN_VOICE, ADHAN_MINSHAWI) ?: ADHAN_MINSHAWI

    fun setAdhanVoice(context: Context, value: String) {
        getPrefs(context).edit().putString(KEY_ADHAN_VOICE, value).apply()
    }

    // Language
    fun getCurrentAppLanguage(context: Context): String {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        val appLocale = appLocales[0]?.language
        if (!appLocale.isNullOrBlank()) return appLocale

        val savedLanguage = getPrefs(context).getString(KEY_APP_LANGUAGE, null)
        if (!savedLanguage.isNullOrBlank()) return savedLanguage

        return when (Locale.getDefault().language) {
            LANGUAGE_ENGLISH -> LANGUAGE_ENGLISH
            else -> LANGUAGE_ARABIC
        }
    }

    fun setAppLanguage(context: Context, languageCode: String) {
        getPrefs(context).edit().putString(KEY_APP_LANGUAGE, languageCode).apply()
        val targetLocales = LocaleListCompat.forLanguageTags(languageCode)
        if (AppCompatDelegate.getApplicationLocales() != targetLocales) {
            AppCompatDelegate.setApplicationLocales(targetLocales)
        }
    }

    fun applySavedLanguage(context: Context) {
        // Default to Arabic on first launch; the user can switch to English in Settings.
        val savedLanguage = getPrefs(context).getString(KEY_APP_LANGUAGE, null)
            ?.takeIf { it.isNotBlank() } ?: LANGUAGE_ARABIC

        val targetLocales = LocaleListCompat.forLanguageTags(savedLanguage)
        if (AppCompatDelegate.getApplicationLocales() != targetLocales) {
            AppCompatDelegate.setApplicationLocales(targetLocales)
        }
    }
}
