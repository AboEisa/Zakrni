package com.zakrni.app.clean.ui.compose.quran

import android.content.Context

/** Persisted per-device Quran reading preferences (view mode, font scale, background mode). */
private const val READER_PREFS = "zakrni_reader"

fun readReaderMushaf(context: Context): Boolean =
    context.getSharedPreferences(READER_PREFS, Context.MODE_PRIVATE).getBoolean("mushaf_mode", true)

fun writeReaderMushaf(context: Context, value: Boolean) {
    context.getSharedPreferences(READER_PREFS, Context.MODE_PRIVATE).edit().putBoolean("mushaf_mode", value).apply()
}

fun readReaderFontScale(context: Context): Float =
    context.getSharedPreferences(READER_PREFS, Context.MODE_PRIVATE).getFloat("font_scale", 1.0f)

fun writeReaderFontScale(context: Context, value: Float) {
    context.getSharedPreferences(READER_PREFS, Context.MODE_PRIVATE).edit().putFloat("font_scale", value).apply()
}

/** 0 = normal (theme), 1 = sepia, 2 = night. */
fun readReaderMode(context: Context): Int =
    context.getSharedPreferences(READER_PREFS, Context.MODE_PRIVATE).getInt("reading_mode", 0)

fun writeReaderMode(context: Context, value: Int) {
    context.getSharedPreferences(READER_PREFS, Context.MODE_PRIVATE).edit().putInt("reading_mode", value).apply()
}
