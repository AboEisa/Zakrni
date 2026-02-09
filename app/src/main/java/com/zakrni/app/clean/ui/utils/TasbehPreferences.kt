package com.zakrni.app.clean.ui.utils

import android.content.Context
import com.zakrni.app.clean.ui.views.TasbehFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TasbehPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("tasbeh_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveDhikrList(list: List<TasbehFragment.DhikrItem>) {
        val json = gson.toJson(list)
        prefs.edit().putString("dhikr_list", json).apply()
    }

    fun getDhikrList(): List<TasbehFragment.DhikrItem> {
        val json = prefs.getString("dhikr_list", null) ?: return emptyList()
        val type = object : TypeToken<List<TasbehFragment.DhikrItem>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveCurrentDhikrIndex(index: Int) {
        prefs.edit().putInt("current_dhikr_index", index).apply()
    }

    fun getCurrentDhikrIndex(): Int {
        return prefs.getInt("current_dhikr_index", 0)
    }
}