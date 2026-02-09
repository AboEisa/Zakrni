package com.zakrni.app.clean.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.zakrni.app.R
import com.zakrni.app.clean.ui.utils.LocaleHelper
import com.zakrni.app.clean.ui.views.HomeActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AudioPlaybackService : Service(), AudioManager.OnAudioFocusChangeListener {

    companion object {
        const val CHANNEL_ID = "quran_audio_playback_channel"
        const val NOTIFICATION_ID = 2001
        private const val TAG = "AudioPlaybackService"

        const val ACTION_PLAY = "com.zakrni.app.audio.PLAY"
        const val ACTION_PAUSE = "com.zakrni.app.audio.PAUSE"
        const val ACTION_STOP = "com.zakrni.app.audio.STOP"
        const val ACTION_NEXT = "com.zakrni.app.audio.NEXT"
        const val ACTION_PREVIOUS = "com.zakrni.app.audio.PREVIOUS"
        const val ACTION_PLAY_PAUSE = "com.zakrni.app.audio.PLAY_PAUSE"
        
        const val EXTRA_AUDIO_URL = "audio_url"
        const val EXTRA_SURAH_NAME = "surah_name"
        const val EXTRA_SURAH_NUMBER = "surah_number"
        const val EXTRA_RECITER_NAME = "reciter_name"

        private var instance: AudioPlaybackService? = null
        
        fun getInstance(): AudioPlaybackService? = instance
        
        fun startPlayback(
            context: Context,
            audioUrl: String,
            surahName: String,
            surahNumber: Int,
            reciterName: String
        ) {
            val intent = Intent(context, AudioPlaybackService::class.java).apply {
                action = ACTION_PLAY
                putExtra(EXTRA_AUDIO_URL, audioUrl)
                putExtra(EXTRA_SURAH_NAME, surahName)
                putExtra(EXTRA_SURAH_NUMBER, surahNumber)
                putExtra(EXTRA_RECITER_NAME, reciterName)
            }
            context.startForegroundService(intent)
        }
        
        fun stopPlayback(context: Context) {
            val intent = Intent(context, AudioPlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private var mediaPlayer: MediaPlayer? = null
    private var mediaSession: MediaSessionCompat? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null
    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> get() = _isPlaying

    // Progress as current position in milliseconds (not fraction)
    private val _currentProgress = MutableStateFlow(0)
    val currentProgress: StateFlow<Int> get() = _currentProgress

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> get() = _duration

    private val _currentSurahNumber = MutableStateFlow(-1)
    val currentSurahNumber: StateFlow<Int> get() = _currentSurahNumber

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private var currentSurahName: String = ""
    private var currentReciterName: String = ""
    private var currentAudioUrl: String = ""

    // Callbacks for navigation
    var onNextSurah: (() -> Unit)? = null
    var onPreviousSurah: (() -> Unit)? = null
    var onPlaybackCompleted: (() -> Unit)? = null

    private val binder = AudioBinder()

    inner class AudioBinder : Binder() {
        fun getService(): AudioPlaybackService = this@AudioPlaybackService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        instance = this
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
        setupMediaSession()
        registerAudioControlReceiver()
        Log.d(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val audioUrl = intent.getStringExtra(EXTRA_AUDIO_URL) ?: ""
                val surahName = intent.getStringExtra(EXTRA_SURAH_NAME) ?: ""
                val surahNumber = intent.getIntExtra(EXTRA_SURAH_NUMBER, -1)
                val reciterName = intent.getStringExtra(EXTRA_RECITER_NAME) ?: ""
                
                if (audioUrl.isNotEmpty()) {
                    playAudio(audioUrl, surahName, surahNumber, reciterName)
                }
            }
            ACTION_PAUSE -> pause()
            ACTION_PLAY_PAUSE -> {
                if (_isPlaying.value) pause() else resume()
            }
            ACTION_STOP -> {
                stop()
                stopSelf()
            }
            ACTION_NEXT -> onNextSurah?.invoke()
            ACTION_PREVIOUS -> onPreviousSurah?.invoke()
        }
        
        return START_STICKY // Service will be restarted if killed
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                localizedText("تشغيل القرآن الكريم", "Quran playback"),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = localizedText(
                    "إشعارات تشغيل القرآن الكريم",
                    "Quran playback notifications"
                )
                setShowBadge(false)
                setSound(null, null)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupMediaSession() {
        mediaSession = MediaSessionCompat(this, "QuranAudioService").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() { resume() }
                override fun onPause() { pause() }
                override fun onStop() { stop() }
                override fun onSkipToNext() { onNextSurah?.invoke() }
                override fun onSkipToPrevious() { onPreviousSurah?.invoke() }
                override fun onSeekTo(pos: Long) { seekTo(pos.toInt()) }
            })
            isActive = true
        }
    }

    private fun requestAudioFocus(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this)
                .build()

            val result = audioManager.requestAudioFocus(audioFocusRequest!!)
            hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            val result = audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
        return hasAudioFocus
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(this)
        }
        hasAudioFocus = false
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                pause()
                abandonAudioFocus()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer?.setVolume(0.3f, 0.3f)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.setVolume(1.0f, 1.0f)
                if (!_isPlaying.value && mediaPlayer != null) {
                    resume()
                }
            }
        }
    }

    fun playAudio(audioUrl: String, surahName: String, surahNumber: Int, reciterName: String) {
        serviceScope.launch {
            _isLoading.value = true
            _currentSurahNumber.value = surahNumber
            currentSurahName = surahName
            currentReciterName = reciterName
            currentAudioUrl = audioUrl

            stop()

            if (!requestAudioFocus()) {
                Log.w(TAG, "Failed to get audio focus")
                _isLoading.value = false
                return@launch
            }

            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(audioUrl)
                    prepareAsync()

                    setOnPreparedListener { mp ->
                        _duration.value = mp.duration
                        _isLoading.value = false
                        mp.start()
                        _isPlaying.value = true
                        startProgressTracking()
                        updateNotification()
                        updateMediaSessionState(PlaybackStateCompat.STATE_PLAYING)
                        Log.d(TAG, "Started playing: $surahName")
                    }

                    setOnCompletionListener {
                        _isPlaying.value = false
                        _currentProgress.value = _duration.value
                        stopProgressTracking()
                        updateMediaSessionState(PlaybackStateCompat.STATE_STOPPED)
                        onPlaybackCompleted?.invoke()
                        updateNotification()
                    }

                    setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "MediaPlayer Error - What: $what, Extra: $extra")
                        _isLoading.value = false
                        _isPlaying.value = false
                        true
                    }
                }
                
                // Start foreground immediately with loading notification
                startForeground(NOTIFICATION_ID, buildNotification())
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize MediaPlayer: ${e.message}")
                _isLoading.value = false
            }
        }
    }

    fun pause() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.pause()
                _isPlaying.value = false
                stopProgressTracking()
                updateNotification()
                updateMediaSessionState(PlaybackStateCompat.STATE_PAUSED)
            }
        }
    }

    fun resume() {
        if (!hasAudioFocus && !requestAudioFocus()) {
            return
        }
        
        mediaPlayer?.let { mp ->
            if (!mp.isPlaying) {
                mp.start()
                _isPlaying.value = true
                startProgressTracking()
                updateNotification()
                updateMediaSessionState(PlaybackStateCompat.STATE_PLAYING)
            }
        }
    }

    fun stop() {
        progressJob?.cancel()
        mediaPlayer?.let { mp ->
            try {
                if (mp.isPlaying) mp.stop()
                mp.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping MediaPlayer: ${e.message}")
            }
        }
        mediaPlayer = null
        _isPlaying.value = false
        _currentProgress.value = 0
        _currentSurahNumber.value = -1
        abandonAudioFocus()
        updateMediaSessionState(PlaybackStateCompat.STATE_STOPPED)
    }

    fun seekTo(position: Int) {
        mediaPlayer?.let { mp ->
            mp.seekTo(position)
            _currentProgress.value = position
            updateMediaSessionState(if (_isPlaying.value) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED)
        }
    }

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0
    fun getDurationMs(): Int = mediaPlayer?.duration ?: 0

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = serviceScope.launch {
            while (isActive) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying && mp.duration > 0) {
                        _currentProgress.value = mp.currentPosition
                    }
                }
                delay(250) // Update 4x per second for smooth seekbar
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseAction = if (_isPlaying.value) {
            NotificationCompat.Action.Builder(
                R.drawable.ic_pause,
                localizedText("إيقاف مؤقت", "Pause"),
                getPendingIntent(ACTION_PAUSE)
            ).build()
        } else {
            NotificationCompat.Action.Builder(
                R.drawable.ic_play,
                localizedText("تشغيل", "Play"),
                getPendingIntent(ACTION_PLAY_PAUSE)
            ).build()
        }

        val previousAction = NotificationCompat.Action.Builder(
            R.drawable.ic_previous,
            localizedText("السابقة", "Previous"),
            getPendingIntent(ACTION_PREVIOUS)
        ).build()

        val nextAction = NotificationCompat.Action.Builder(
            R.drawable.ic_next,
            localizedText("التالية", "Next"),
            getPendingIntent(ACTION_NEXT)
        ).build()

        val stopAction = NotificationCompat.Action.Builder(
            R.drawable.ic_close,
            localizedText("إيقاف", "Stop"),
            getPendingIntent(ACTION_STOP)
        ).build()

        val title = if (_isLoading.value) {
            localizedText("جاري التحميل...", "Loading...")
        } else {
            currentSurahName
        }
        val subtitle = if (_isLoading.value) "" else currentReciterName

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setSmallIcon(R.drawable.ic_quran)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_quran))
            .setContentIntent(pendingIntent)
            .setOngoing(_isPlaying.value)
            .setShowWhen(false)
            .addAction(previousAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .addAction(stopAction)
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession?.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, AudioPlaybackService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun updateMediaSessionState(state: Int) {
        val position = mediaPlayer?.currentPosition?.toLong() ?: 0L
        val playbackState = PlaybackStateCompat.Builder()
            .setState(state, position, 1.0f)
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SEEK_TO or
                PlaybackStateCompat.ACTION_STOP
            )
            .build()
        mediaSession?.setPlaybackState(playbackState)

        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentSurahName)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentReciterName)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, _duration.value.toLong())
            .build()
        mediaSession?.setMetadata(metadata)
    }

    private val audioControlReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_PLAY, ACTION_PLAY_PAUSE -> {
                    if (_isPlaying.value) pause() else resume()
                }
                ACTION_PAUSE -> pause()
                ACTION_STOP -> {
                    stop()
                    stopSelf()
                }
                ACTION_NEXT -> onNextSurah?.invoke()
                ACTION_PREVIOUS -> onPreviousSurah?.invoke()
            }
        }
    }

    private fun registerAudioControlReceiver() {
        val filter = IntentFilter().apply {
            addAction(ACTION_PLAY)
            addAction(ACTION_PAUSE)
            addAction(ACTION_STOP)
            addAction(ACTION_NEXT)
            addAction(ACTION_PREVIOUS)
            addAction(ACTION_PLAY_PAUSE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(audioControlReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(audioControlReceiver, filter)
        }
    }

    private fun localizedText(arabic: String, english: String): String {
        return if (LocaleHelper.isArabic(this)) arabic else english
    }

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")
        instance = null
        stop()
        progressJob?.cancel()
        serviceScope.cancel()
        mediaSession?.release()
        try {
            unregisterReceiver(audioControlReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver: ${e.message}")
        }
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Keep playing when app is removed from recent apps
        // Only stop if explicitly requested
        Log.d(TAG, "Task removed, continuing playback")
        super.onTaskRemoved(rootIntent)
    }
}
