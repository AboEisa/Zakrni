package com.example.zakrni.clean.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zakrni.clean.domain.usecase.GetDuasUseCase
import com.example.zakrni.clean.ui.models.PresentationDuaResponse
import com.example.zakrni.clean.ui.models.mapToPresentation
import com.example.zakrni.clean.ui.utils.DuaType
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

    // UI State for Dua data
    private val _duaData = MutableStateFlow<PresentationDuaResponse?>(null)
    val duaData: StateFlow<PresentationDuaResponse?> = _duaData.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Expansion states for different sections
    private val _expandedSections = MutableStateFlow<Set<DuaType>>(emptySet())
    val expandedSections: StateFlow<Set<DuaType>> = _expandedSections.asStateFlow()

//    init {
//        loadDuas()
//    }

    /**
     * Loads Dua data from the use case
     */
//    fun loadDuas() {
//        viewModelScope.launch {
//            _isLoading.value = true
//            _error.value = null
//
//            try {
//                val result = getDuasUseCase()
//                result.onSuccess { domainResponse ->
//                    // Map domain response to presentation
//                    _duaData.value = domainResponse.mapToPresentation()
//                }.onFailure { exception ->
//                    _error.value = exception.message ?: "Unknown error occurred"
//                }
//            } catch (e: Exception) {
//                _error.value = e.message ?: "Failed to load Duas"
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }

    /**
     * Toggles the expansion state of a specific dua section
     */
    fun toggleSection(duaType: DuaType) {
        val currentExpanded = _expandedSections.value.toMutableSet()
        if (currentExpanded.contains(duaType)) {
            currentExpanded.remove(duaType)
        } else {
            currentExpanded.add(duaType)
        }
        _expandedSections.value = currentExpanded
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
//    fun refresh() {
//        loadDuas()
//    }
}