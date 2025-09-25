package com.example.zakrni.clean.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zakrni.clean.domain.usecase.GetArticlesUseCase
import com.example.zakrni.clean.ui.models.PresentationArticleResponse
import com.example.zakrni.clean.ui.models.mapToPresentation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticlesViewModel @Inject constructor(
    private val getArticlesUseCase: GetArticlesUseCase
) : ViewModel() {

    private val _articleData = MutableStateFlow<PresentationArticleResponse?>(null)
    val articleData: StateFlow<PresentationArticleResponse?> = _articleData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadArticles()
    }

    fun loadArticles() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            getArticlesUseCase()
                .onSuccess { domainResponse ->
                    _articleData.value = domainResponse.mapToPresentation()
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "حدث خطأ في تحميل المقالات"
                    _isLoading.value = false
                }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun refresh() {
        loadArticles()
    }
}