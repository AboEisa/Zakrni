package com.zakrni.app.clean.ui.viewmodels

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zakrni.app.clean.domain.models.DomainHisnDua
import com.zakrni.app.clean.domain.models.DomainHisnSection
import com.zakrni.app.clean.domain.usecase.GetDuasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DuasViewModel @Inject constructor(
    private val getDuasUseCase: GetDuasUseCase
) : ViewModel() {

    // Sections state
    private val _sections = MutableStateFlow<List<DomainHisnSection>>(emptyList())
    val sections: StateFlow<List<DomainHisnSection>> = _sections.asStateFlow()

    // Current section duas
    private val _duas = MutableStateFlow<List<DomainHisnDua>>(emptyList())
    val duas: StateFlow<List<DomainHisnDua>> = _duas.asStateFlow()

    // Selected section
    private val _selectedSection = MutableStateFlow<String?>(null)
    val selectedSection: StateFlow<String?> = _selectedSection.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Expansion states for sections
    private val _expandedSections = MutableStateFlow<Set<String>>(emptySet())
    val expandedSections: StateFlow<Set<String>> = _expandedSections.asStateFlow()

    // Map of section name to its duas
    private val _sectionDuasMap = MutableStateFlow<Map<String, List<DomainHisnDua>>>(emptyMap())
    val sectionDuasMap: StateFlow<Map<String, List<DomainHisnDua>>> = _sectionDuasMap.asStateFlow()

    init {
        loadDuas()
    }

    /**
     * Load all duas grouped by sections
     */
    fun loadDuas() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = getDuasUseCase.getAllDuas(page = 1, limit = 500)
                result.onSuccess { response ->
                    // Filter out all أذكار sections (shown in Azkar page, not Duas page)
                    val groupedDuas = response.duas
                        .filter { !it.section.contains("أذكار") }
                        .groupBy { it.section }
                    _sectionDuasMap.value = groupedDuas
                    
                    // Create sections from grouped data
                    val sectionsList = groupedDuas.map { (name, duasList) ->
                        DomainHisnSection(
                            name = name,
                            count = duasList.size,
                            englishName = duasList.firstOrNull()?.sectionEnglish
                        )
                    }
                    _sections.value = sectionsList
                    _duas.value = response.duas
                }.onFailure { exception ->
                    _error.value = exception.message
                        ?: localizedText("فشل في تحميل الأدعية", "Failed to load supplications")
                }
            } catch (e: Exception) {
                _error.value = e.message ?: localizedText("حدث خطأ غير متوقع", "An unexpected error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Toggle section expansion
     */
    fun toggleSection(sectionName: String) {
        val currentExpanded = _expandedSections.value.toMutableSet()
        if (currentExpanded.contains(sectionName)) {
            currentExpanded.remove(sectionName)
        } else {
            currentExpanded.add(sectionName)
        }
        _expandedSections.value = currentExpanded
    }

    /**
     * Get duas for a specific section
     */
    fun getDuasForSection(sectionName: String): List<DomainHisnDua> {
        return _sectionDuasMap.value[sectionName] ?: emptyList()
    }

    /**
     * Clears any error state
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Refreshes the dua data
     */
    fun refresh() {
        loadDuas()
    }

    private fun localizedText(arabic: String, english: String): String {
        val appLocale = AppCompatDelegate.getApplicationLocales()[0]
        val language = appLocale?.language ?: "en"
        return if (language.equals("ar", ignoreCase = true)) arabic else english
    }
}
