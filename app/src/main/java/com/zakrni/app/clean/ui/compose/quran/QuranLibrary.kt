package com.zakrni.app.clean.ui.compose.quran

import android.content.Context

/**
 * Persistent "library" for the Quran feature (no Hilt/Room): the last-read surah (for the Home
 * "continue reading" card) and a set of favorite ayat.
 */
private const val LIB_PREFS = "zakrni_quran_library"
private const val KEY_LAST_SURAH = "last_surah"
private const val KEY_LAST_NAME_AR = "last_name_ar"
private const val KEY_LAST_NAME_EN = "last_name_en"
private const val KEY_FAVORITES = "favorites"

private fun prefs(context: Context) = context.getSharedPreferences(LIB_PREFS, Context.MODE_PRIVATE)

// ----- Continue reading -----

fun writeLastRead(context: Context, surah: Int, nameAr: String, nameEn: String) {
    prefs(context).edit()
        .putInt(KEY_LAST_SURAH, surah)
        .putString(KEY_LAST_NAME_AR, nameAr)
        .putString(KEY_LAST_NAME_EN, nameEn)
        .apply()
}

/** Last-read surah number, or 0 when nothing has been read yet. */
fun readLastReadSurah(context: Context): Int = prefs(context).getInt(KEY_LAST_SURAH, 0)

fun readLastReadName(context: Context, arabic: Boolean): String =
    prefs(context).getString(if (arabic) KEY_LAST_NAME_AR else KEY_LAST_NAME_EN, "").orEmpty()

// ----- Favorite ayat -----

data class FavoriteAyah(
    val surah: Int,
    val ayahInSurah: Int,
    val surahName: String,
    val text: String,
) {
    fun id(): String = "$surah:$ayahInSurah"
    fun encode(): String = listOf(
        surah.toString(),
        ayahInSurah.toString(),
        surahName.replace("|", " "),
        text.replace("\n", " ").replace("|", " "),
    ).joinToString("|")
}

private fun decodeFavorite(raw: String): FavoriteAyah? {
    val parts = raw.split("|", limit = 4)
    if (parts.size < 4) return null
    val surah = parts[0].toIntOrNull() ?: return null
    val ayah = parts[1].toIntOrNull() ?: return null
    return FavoriteAyah(surah, ayah, parts[2], parts[3])
}

private fun rawFavorites(context: Context): MutableSet<String> =
    prefs(context).getStringSet(KEY_FAVORITES, emptySet())!!.toMutableSet()

fun readFavorites(context: Context): List<FavoriteAyah> =
    rawFavorites(context).mapNotNull { decodeFavorite(it) }.sortedWith(compareBy({ it.surah }, { it.ayahInSurah }))

fun isFavorite(context: Context, surah: Int, ayahInSurah: Int): Boolean =
    rawFavorites(context).any { it.startsWith("$surah|$ayahInSurah|") }

/** Adds or removes the ayah; returns the new favorite state (true = now a favorite). */
fun toggleFavorite(context: Context, fav: FavoriteAyah): Boolean {
    val set = rawFavorites(context)
    val existing = set.firstOrNull { it.startsWith("${fav.surah}|${fav.ayahInSurah}|") }
    val nowFavorite: Boolean
    if (existing != null) {
        set.remove(existing)
        nowFavorite = false
    } else {
        set.add(fav.encode())
        nowFavorite = true
    }
    prefs(context).edit().putStringSet(KEY_FAVORITES, set).apply()
    return nowFavorite
}

fun removeFavorite(context: Context, surah: Int, ayahInSurah: Int) {
    val set = rawFavorites(context)
    set.removeAll { it.startsWith("$surah|$ayahInSurah|") }
    prefs(context).edit().putStringSet(KEY_FAVORITES, set).apply()
}
