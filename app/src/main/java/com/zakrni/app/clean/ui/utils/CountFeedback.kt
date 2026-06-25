package com.zakrni.app.clean.ui.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.zakrni.app.R

/**
 * Plays a short, pleasant "tick" for tap-to-count (tasbih / azkar / dua).
 *
 * Uses a bundled click sample via [SoundPool] (not `View.playSoundEffect`, which is silenced when
 * the device's system "touch sounds" setting is off, nor a harsh [android.media.ToneGenerator] beep).
 * Gated by the user's [ThemeManager.isCountSoundEnabled] preference.
 */
object CountFeedback {
    private var pool: SoundPool? = null
    private var soundId: Int = 0
    @Volatile private var loaded = false

    private fun ensure(context: Context) {
        if (pool != null) return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        pool = SoundPool.Builder().setMaxStreams(6).setAudioAttributes(attrs).build().also { sp ->
            sp.setOnLoadCompleteListener { _, _, status -> loaded = status == 0 }
            soundId = sp.load(context.applicationContext, R.raw.tick_count, 1)
        }
    }

    fun click(context: Context) {
        if (!ThemeManager.isCountSoundEnabled(context)) return
        ensure(context)
        val sp = pool ?: return
        if (loaded) sp.play(soundId, 0.8f, 0.8f, 1, 0, 1f)
    }
}
