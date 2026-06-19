package com.zakrni.app.clean.ui.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zakrni.app.clean.domain.models.DomainHisnDua
import com.zakrni.app.clean.domain.usecase.GetAzkarUseCase
import com.zakrni.app.clean.domain.usecase.GetDuasUseCase


import com.zakrni.app.clean.ui.models.PresentationAzkarResponse
import com.zakrni.app.clean.ui.models.mapToPresentation
import com.zakrni.app.clean.ui.utils.AzkarType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AzkarViewModel @Inject constructor(
    private val getAzkarUseCase: GetAzkarUseCase,
    private val getDuasUseCase: GetDuasUseCase
) : ViewModel() {
    companion object {
        // hisn.json currently has ~300 items; keep headroom to avoid missing sections.
        private const val HISN_FETCH_LIMIT = 2000
    }

    private val _azkarData = MutableStateFlow<PresentationAzkarResponse?>(null)
    val azkarData: StateFlow<PresentationAzkarResponse?> = _azkarData.asStateFlow()

    // Hisn أذكار sections (from Duas API)
    private val _hisnAzkarSections = MutableStateFlow<Map<String, List<DomainHisnDua>>>(emptyMap())
    val hisnAzkarSections: StateFlow<Map<String, List<DomainHisnDua>>> = _hisnAzkarSections.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _expandedSections = MutableStateFlow<Set<AzkarType>>(emptySet())
    val expandedSections: StateFlow<Set<AzkarType>> = _expandedSections.asStateFlow()

    // Track expanded Hisn sections by name
    private val _expandedHisnSections = MutableStateFlow<Set<String>>(emptySet())
    val expandedHisnSections: StateFlow<Set<String>> = _expandedHisnSections.asStateFlow()

    init {
        loadAzkar()
        loadHisnAzkar()
    }

    fun loadAzkar() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = getAzkarUseCase()
                result.onSuccess { combinedResponse ->
                    val presentationResponse = combinedResponse.mapToPresentation()
                    _azkarData.value = presentationResponse

                    if (combinedResponse.hasAnyData() && combinedResponse.failures.isNotEmpty()) {
                        _error.value = "Some sections failed to load"
                    }
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load Azkar data"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadHisnAzkar() {
        viewModelScope.launch {
            try {
                val result = getDuasUseCase.getAllDuas(page = 1, limit = HISN_FETCH_LIMIT)
                result.onSuccess { response ->
                    // Keep only sections that contain "أذكار" (these were removed from Duas page)
                    // Exclude "أذكار الصباح والمساء" since we already have them separately
                    val azkarSections = response.duas
                        .filter { dua ->
                            val isAzkarSection = dua.section.contains("أذكار") ||
                                (dua.sectionEnglish?.contains("Azkar", ignoreCase = true) == true)
                            val isMorningEvening = dua.section == "أذكار الصباح والمساء" ||
                                (dua.sectionEnglish?.contains("morning", ignoreCase = true) == true &&
                                    dua.sectionEnglish.contains("evening", ignoreCase = true))
                            isAzkarSection && !isMorningEvening
                        }
                        .groupBy { it.section }
                    _hisnAzkarSections.value = azkarSections
                }
            } catch (_: Exception) {
                // Silently fail - these are bonus sections
            }
        }
    }

    fun toggleSection(azkarType: AzkarType) {
        val currentExpanded = _expandedSections.value.toMutableSet()
        if (currentExpanded.contains(azkarType)) {
            currentExpanded.remove(azkarType)
        } else {
            currentExpanded.add(azkarType)
        }
        _expandedSections.value = currentExpanded
    }

    fun toggleHisnSection(sectionName: String) {
        val currentExpanded = _expandedHisnSections.value.toMutableSet()
        if (currentExpanded.contains(sectionName)) {
            currentExpanded.remove(sectionName)
        } else {
            currentExpanded.add(sectionName)
        }
        _expandedHisnSections.value = currentExpanded
    }

    fun clearError() {
        _error.value = null
    }
    fun refresh() {
        loadAzkar()
        loadHisnAzkar()
    }


}
