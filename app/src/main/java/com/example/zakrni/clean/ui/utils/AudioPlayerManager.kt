package com.example.zakrni.clean.ui.utils

import android.content.Context
import android.media.AudioAttributes
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
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentProgress = MutableStateFlow(0f)
    val currentProgress: StateFlow<Float> = _currentProgress

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration

    fun playAudio(audioUrl: String, onCompletion: () -> Unit = {}) {
        try {
            release()

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                setDataSource(audioUrl)
                prepareAsync()

                setOnPreparedListener {
                    start()
                    _isPlaying.value = true
                    _duration.value = duration
                }

                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentProgress.value = 0f
                    onCompletion()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e("AudioPlayer", "Error: what=$what, extra=$extra")
                    _isPlaying.value = false
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error playing audio: ${e.message}")
        }
    }

    fun pause() {
        mediaPlayer?.pause()
        _isPlaying.value = false
    }

    fun resume() {
        mediaPlayer?.start()
        _isPlaying.value = true
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.value = false
        _currentProgress.value = 0f
    }

    fun updateProgress() {
        mediaPlayer?.let {
            if (it.duration > 0) {
                _currentProgress.value = (it.currentPosition.toFloat() / it.duration) * 100
            }
        }
    }
}