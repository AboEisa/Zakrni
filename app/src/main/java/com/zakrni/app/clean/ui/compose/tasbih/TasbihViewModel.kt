package com.zakrni.app.clean.ui.compose.tasbih

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.zakrni.app.clean.ui.utils.DhikrItem
import com.zakrni.app.clean.ui.utils.LocaleHelper
import com.zakrni.app.clean.ui.utils.TasbehPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** A dhikr the user can count, rendered by the Compose screen. */
data class TasbihDhikr(
    val id: Int,
    val text: String,
    val target: Int,
    val count: Int,
    val isCustom: Boolean,
) {
    val isCompleted: Boolean get() = count >= target
    val progress: Float get() = if (target <= 0) 0f else (count.toFloat() / target).coerceIn(0f, 1f)
}

/** Immutable snapshot the [TasbihScreen] renders. */
data class TasbihUiState(
    val dhikrs: List<TasbihDhikr> = emptyList(),
    val currentIndex: Int = 0,
) {
    val current: TasbihDhikr? get() = dhikrs.getOrNull(currentIndex)
}

/**
 * Owns the dhikr list + counts and persists them through [TasbehPreferences], staying
 * data-compatible with the legacy [TasbehFragment]. Not a Hilt VM because the preference
 * store needs a [Context]; created via [factory] with `viewModel(...)`.
 */
class TasbihViewModel(
    private val preferences: TasbehPreferences,
    private val isArabic: Boolean,
) : ViewModel() {

    private val _state = MutableStateFlow(TasbihUiState())
    val state: StateFlow<TasbihUiState> = _state.asStateFlow()

    init {
        val saved = preferences.getDhikrList()
        val items = if (saved.isNotEmpty()) saved else defaultDhikrs()
        var index = preferences.getCurrentDhikrIndex()
        if (index !in items.indices) index = 0
        _state.value = TasbihUiState(items.map { it.toUi() }, index)
        if (saved.isEmpty()) persist()
    }

    fun selectDhikr(index: Int) {
        val s = _state.value
        if (index == s.currentIndex || index !in s.dhikrs.indices) return
        _state.value = s.copy(currentIndex = index)
        preferences.saveCurrentDhikrIndex(index)
    }

    /** @return true if the count actually advanced (i.e. target not yet reached). */
    fun increment(): Boolean {
        val s = _state.value
        val current = s.current ?: return false
        if (current.count >= current.target) return false
        val updated = current.copy(count = current.count + 1)
        _state.value = s.copy(dhikrs = s.dhikrs.replaceAt(s.currentIndex, updated))
        persist()
        return true
    }

    fun resetCurrent() {
        val s = _state.value
        val current = s.current ?: return
        _state.value = s.copy(dhikrs = s.dhikrs.replaceAt(s.currentIndex, current.copy(count = 0)))
        persist()
    }

    fun resetAll() {
        val s = _state.value
        _state.value = s.copy(dhikrs = s.dhikrs.map { it.copy(count = 0) })
        persist()
    }

    fun addDhikr(text: String, target: Int) {
        val s = _state.value
        val newItem = TasbihDhikr(
            id = (s.dhikrs.maxOfOrNull { it.id } ?: -1) + 1,
            text = text,
            target = target,
            count = 0,
            isCustom = true,
        )
        val list = s.dhikrs + newItem
        _state.value = s.copy(dhikrs = list, currentIndex = list.lastIndex)
        preferences.saveCurrentDhikrIndex(list.lastIndex)
        persist()
    }

    fun deleteDhikr(index: Int) {
        val s = _state.value
        val target = s.dhikrs.getOrNull(index) ?: return
        if (!target.isCustom) return
        val list = s.dhikrs.toMutableList().apply { removeAt(index) }
        if (list.isEmpty()) {
            val defaults = defaultDhikrs().map { it.toUi() }
            _state.value = TasbihUiState(defaults, 0)
        } else {
            val newIndex = s.currentIndex.coerceAtMost(list.lastIndex)
            _state.value = TasbihUiState(list, newIndex)
            preferences.saveCurrentDhikrIndex(newIndex)
        }
        persist()
    }

    private fun persist() {
        preferences.saveDhikrList(_state.value.dhikrs.map { it.toEntity() })
    }

    private fun defaultDhikrs(): List<DhikrItem> = if (isArabic) {
        listOf(
            DhikrItem(0, "سبحان الله", 33, "#27AE60"),
            DhikrItem(1, "الحمد لله", 33, "#E67E22"),
            DhikrItem(2, "الله أكبر", 33, "#3498DB"),
            DhikrItem(3, "لا إله إلا الله", 33, "#9B59B6"),
        )
    } else {
        listOf(
            DhikrItem(0, "Subhan Allah", 33, "#27AE60"),
            DhikrItem(1, "Alhamdulillah", 33, "#E67E22"),
            DhikrItem(2, "Allahu Akbar", 33, "#3498DB"),
            DhikrItem(3, "La ilaha illa Allah", 33, "#9B59B6"),
        )
    }

    private fun DhikrItem.toUi() = TasbihDhikr(
        id = id,
        text = arabicText,
        target = targetCount,
        count = currentCount,
        isCustom = isCustom,
    )

    private fun TasbihDhikr.toEntity() = DhikrItem(
        id = id,
        arabicText = text,
        targetCount = target,
        color = "#2E7D32",
        currentCount = count,
        isCompleted = isCompleted,
        isCustom = isCustom,
    )

    private fun List<TasbihDhikr>.replaceAt(index: Int, value: TasbihDhikr): List<TasbihDhikr> =
        mapIndexed { i, item -> if (i == index) value else item }

    companion object {
        /** Builds a [TasbihViewModel] from the Android [Context] in [CreationExtras]. */
        fun factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val appContext = context.applicationContext
                return TasbihViewModel(
                    preferences = TasbehPreferences(appContext),
                    isArabic = LocaleHelper.isArabic(context),
                ) as T
            }
        }
    }
}
