package com.example.zakrni.clean.ui.utils

import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioPlayerManager @Inject constructor() {

    private var mediaPlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> get() = _isPlaying

    private val _currentProgress = MutableStateFlow(0f)
    val currentProgress: StateFlow<Float> get() = _currentProgress

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> get() = _duration

    private val _currentSurahNumber = MutableStateFlow(-1)
    val currentSurahNumber: StateFlow<Int> get() = _currentSurahNumber

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    fun playAudio(
        audioUrl: String,
        surahNumber: Int,
        onCompletion: () -> Unit,
        onError: (String) -> Unit
    ) {
        stop() // وقف أي تشغيل سابق
        _isLoading.value = true
        Log.d("AudioPlayerManager", "🔊 Starting audio from $audioUrl")

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                setOnPreparedListener {
                    _isLoading.value = false
                    _duration.value = it.duration
                    _isPlaying.value = true
                    _currentSurahNumber.value = surahNumber
                    it.start()
                    Log.d("AudioPlayerManager", "Playing Surah $surahNumber")
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    onCompletion()
                }
                setOnErrorListener { _, what, extra ->
                    _isPlaying.value = false
                    _isLoading.value = false
                    val errorMsg = "Error code: $what extra: $extra"
                    onError(errorMsg)
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            _isLoading.value = false
            _isPlaying.value = false
            Log.e("AudioPlayerManager", "Failed to play audio: ${e.message}")
            onError(e.message ?: "Unknown error")
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
                Log.d("AudioPlayerManager", "Audio paused")
            }
        }
    }

    fun resume() {
        mediaPlayer?.let {
            if (!_isPlaying.value) {
                it.start()
                _isPlaying.value = true
                Log.d("AudioPlayerManager", "Audio resumed")
            }
        }
    }

    fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        _isPlaying.value = false
        _currentSurahNumber.value = -1
        Log.d("AudioPlayerManager", "⏹️ Audio stopped")
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun isPlayingSurah(surahNumber: Int): Boolean {
        return _isPlaying.value && _currentSurahNumber.value == surahNumber
    }

    fun isCurrentSurah(surahNumber: Int): Boolean {
        return _currentSurahNumber.value == surahNumber
    }

    fun release() {
        stop()
    }
}
