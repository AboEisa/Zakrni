package com.zakrni.app.clean.ui.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AudioControlReceiver : BroadcastReceiver() {

    @Inject
    lateinit var audioPlayerManager: AudioPlayerManager

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AudioControlReceiver", "📥 Received action: ${intent.action}")
        
        when (intent.action) {
            AudioPlayerManager.ACTION_PLAY_PAUSE -> {
                if (audioPlayerManager.isPlaying.value) {
                    audioPlayerManager.pause()
                } else {
                    audioPlayerManager.resume()
                }
            }
            AudioPlayerManager.ACTION_PLAY -> {
                audioPlayerManager.resume()
            }
            AudioPlayerManager.ACTION_PAUSE -> {
                audioPlayerManager.pause()
            }
            AudioPlayerManager.ACTION_STOP -> {
                audioPlayerManager.stop()
            }
            AudioPlayerManager.ACTION_NEXT -> {
                audioPlayerManager.onNextSurah?.invoke()
            }
            AudioPlayerManager.ACTION_PREVIOUS -> {
                audioPlayerManager.onPreviousSurah?.invoke()
            }
        }
    }
}
