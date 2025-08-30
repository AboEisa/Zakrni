package com.example.zakrni.clean.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zakrni.clean.domain.usecase.GetAzkarUseCase
import com.example.zakrni.clean.ui.models.PresentationAzkarResponse
import com.example.zakrni.clean.ui.models.mapToPresentation
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

    // UI State for Azkar data
    private val _azkarData = MutableStateFlow<PresentationAzkarResponse?>(null)
    val azkarData: StateFlow<PresentationAzkarResponse?> = _azkarData.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Expansion states for different sections
    private val _morningExpanded = MutableStateFlow(false)
    val morningExpanded: StateFlow<Boolean> = _morningExpanded.asStateFlow()

    private val _eveningExpanded = MutableStateFlow(false)
    val eveningExpanded: StateFlow<Boolean> = _eveningExpanded.asStateFlow()

    private val _sleepExpanded = MutableStateFlow(false)
    val sleepExpanded: StateFlow<Boolean> = _sleepExpanded.asStateFlow()

    private val _wakeUpExpanded = MutableStateFlow(false)
    val wakeUpExpanded: StateFlow<Boolean> = _wakeUpExpanded.asStateFlow()

    private val _prayerExpanded = MutableStateFlow(false)
    val prayerExpanded: StateFlow<Boolean> = _prayerExpanded.asStateFlow()

    init {
        loadAzkar()
    }

    /**
     * Loads Azkar data from the use case
     */
    fun loadAzkar() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = getAzkarUseCase()
                result.onSuccess { domainResponse ->
                    // Map domain response to presentation
                    _azkarData.value = domainResponse.mapToPresentation()
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Unknown error occurred"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load Azkar"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Toggles the expansion state of morning azkar section
     */
    fun toggleMorningSection() {
        _morningExpanded.value = !_morningExpanded.value
    }

    /**
     * Toggles the expansion state of evening azkar section
     */
    fun toggleEveningSection() {
        _eveningExpanded.value = !_eveningExpanded.value
    }

    /**
     * Toggles the expansion state of sleep azkar section
     */
    fun toggleSleepSection() {
        _sleepExpanded.value = !_sleepExpanded.value
    }

    /**
     * Toggles the expansion state of wake up azkar section
     */
    fun toggleWakeUpSection() {
        _wakeUpExpanded.value = !_wakeUpExpanded.value
    }

    /**
     * Toggles the expansion state of prayer azkar section
     */
    fun togglePrayerSection() {
        _prayerExpanded.value = !_prayerExpanded.value
    }

    /**
     * Clears any error state
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Refreshes the azkar data
     */
    fun refresh() {
        loadAzkar()
    }
}