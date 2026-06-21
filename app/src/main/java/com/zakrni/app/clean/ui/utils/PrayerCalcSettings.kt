package com.zakrni.app.clean.ui.utils

import android.content.Context

/**
 * Persisted prayer-calculation preferences: aladhan calculation [method], Asr [school] (madhab),
 * and the optional [preAdhanMinutes] pre-adhan reminder. Defaults match the previous hardcoded
 * behaviour (method 5 = Egyptian, school 0 = standard) so existing users are unaffected.
 */
object PrayerCalcSettings {
    private const val PREFS = "zakrni_prayer_calc"
    private const val KEY_METHOD = "method"
    private const val KEY_SCHOOL = "school"
    private const val KEY_PRE_ADHAN = "pre_adhan_min"
    private const val KEY_DIRTY = "dirty"

    const val DEFAULT_METHOD = 5
    const val DEFAULT_SCHOOL = 0

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun method(context: Context): Int = prefs(context).getInt(KEY_METHOD, DEFAULT_METHOD)
    fun school(context: Context): Int = prefs(context).getInt(KEY_SCHOOL, DEFAULT_SCHOOL)
    fun preAdhanMinutes(context: Context): Int = prefs(context).getInt(KEY_PRE_ADHAN, 0)

    fun setMethod(context: Context, value: Int) {
        prefs(context).edit().putInt(KEY_METHOD, value).putBoolean(KEY_DIRTY, true).apply()
    }

    fun setSchool(context: Context, value: Int) {
        prefs(context).edit().putInt(KEY_SCHOOL, value).putBoolean(KEY_DIRTY, true).apply()
    }

    fun setPreAdhanMinutes(context: Context, value: Int) {
        prefs(context).edit().putInt(KEY_PRE_ADHAN, value).apply()
    }

    /** Returns true once if a calc setting changed since the last check, clearing the flag. */
    fun consumeDirty(context: Context): Boolean {
        val dirty = prefs(context).getBoolean(KEY_DIRTY, false)
        if (dirty) prefs(context).edit().putBoolean(KEY_DIRTY, false).apply()
        return dirty
    }
}
