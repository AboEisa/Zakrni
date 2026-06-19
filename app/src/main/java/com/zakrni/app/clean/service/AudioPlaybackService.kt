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
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.AudioAttributes as ExoAudioAttributes
import androidx.media3.exoplayer.ExoPlayer
import com.zakrni.app.R
import com.zakrni.app.clean.ui.utils.LocaleHelper
import com.zakrni.app.clean.ui.views.HomeActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

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

    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSessionCompat? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var playJob: Job? = null
    private var progressJob: Job? = null
    private var loadingTimeoutJob: Job? = null
    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false
    private val playbackSessionCounter = AtomicInteger(0)

    @Volatile
    private var activePlaybackSessionId: Int = 0

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
    var onPlaybackError: ((String) -> Unit)? = null

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
                exoPlayer?.volume = 0.3f
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                exoPlayer?.volume = 1.0f
                if (!_isPlaying.value && exoPlayer != null) {
                    resume()
                }
            }
        }
    }

    fun playAudio(audioUrl: String, surahName: String, surahNumber: Int, reciterName: String) {
        val sessionId = playbackSessionCounter.incrementAndGet()
        activePlaybackSessionId = sessionId
        playJob?.cancel()
        playJob = serviceScope.launch {
            stopPlayerInternal(resetSurah = false)
            abandonAudioFocus()

            _isLoading.value = true
            _currentSurahNumber.value = surahNumber
            _currentProgress.value = 0
            _duration.value = 0
            currentSurahName = surahName
            currentReciterName = reciterName
            currentAudioUrl = audioUrl
            startForeground(NOTIFICATION_ID, buildNotification())

            if (!requestAudioFocus()) {
                val errorMessage = "Failed to get audio focus"
                Log.w(TAG, errorMessage)
                _isLoading.value = false
                onPlaybackError?.invoke(errorMessage)
                stopPlayerInternal(resetSurah = false)
                stopSelf()
                return@launch
            }

            try {
                val terminalCallbackHandled = AtomicBoolean(false)
                exoPlayer = ExoPlayer.Builder(this@AudioPlaybackService)
                    .build()
                    .also { player ->
                        player.setAudioAttributes(
                            ExoAudioAttributes.Builder()
                                .setUsage(C.USAGE_MEDIA)
                                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                                .build(),
                            false
                        )
                        player.addListener(object : Player.Listener {
                            override fun onPlaybackStateChanged(state: Int) {
                                if (!isCurrentPlaybackSession(sessionId)) return
                                when (state) {
                                    Player.STATE_BUFFERING -> {
                                        _isLoading.value = !player.isPlaying
                                        updateNotification()
                                    }

                                    Player.STATE_READY -> {
                                        _duration.value = player.duration.toSafeDurationMs()
                                        _isLoading.value = false
                                        updateNotification()
                                    }

                                    Player.STATE_ENDED -> {
                                        if (!terminalCallbackHandled.compareAndSet(false, true)) return
                                        cancelLoadingTimeout()
                                        _isLoading.value = false
                                        _isPlaying.value = false
                                        _currentProgress.value = _duration.value
                                        stopProgressTracking()
                                        updateMediaSessionState(PlaybackStateCompat.STATE_STOPPED)
                                        updateNotification()
                                        onPlaybackCompleted?.invoke()
                                    }

                                    else -> Unit
                                }
                            }

                            override fun onIsPlayingChanged(isPlaying: Boolean) {
                                if (!isCurrentPlaybackSession(sessionId)) return
                                _isPlaying.value = isPlaying
                                if (isPlaying) {
                                    _isLoading.value = false
                                    cancelLoadingTimeout()
                                    startProgressTracking(sessionId)
                                    updateMediaSessionState(PlaybackStateCompat.STATE_PLAYING)
                                } else {
                                    stopProgressTracking()
                                    if (player.playbackState == Player.STATE_READY) {
                                        updateMediaSessionState(PlaybackStateCompat.STATE_PAUSED)
                                    }
                                }
                                updateNotification()
                            }

                            override fun onPlayerError(error: PlaybackException) {
                                if (!isCurrentPlaybackSession(sessionId)) return
                                if (!terminalCallbackHandled.compareAndSet(false, true)) return
                                cancelLoadingTimeout()
                                val errorMessage =
                                    "ExoPlayer error (${error.errorCodeName}): ${error.message}"
                                Log.e(TAG, errorMessage, error)
                                _isLoading.value = false
                                _isPlaying.value = false
                                stopProgressTracking()
                                releaseCurrentPlayerSafely()
                                abandonAudioFocus()
                                updateMediaSessionState(PlaybackStateCompat.STATE_STOPPED)
                                onPlaybackError?.invoke(errorMessage)
                            }
                        })
                        player.setMediaItem(MediaItem.fromUri(audioUrl))
                        player.prepare()
                        player.playWhenReady = true
                        // Start polling immediately so UI state stays in sync even if
                        // device/decoder delays the first isPlaying callback.
                        startProgressTracking(sessionId)
                        Log.d(TAG, "Started ExoPlayer prepare: $surahName")
                        startLoadingTimeout(audioUrl, sessionId, terminalCallbackHandled)
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                cancelLoadingTimeout()
                val errorMessage = "Failed to initialize ExoPlayer: ${e.message}"
                Log.e(TAG, errorMessage, e)
                _isLoading.value = false
                if (isCurrentPlaybackSession(sessionId)) {
                    stopPlayerInternal(resetSurah = false)
                    onPlaybackError?.invoke(errorMessage)
                    stopSelf()
                }
            }
        }
    }

    fun pause() {
        exoPlayer?.let { player ->
            if (player.isPlaying || player.playWhenReady) {
                player.playWhenReady = false
                cancelLoadingTimeout()
                _isPlaying.value = false
                _isLoading.value = false
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

        exoPlayer?.let { player ->
            try {
                if (player.playbackState == Player.STATE_ENDED) {
                    player.seekTo(0)
                    _currentProgress.value = 0
                }
                player.playWhenReady = true
                _isLoading.value = player.playbackState == Player.STATE_BUFFERING
                _isPlaying.value = true
                startProgressTracking(activePlaybackSessionId)
                updateNotification()
                updateMediaSessionState(PlaybackStateCompat.STATE_PLAYING)
            } catch (e: Exception) {
                val errorMessage = "Failed to resume playback: ${e.message}"
                Log.e(TAG, errorMessage, e)
                _isLoading.value = false
                _isPlaying.value = false
                onPlaybackError?.invoke(errorMessage)
            }
        }
    }

    fun stop() {
        activePlaybackSessionId = playbackSessionCounter.incrementAndGet()
        playJob?.cancel()
        playJob = null
        stopPlayerInternal(resetSurah = true)
        abandonAudioFocus()
    }

    private fun stopPlayerInternal(resetSurah: Boolean) {
        cancelLoadingTimeout()
        stopProgressTracking()
        releaseCurrentPlayerSafely()
        _isLoading.value = false
        _isPlaying.value = false
        _currentProgress.value = 0
        if (resetSurah) {
            _currentSurahNumber.value = -1
        }
        updateMediaSessionState(PlaybackStateCompat.STATE_STOPPED)
    }

    private fun releaseCurrentPlayerSafely() {
        val currentPlayer = exoPlayer ?: return
        exoPlayer = null
        try {
            currentPlayer.playWhenReady = false
            currentPlayer.stop()
        } catch (_: Exception) {
            // Ignore stop errors during teardown.
        }
        try {
            currentPlayer.clearMediaItems()
        } catch (_: Exception) {
            // Ignore cleanup errors.
        }
        try {
            currentPlayer.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing ExoPlayer: ${e.message}")
        }
    }

    private fun startLoadingTimeout(
        audioUrl: String,
        sessionId: Int,
        terminalCallbackHandled: AtomicBoolean
    ) {
        loadingTimeoutJob?.cancel()
        loadingTimeoutJob = serviceScope.launch {
            delay(20000)
            if (!isCurrentPlaybackSession(sessionId)) return@launch
            if (_isLoading.value && !_isPlaying.value && terminalCallbackHandled.compareAndSet(false, true)) {
                val errorMessage = "Audio loading timeout for URL: $audioUrl"
                Log.e(TAG, errorMessage)
                _isLoading.value = false
                _isPlaying.value = false
                stopProgressTracking()
                releaseCurrentPlayerSafely()
                abandonAudioFocus()
                updateMediaSessionState(PlaybackStateCompat.STATE_STOPPED)
                onPlaybackError?.invoke(errorMessage)
            }
        }
    }

    private fun cancelLoadingTimeout() {
        loadingTimeoutJob?.cancel()
        loadingTimeoutJob = null
    }

    fun seekTo(position: Int) {
        exoPlayer?.let { player ->
            player.seekTo(position.toLong())
            _currentProgress.value = position
            updateMediaSessionState(
                if (_isPlaying.value) {
                    PlaybackStateCompat.STATE_PLAYING
                } else {
                    PlaybackStateCompat.STATE_PAUSED
                }
            )
        }
    }

    fun getCurrentPosition(): Int = exoPlayer?.currentPosition?.toInt() ?: 0
    fun getDurationMs(): Int = exoPlayer?.duration.toSafeDurationMs()

    private fun startProgressTracking(sessionId: Int) {
        progressJob?.cancel()
        progressJob = serviceScope.launch {
            while (isActive && isCurrentPlaybackSession(sessionId)) {
                exoPlayer?.let { player ->
                    val currentlyPlaying = player.isPlaying
                    if (_isPlaying.value != currentlyPlaying) {
                        _isPlaying.value = currentlyPlaying
                        updateMediaSessionState(
                            if (currentlyPlaying) {
                                PlaybackStateCompat.STATE_PLAYING
                            } else {
                                PlaybackStateCompat.STATE_PAUSED
                            }
                        )
                        updateNotification()
                    }
                    if (currentlyPlaying && _isLoading.value) {
                        _isLoading.value = false
                        cancelLoadingTimeout()
                        updateNotification()
                    }
                    val position = player.currentPosition
                    if (position >= 0) {
                        _currentProgress.value = position.toInt()
                    }
                    _duration.value = player.duration.toSafeDurationMs()
                }
                delay(250) // Update 4x per second for smooth seekbar
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun isCurrentPlaybackSession(sessionId: Int): Boolean {
        return activePlaybackSessionId == sessionId
    }

    private fun Long?.toSafeDurationMs(): Int {
        val value = this ?: 0L
        return if (value <= 0L || value == C.TIME_UNSET) 0 else value.toInt()
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
        val position = exoPlayer?.currentPosition ?: 0L
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
        playJob?.cancel()
        playJob = null
        cancelLoadingTimeout()
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
