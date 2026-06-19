package com.zakrni.app.clean.ui.utils

import android.content.Context
import org.json.JSONArray

object SurahTafsirProvider {

    private const val TAFSIR_ASSET_FILE = "surah_tafsir.json"

    data class SurahTafsir(
        val number: Int,
        val arabicName: String,
        val englishName: String,
        val arabicTafsir: String,
        val englishTafsir: String
    )

    @Volatile
    private var tafsirCache: Map<Int, SurahTafsir>? = null

    fun getTafsirText(context: Context, surahNumber: Int, isArabic: Boolean): String? {
        val item = getTafsirEntry(context, surahNumber) ?: return null
        val preferred = if (isArabic) item.arabicTafsir else item.englishTafsir
        val text = preferred.takeIf { it.isNotBlank() } ?: item.arabicTafsir
        return text.normalizeTafsirText()
    }

    fun getTafsirEntry(context: Context, surahNumber: Int): SurahTafsir? {
        return ensureCache(context)[surahNumber]
    }

    private fun ensureCache(context: Context): Map<Int, SurahTafsir> {
        tafsirCache?.let { return it }
        synchronized(this) {
            tafsirCache?.let { return it }
            val loaded = loadFromAssets(context)
            tafsirCache = loaded
            return loaded
        }
    }

    private fun loadFromAssets(context: Context): Map<Int, SurahTafsir> {
        val json = runCatching {
            context.assets.open(TAFSIR_ASSET_FILE).bufferedReader().use { it.readText() }
        }.getOrNull() ?: return emptyMap()

        val array = runCatching { JSONArray(json) }.getOrNull() ?: return emptyMap()
        val output = linkedMapOf<Int, SurahTafsir>()

        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val number = item.optInt("number", -1)
            if (number !in 1..114) continue

            output[number] = SurahTafsir(
                number = number,
                arabicName = item.optString("arabicName", ""),
                englishName = item.optString("englishName", ""),
                arabicTafsir = item.optString("arabicTafsir", ""),
                englishTafsir = item.optString("englishTafsir", "")
            )
        }
        return output
    }

    private fun String.normalizeTafsirText(): String {
        return replace(Regex("\\s+"), " ").trim()
    }
}
