package com.example.zakrni.clean.ui.utils

import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioPlayerManager @Inject constructor() {

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

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
        Log.d("AudioPlayerManager", "🎵 Attempting to play audio: $audioUrl")
        stop()
        _isLoading.value = true

        try {
            mediaPlayer = MediaPlayer().apply {
                Log.d("AudioPlayerManager", "📡 Setting data source...")
                setDataSource(audioUrl)

                setOnPreparedListener { mp ->
                    Log.d("AudioPlayerManager", "✅ Audio prepared successfully")
                    _isLoading.value = false
                    val audioDuration = mp.duration
                    _duration.value = audioDuration
                    _isPlaying.value = true
                    _currentSurahNumber.value = surahNumber
                    mp.start()
                    startProgressTracking()
                    Log.d("AudioPlayerManager", "▶️ Playing Surah $surahNumber, Duration: ${audioDuration}ms (${formatTime(audioDuration)})")
                }

                setOnCompletionListener {
                    Log.d("AudioPlayerManager", "🏁 Audio completed")
                    _isPlaying.value = false
                    stopProgressTracking()
                    onCompletion()
                }

                setOnErrorListener { _, what, extra ->
                    _isPlaying.value = false
                    _isLoading.value = false
                    stopProgressTracking()
                    val errorMsg = "MediaPlayer Error - What: $what, Extra: $extra"
                    Log.e("AudioPlayerManager", "❌ $errorMsg")
                    onError(errorMsg)
                    true
                }

                setOnInfoListener { _, what, extra ->
                    Log.d("AudioPlayerManager", "ℹ️ MediaPlayer Info - What: $what, Extra: $extra")
                    false
                }

                Log.d("AudioPlayerManager", "⏳ Starting async preparation...")
                prepareAsync()
            }
        } catch (e: Exception) {
            _isLoading.value = false
            _isPlaying.value = false
            val errorMsg = "Failed to initialize MediaPlayer: ${e.message}"
            Log.e("AudioPlayerManager", "💥 $errorMsg", e)
            onError(errorMsg)
        }
    }

    fun pause() {
        mediaPlayer?.let { mp ->
            try {
                if (mp.isPlaying) {
                    mp.pause()
                    _isPlaying.value = false
                    stopProgressTracking()
                    Log.d("AudioPlayerManager", "⏸️ Audio paused")
                } else {
                    Log.w("AudioPlayerManager", "⚠️ Pause called but audio not playing")
                }
            } catch (e: Exception) {
                Log.e("AudioPlayerManager", "❌ Error pausing: ${e.message}")
            }
        } ?: Log.w("AudioPlayerManager", "⚠️ Pause called but MediaPlayer is null")
    }

    fun resume() {
        mediaPlayer?.let { mp ->
            try {
                if (!_isPlaying.value && _currentSurahNumber.value != -1) {
                    mp.start()
                    _isPlaying.value = true
                    startProgressTracking()
                    Log.d("AudioPlayerManager", "▶️ Audio resumed")
                } else {
                    Log.w("AudioPlayerManager", "⚠️ Resume called but conditions not met")
                }
            } catch (e: Exception) {
                Log.e("AudioPlayerManager", "❌ Error resuming: ${e.message}")
            }
        } ?: Log.w("AudioPlayerManager", "⚠️ Resume called but MediaPlayer is null")
    }

    fun stop() {
        stopProgressTracking()
        mediaPlayer?.let { mp ->
            try {
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
                Log.d("AudioPlayerManager", "⏹️ MediaPlayer stopped and released")
            } catch (e: Exception) {
                Log.e("AudioPlayerManager", "❌ Error stopping MediaPlayer: ${e.message}")
            }
        }
        mediaPlayer = null
        _isPlaying.value = false
        _currentSurahNumber.value = -1
        _currentProgress.value = 0f
        _duration.value = 0
    }

    fun seekTo(position: Int) {
        mediaPlayer?.let { mp ->
            try {
                mp.seekTo(position)
                Log.d("AudioPlayerManager", "⏭️ Seeked to position: $position")
            } catch (e: Exception) {
                Log.e("AudioPlayerManager", "❌ Error seeking: ${e.message}")
            }
        }
    }

    fun isPlayingSurah(surahNumber: Int): Boolean {
        return _isPlaying.value && _currentSurahNumber.value == surahNumber
    }

    fun isCurrentSurah(surahNumber: Int): Boolean {
        return _currentSurahNumber.value == surahNumber
    }


    private fun startProgressTracking() {
        stopProgressTracking()

        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                mediaPlayer?.let { mp ->
                    try {
                        if (mp.isPlaying) {
                            val currentPosition = mp.currentPosition
                            val duration = mp.duration

                            if (duration > 0) {
                                val progress = (currentPosition * 100f) / duration
                                _currentProgress.value = progress
                                _duration.value = duration

                                Log.v("AudioPlayerManager", "📊 Progress: ${progress.toInt()}% (${formatTime(currentPosition)}/${formatTime(duration)})")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("AudioPlayerManager", "❌ Error tracking progress: ${e.message}")
                        _isPlaying.value = false
                        stopProgressTracking()
                    }
                }
                delay(1000)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }


    fun getCurrentPosition(): Int {
        return try {
            mediaPlayer?.currentPosition ?: 0
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "❌ Error getting current position: ${e.message}")
            0
        }
    }


    fun getDuration(): Int {
        return try {
            val duration = mediaPlayer?.duration ?: _duration.value
            Log.d("AudioPlayerManager", "⏱️ Duration requested: $duration ms")
            duration
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "❌ Error getting duration: ${e.message}")
            _duration.value
        }
    }

    private fun formatTime(milliseconds: Int): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }

    fun release() {
        Log.d("AudioPlayerManager", "🧹 Releasing AudioPlayerManager")
        stop()
        scope.cancel()
    }
}