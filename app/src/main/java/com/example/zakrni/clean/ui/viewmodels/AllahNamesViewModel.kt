package com.example.zakrni.clean.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zakrni.clean.domain.usecase.GetAllahNamesUseCase
import com.example.zakrni.clean.ui.models.PresentationAllahNameData
import com.example.zakrni.clean.ui.models.mapToPresentation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllahNamesViewModel @Inject constructor(
    private val getAllahNamesUseCase: GetAllahNamesUseCase
) : ViewModel() {

    // State for the list of Allah names
    private val _allahNamesList = MutableStateFlow<List<PresentationAllahNameData>>(emptyList())
    val allahNamesList: StateFlow<List<PresentationAllahNameData>> = _allahNamesList.asStateFlow()

    // State for loading indicator
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // State for error handling
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // State for empty list
    private val _isEmpty = MutableStateFlow(false)
    val isEmpty: StateFlow<Boolean> = _isEmpty.asStateFlow()

    init {
        fetchAllahNames()
    }

    private fun fetchAllahNames() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _errorMessage.value = null
            _isEmpty.value = false

            try {
                val result = getAllahNamesUseCase()
                result.onSuccess { response ->
                    val presentationList = response.data.map { it.mapToPresentation() }
                    _allahNamesList.value = presentationList
                    _isEmpty.value = presentationList.isEmpty()
                }.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to load Allah names"
                    _isEmpty.value = _allahNamesList.value.isEmpty()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                _isEmpty.value = _allahNamesList.value.isEmpty()
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun retryLoading() {
        fetchAllahNames()
    }










}