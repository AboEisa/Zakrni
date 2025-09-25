// ui/viewmodels/VideosViewModel.kt
package com.example.zakrni.clean.ui.viewmodels

import PresentationVideo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zakrni.clean.domain.usecase.GetVideosUseCase
import com.example.zakrni.clean.ui.models.mapToPresentation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideosViewModel @Inject constructor(
    private val getVideosUseCase: GetVideosUseCase
) : ViewModel() {

    private val _videos = MutableLiveData<List<PresentationVideo>>()
    val videos: LiveData<List<PresentationVideo>> = _videos

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isPlaying = MutableLiveData<String?>()
    val isPlaying: LiveData<String?> = _isPlaying

    fun loadVideos(category: String? = null, query: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            getVideosUseCase.execute(1, category, query).fold(
                onSuccess = { response ->
                    _videos.value = response.videos.map { it.mapToPresentation() }
                },
                onFailure = { exception ->
                    _error.value = exception.message
                }
            )

            _isLoading.value = false
        }
    }

    fun playVideo(videoUrl: String) {
        _isPlaying.value = videoUrl
    }

    fun stopVideo() {
        _isPlaying.value = null
    }
}