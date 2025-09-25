// ui/viewmodels/AudioViewModel.kt
package com.example.zakrni.clean.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zakrni.clean.domain.usecase.GetAudioLecturesUseCase
import com.example.zakrni.clean.ui.models.mapToPresentation
import com.example.zakrni.clean.ui.utils.AudioPlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val getAudioLecturesUseCase: GetAudioLecturesUseCase,
    private val audioPlayerManager: AudioPlayerManager
) : ViewModel() {

    private val _lectures = MutableLiveData<List<PresentationAudioLecture>>()
    val lectures: LiveData<List<PresentationAudioLecture>> = _lectures

    private val _currentPlaying = MutableLiveData<Int?>()
    val currentPlaying: LiveData<Int?> = _currentPlaying

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadLectures(sheikh: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true

            getAudioLecturesUseCase.execute(1, sheikh).fold(
                onSuccess = { response ->
                    _lectures.value = response.lectures.map { it.mapToPresentation() }
                },
                onFailure = { exception ->
                    _error.value = exception.message
                }
            )

            _isLoading.value = false
        }
    }

    fun playLecture(lecture: PresentationAudioLecture) {
        audioPlayerManager.playAudio(
            audioUrl = lecture.audioUrl,
            surahNumber = lecture.id,
            onCompletion = { playNext() },
            onError = { _error.value = it }
        )
        _currentPlaying.value = lecture.id
    }

    fun pauseAudio() {
        audioPlayerManager.pause()
    }

    fun resumeAudio() {
        audioPlayerManager.resume()
    }

    private fun playNext() {
        val currentIndex = _lectures.value?.indexOfFirst { it.id == _currentPlaying.value } ?: -1
        if (currentIndex != -1 && currentIndex < (_lectures.value?.size ?: 0) - 1) {
            _lectures.value?.get(currentIndex + 1)?.let { playLecture(it) }
        }
    }
}