// ui/viewmodels/ArticlesViewModel.kt
package com.example.zakrni.clean.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zakrni.clean.domain.usecase.GetArticlesUseCase
import com.example.zakrni.clean.ui.models.PresentationArticle
import com.example.zakrni.clean.ui.models.mapToPresentation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticlesViewModel @Inject constructor(
    private val getArticlesUseCase: GetArticlesUseCase
) : ViewModel() {

    private val _articles = MutableLiveData<List<PresentationArticle>>()
    val articles: LiveData<List<PresentationArticle>> = _articles

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _hasMorePages = MutableLiveData<Boolean>(true)
    val hasMorePages: LiveData<Boolean> = _hasMorePages

    private var currentPage = 1
    private var currentCategory: String? = null
    private val allArticles = mutableListOf<PresentationArticle>()

    fun loadArticles(category: String? = null, refresh: Boolean = false) {
        if (refresh) {
            currentPage = 1
            allArticles.clear()
            currentCategory = category
        }

        if (!_hasMorePages.value!! && !refresh) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            getArticlesUseCase.execute(currentPage, currentCategory).fold(
                onSuccess = { response ->
                    val presentationArticles = response.articles.map { it.mapToPresentation() }
                    allArticles.addAll(presentationArticles)
                    _articles.value = allArticles
                    _hasMorePages.value = currentPage < response.pagination.lastPage
                    currentPage++
                },
                onFailure = { exception ->
                    _error.value = exception.message
                }
            )

            _isLoading.value = false
        }
    }

    fun refreshArticles() {
        loadArticles(currentCategory, true)
    }

    fun clearError() {
        _error.value = null
    }
}