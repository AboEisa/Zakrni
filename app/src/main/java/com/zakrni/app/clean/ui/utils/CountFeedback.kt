package com.zakrni.app.clean.ui.utils

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator

/**
 * Plays a short, reliable click for tap-to-count (tasbih / azkar / dua).
 *
 * Uses [ToneGenerator] on the music stream instead of `View.playSoundEffect`, because the latter
 * is silenced whenever the device's system "touch sounds" setting is off. Gated by the user's
 * [ThemeManager.isCountSoundEnabled] preference.
 */
object CountFeedback {
    @Volatile private var tone: ToneGenerator? = null

    fun click(context: Context) {
        if (!ThemeManager.isCountSoundEnabled(context)) return
        try {
            val gen = tone ?: ToneGenerator(AudioManager.STREAM_MUSIC, 80).also { tone = it }
            gen.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        } catch (_: Exception) {
            // Audio resource may be briefly unavailable; drop this click and reset.
            tone = null
        }
    }
}
