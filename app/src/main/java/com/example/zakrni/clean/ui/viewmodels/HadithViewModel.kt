package com.example.zakrni.clean.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zakrni.clean.domain.usecase.GetHadithsUseCase
import com.example.zakrni.clean.ui.models.PresentationHadith
import com.example.zakrni.clean.ui.models.mapToPresentation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HadithViewModel @Inject constructor(
    private val getHadithsUseCase: GetHadithsUseCase
) : ViewModel() {

    // State for the list of hadiths (what the adapter needs)
    private val _hadithsList = MutableStateFlow<List<PresentationHadith>>(emptyList())
    val hadithsList: StateFlow<List<PresentationHadith>> = _hadithsList.asStateFlow()

    // Loading states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Pagination state
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _hasMorePages = MutableStateFlow(true)
    val hasMorePages: StateFlow<Boolean> = _hasMorePages.asStateFlow()

    private var isRequestInProgress = false

    init {
        loadHadiths() // Auto-load on creation
    }

    fun loadHadiths(page: Int = 1, limit: Int = 10) {
        if (isRequestInProgress) return

        viewModelScope.launch(Dispatchers.IO) {
            isRequestInProgress = true

            // Set loading state based on whether it's first page or pagination
            if (page == 1) {
                _isLoading.value = true
                _error.value = null
            } else {
                _isLoadingMore.value = true
            }

            try {
                val result = getHadithsUseCase(page, limit)

                result.onSuccess { domainResponse ->
                    val presentationResponse = domainResponse.mapToPresentation()

                    if (page == 1) {
                        // First page - replace all data
                        _hadithsList.value = presentationResponse.hadiths.data
                    } else {
                        // Pagination - append to existing data
                        val currentList = _hadithsList.value.toMutableList()
                        currentList.addAll(presentationResponse.hadiths.data)
                        _hadithsList.value = currentList
                    }

                    // Update pagination state using the correct model structure
                    _currentPage.value = presentationResponse.hadiths.currentPage
                    _hasMorePages.value = presentationResponse.hadiths.currentPage < presentationResponse.hadiths.lastPage
                    _error.value = null

                }.onFailure { exception ->
                    val errorMessage = exception.message ?: "حدث خطأ في تحميل الأحاديث"
                    _error.value = errorMessage
                }

            } catch (e: Exception) {
                _error.value = e.message ?: "حدث خطأ غير متوقع"
            } finally {
                _isLoading.value = false
                _isLoadingMore.value = false
                isRequestInProgress = false
            }
        }
    }

    fun loadNextPage() {
        val currentState = _currentPage.value
        val hasMore = _hasMorePages.value

        if (!isRequestInProgress && hasMore && !_isLoading.value && !_isLoadingMore.value) {
            loadHadiths(page = currentState + 1)
        }
    }

    fun refresh() {
        _currentPage.value = 1
        _hasMorePages.value = true
        _hadithsList.value = emptyList()
        _error.value = null
        loadHadiths(page = 1)
    }

    fun clearError() {
        _error.value = null
    }
}