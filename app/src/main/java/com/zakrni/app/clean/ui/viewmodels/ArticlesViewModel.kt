package com.zakrni.app.clean.ui.viewmodels

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zakrni.app.clean.data.models.IslamicVideos
import com.zakrni.app.clean.data.models.YouTubeChannel
import com.zakrni.app.clean.data.models.YouTubeVideo
import com.zakrni.app.clean.domain.usecase.GetIslamicVideosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticlesViewModel @Inject constructor(
    private val getIslamicVideosUseCase: GetIslamicVideosUseCase
) : ViewModel() {

    private val _videos = MutableStateFlow<List<YouTubeVideo>>(emptyList())
    val videos: StateFlow<List<YouTubeVideo>> = _videos.asStateFlow()

    private val _channels = MutableStateFlow<List<YouTubeChannel>>(IslamicVideos.channels)
    val channels: StateFlow<List<YouTubeChannel>> = _channels.asStateFlow()

    private val _selectedChannel = MutableStateFlow<YouTubeChannel?>(null)
    val selectedChannel: StateFlow<YouTubeChannel?> = _selectedChannel.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private var currentContentType: String = "all"

    init {
        android.util.Log.d("ArticlesViewModel", "ViewModel initialized")
    }

    fun loadVideosByType(contentType: String) {
        currentContentType = contentType
        viewModelScope.launch {
            val isArabic = isArabicUi()
            android.util.Log.d("ArticlesViewModel", "loadVideosByType: $contentType")
            _isLoading.value = true
            _error.value = null

            getIslamicVideosUseCase.getVideosByType(contentType, isArabic)
                .onSuccess { videoList ->
                    android.util.Log.d("ArticlesViewModel", "Success: loaded ${videoList.size} videos for $contentType")
                    _videos.value = videoList
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    android.util.Log.e("ArticlesViewModel", "Error: ${exception.message}")
                    _error.value = exception.message
                        ?: localizedText("حدث خطأ في تحميل الفيديوهات", "Failed to load videos")
                    _isLoading.value = false
                }
        }
    }

    fun loadVideos() {
        loadVideosByType(currentContentType)
    }

    fun loadChannelVideos(channel: YouTubeChannel) {
        viewModelScope.launch {
            val isArabic = isArabicUi()
            _isLoading.value = true
            _error.value = null
            _selectedChannel.value = channel

            val channelQuery = if (isArabic) channel.nameAr else channel.name
            getIslamicVideosUseCase.getChannelVideos(channelQuery, isArabic)
                .onSuccess { videoList ->
                    _videos.value = videoList
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _error.value = exception.message
                        ?: localizedText("حدث خطأ في تحميل الفيديوهات", "Failed to load videos")
                    _isLoading.value = false
                }
        }
    }

    fun searchVideos(query: String) {
        if (query.isBlank()) {
            loadVideos()
            return
        }

        viewModelScope.launch {
            val isArabic = isArabicUi()
            _isLoading.value = true
            _error.value = null

            getIslamicVideosUseCase.search(query, isArabic)
                .onSuccess { videoList ->
                    _videos.value = videoList
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: localizedText("حدث خطأ في البحث", "Search failed")
                    _isLoading.value = false
                }
        }
    }

    fun clearChannelFilter() {
        _selectedChannel.value = null
        loadVideos()
    }

    fun clearError() {
        _error.value = null
    }

    fun refresh() {
        if (_selectedChannel.value != null) {
            loadChannelVideos(_selectedChannel.value!!)
        } else {
            loadVideos()
        }
    }

    private fun isArabicUi(): Boolean {
        val appLocale = AppCompatDelegate.getApplicationLocales()[0]
        val language = appLocale?.language ?: "en"
        return language.equals("ar", ignoreCase = true)
    }

    private fun localizedText(arabic: String, english: String): String {
        return if (isArabicUi()) arabic else english
    }
}
