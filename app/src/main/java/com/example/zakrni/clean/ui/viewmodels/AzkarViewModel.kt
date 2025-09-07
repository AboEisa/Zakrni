package com.example.zakrni.clean.ui.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zakrni.clean.domain.usecase.GetAzkarUseCase


import com.example.zakrni.clean.ui.models.PresentationAzkarResponse
import com.example.zakrni.clean.ui.models.mapToPresentation
import com.example.zakrni.clean.ui.utils.AzkarType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AzkarViewModel @Inject constructor(
    private val getAzkarUseCase: GetAzkarUseCase
) : ViewModel() {

    private val _azkarData = MutableStateFlow<PresentationAzkarResponse?>(null)
    val azkarData: StateFlow<PresentationAzkarResponse?> = _azkarData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _expandedSections = MutableStateFlow<Set<AzkarType>>(emptySet())
    val expandedSections: StateFlow<Set<AzkarType>> = _expandedSections.asStateFlow()

    init {
        loadAzkar()
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

    fun toggleSection(azkarType: AzkarType) {
        val currentExpanded = _expandedSections.value.toMutableSet()
        if (currentExpanded.contains(azkarType)) {
            currentExpanded.remove(azkarType)
        } else {
            currentExpanded.add(azkarType)
        }
        _expandedSections.value = currentExpanded
    }
    fun clearError() {
        _error.value = null
    }
    fun refresh() {
        loadAzkar()
    }


}