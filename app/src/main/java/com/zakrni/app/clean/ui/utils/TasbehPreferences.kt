package com.zakrni.app.clean.ui.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * A countable dhikr. Lives here (not in a Fragment) so the Compose Tasbih screen and the
 * preference store can share it after the legacy UI was removed. Field names are unchanged
 * so previously-saved JSON still deserializes.
 */
data class DhikrItem(
    val id: Int,
    val arabicText: String,
    val targetCount: Int,
    val color: String,
    var currentCount: Int = 0,
    var isCompleted: Boolean = false,
    var isCustom: Boolean = false,
)

class TasbehPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("tasbeh_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveDhikrList(list: List<DhikrItem>) {
        val json = gson.toJson(list)
        prefs.edit().putString("dhikr_list", json).apply()
    }

    fun getDhikrList(): List<DhikrItem> {
        val json = prefs.getString("dhikr_list", null) ?: return emptyList()
        val type = object : TypeToken<List<DhikrItem>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveCurrentDhikrIndex(index: Int) {
        prefs.edit().putInt("current_dhikr_index", index).apply()
    }

    fun getCurrentDhikrIndex(): Int {
        return prefs.getInt("current_dhikr_index", 0)
    }
}
