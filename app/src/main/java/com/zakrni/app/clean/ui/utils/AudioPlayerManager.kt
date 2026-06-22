package com.zakrni.app.clean.ui.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.zakrni.app.clean.service.AudioPlaybackService
import com.zakrni.app.clean.ui.compose.MainComposeActivity
import com.zakrni.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_ID = "quran_audio_channel"
        const val NOTIFICATION_ID = 1001
        
        const val ACTION_PLAY = "com.zakrni.app.PLAY"
        const val ACTION_PAUSE = "com.zakrni.app.PAUSE"
        const val ACTION_STOP = "com.zakrni.app.STOP"
        const val ACTION_NEXT = "com.zakrni.app.NEXT"
        const val ACTION_PREVIOUS = "com.zakrni.app.PREVIOUS"
        const val ACTION_PLAY_PAUSE = "com.zakrni.app.PLAY_PAUSE"
    }

    private var serviceStateTrackingJob: Job? = null
    private var loadingSafetyJob: Job? = null
    private var directStateSyncJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var mediaSession: MediaSessionCompat? = null
    private val notificationManager by lazy { 
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager 
    }
    
    // Service connection for background playback
    private var audioService: AudioPlaybackService? = null
    private var isServiceBound = false
    private var pendingPlayRequest: PendingPlayRequest? = null
    private var playbackCompletionCallback: (() -> Unit)? = null
    private var playbackErrorCallback: ((String) -> Unit)? = null
    
    private data class PendingPlayRequest(
        val audioUrl: String,
        val surahNumber: Int,
        val surahName: String,
        val reciterName: String,
        val onCompletion: () -> Unit,
        val onError: (String) -> Unit
    )
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val serviceBinder = binder as? AudioPlaybackService.AudioBinder
            audioService = serviceBinder?.getService()
            isServiceBound = audioService != null
            if (!isServiceBound) {
                Log.e("AudioPlayerManager", "❌ Service connected with null binder")
                _isLoading.value = false
                playbackErrorCallback?.invoke("Audio service unavailable")
                return
            }

            Log.d("AudioPlayerManager", "✅ Service connected")
            val service = audioService ?: return
            val completion = pendingPlayRequest?.onCompletion ?: playbackCompletionCallback ?: {}
            val error = pendingPlayRequest?.onError ?: playbackErrorCallback ?: {}

            setupServiceCallbacks(service = service, onCompletion = completion, onError = error)
            startServiceStateTracking(service)
            startDirectStateSync()
            pendingPlayRequest = null
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioService = null
            isServiceBound = false
            serviceStateTrackingJob?.cancel()
            serviceStateTrackingJob = null
            _isLoading.value = false
            Log.d("AudioPlayerManager", "❌ Service disconnected")
        }
    }

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> get() = _isPlaying

    // Progress as current position in milliseconds
    private val _currentProgress = MutableStateFlow(0)
    val currentProgress: StateFlow<Int> get() = _currentProgress

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> get() = _duration

    private val _currentSurahNumber = MutableStateFlow(-1)
    val currentSurahNumber: StateFlow<Int> get() = _currentSurahNumber

    // Current item index within a playlist (mirrored from the service) for ayah-follow.
    private val _currentItemIndex = MutableStateFlow(0)
    val currentItemIndex: StateFlow<Int> get() = _currentItemIndex

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    // Metadata for notification
    private var currentSurahName: String = ""
    private var currentReciterName: String = ""
    
    // Callbacks for navigation
    var onNextSurah: (() -> Unit)? = null
    var onPreviousSurah: (() -> Unit)? = null

    init {
        createNotificationChannel()
        setupMediaSession()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                localizedText("تشغيل القرآن الكريم", "Quran audio playback"),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = localizedText("إشعارات تشغيل القرآن الكريم", "Quran playback notifications")
                setShowBadge(false)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupMediaSession() {
        mediaSession = MediaSessionCompat(context, "QuranAudioSession").apply {
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

    fun playAudio(
        audioUrl: String,
        surahNumber: Int,
        surahName: String = "",
        reciterName: String = "",
        onCompletion: () -> Unit,
        onError: (String) -> Unit
    ) {
        Log.d("AudioPlayerManager", "🎵 Attempting to play audio via service: $audioUrl")

        // Do not hard-stop manager/service here.
        // AudioPlaybackService.playAudio() already handles seamless transition.
        // Forced stop/start creates race conditions that can desync UI state.
        cancelLoadingSafetyTimeout()
        _isLoading.value = true
        _isPlaying.value = false
        _currentSurahNumber.value = surahNumber
        currentSurahName = surahName
        currentReciterName = reciterName
        playbackCompletionCallback = onCompletion
        playbackErrorCallback = onError
        
        // Store callbacks for later use
        pendingPlayRequest = PendingPlayRequest(audioUrl, surahNumber, surahName, reciterName, onCompletion, onError)
        
        // Start the foreground service for background playback (ONLY the service plays audio)
        AudioPlaybackService.startPlayback(
            context = context,
            audioUrl = audioUrl,
            surahName = surahName.ifEmpty { localizedSurahFallback(surahNumber) },
            surahNumber = surahNumber,
            reciterName = reciterName.ifEmpty { localizedDefaultReciterName() }
        )
        
        // Bind to service to get state updates and callbacks
        bindToService()
        startDirectStateSync()
        
        // Start tracking immediately if service is already bound.
        activeService()?.let { readyService ->
            startServiceStateTracking(readyService)
        }
        startLoadingSafetyTimeout()
    }

    /** Gapless ayah-by-ayah playback. The current ayah is exposed via [currentItemIndex]. */
    fun playPlaylist(
        urls: List<String>,
        surahNumber: Int,
        surahName: String,
        reciterName: String,
        startIndex: Int,
        onCompletion: () -> Unit,
        onError: (String) -> Unit,
    ) {
        if (urls.isEmpty()) return
        cancelLoadingSafetyTimeout()
        _isLoading.value = true
        _isPlaying.value = false
        _currentSurahNumber.value = surahNumber
        _currentItemIndex.value = startIndex
        currentSurahName = surahName
        currentReciterName = reciterName
        playbackCompletionCallback = onCompletion
        playbackErrorCallback = onError
        pendingPlayRequest = PendingPlayRequest(urls.first(), surahNumber, surahName, reciterName, onCompletion, onError)

        AudioPlaybackService.startPlaylist(
            context = context,
            audioUrls = ArrayList(urls),
            surahName = surahName.ifEmpty { localizedSurahFallback(surahNumber) },
            surahNumber = surahNumber,
            reciterName = reciterName.ifEmpty { localizedDefaultReciterName() },
            startIndex = startIndex,
        )

        bindToService()
        startDirectStateSync()
        activeService()?.let { readyService -> startServiceStateTracking(readyService) }
        startLoadingSafetyTimeout()
    }

    private fun startServiceStateTracking(readyService: AudioPlaybackService) {
        serviceStateTrackingJob?.cancel()
        serviceStateTrackingJob = scope.launch {
            setupServiceCallbacks(
                service = readyService,
                onCompletion = playbackCompletionCallback ?: {},
                onError = playbackErrorCallback ?: {}
            )
            readyService.onNextSurah = onNextSurah
            readyService.onPreviousSurah = onPreviousSurah
            Log.d("AudioPlayerManager", "✅ Connected to AudioPlaybackService")

            coroutineScope {
                launch {
                    readyService.isPlaying.collect { playing ->
                        _isPlaying.value = playing
                        if (playing) {
                            _isLoading.value = false
                            cancelLoadingSafetyTimeout()
                        }
                    }
                }
                launch {
                    readyService.currentProgress.collect { progress ->
                        _currentProgress.value = progress // Millisecond position from service
                    }
                }
                launch {
                    readyService.duration.collect { dur ->
                        _duration.value = dur
                    }
                }
                launch {
                    readyService.currentSurahNumber.collect { surahNumber ->
                        _currentSurahNumber.value = surahNumber
                    }
                }
                launch {
                    readyService.currentItemIndex.collect { idx ->
                        _currentItemIndex.value = idx
                    }
                }
                launch {
                    readyService.isLoading.collect { loading ->
                        val effectiveLoading = loading && !readyService.isPlaying.value
                        _isLoading.value = effectiveLoading
                        if (effectiveLoading) {
                            startLoadingSafetyTimeout()
                        } else {
                            cancelLoadingSafetyTimeout()
                        }
                    }
                }
            }
        }
    }
    
    private fun bindToService() {
        if (isServiceBound && audioService != null) return

        if (isServiceBound && audioService == null) {
            isServiceBound = false
        }

        val intent = Intent(context, AudioPlaybackService::class.java)
        val bound = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        if (!bound) {
            Log.w("AudioPlayerManager", "⚠️ Failed to bind AudioPlaybackService")
        }
    }
    
    private fun setupServiceCallbacks(service: AudioPlaybackService, onCompletion: () -> Unit, onError: (String) -> Unit) {
        service.onNextSurah = onNextSurah
        service.onPreviousSurah = onPreviousSurah
        service.onPlaybackCompleted = {
            _isLoading.value = false
            _isPlaying.value = false
            cancelLoadingSafetyTimeout()
            onCompletion()
        }
        service.onPlaybackError = { error ->
            _isLoading.value = false
            _isPlaying.value = false
            cancelLoadingSafetyTimeout()
            onError(error)
        }
    }

    // Overload for backward compatibility
    fun playAudio(
        audioUrl: String,
        surahNumber: Int,
        onCompletion: () -> Unit,
        onError: (String) -> Unit
    ) {
        playAudio(audioUrl, surahNumber, "", "", onCompletion, onError)
    }

    fun setSurahInfo(surahName: String, reciterName: String) {
        currentSurahName = surahName
        currentReciterName = reciterName
        if (_isPlaying.value || _currentSurahNumber.value > 0) {
            updateNotification()
            updateMediaSessionState()
        }
    }

    fun pause() {
        Log.d("AudioPlayerManager", "⏸️ pause() called")
        
        // Pause the background service
        val service = activeService()
        service?.pause()
        
        _isPlaying.value = false
        _isLoading.value = false
        updateNotification()
        updateMediaSessionState()
    }

    fun resume() {
        Log.d("AudioPlayerManager", "▶️ resume() called")
        
        // Resume the background service
        val service = activeService()
        service?.resume()
        
        _isPlaying.value = service?.isPlaying?.value ?: true
        _isLoading.value = service?.isLoading?.value == true && !_isPlaying.value
        updateNotification()
        updateMediaSessionState()
    }

    fun stop() {
        Log.d("AudioPlayerManager", "⏹️ stop() called")
        hideNotification()
        serviceStateTrackingJob?.cancel()
        serviceStateTrackingJob = null
        directStateSyncJob?.cancel()
        directStateSyncJob = null
        cancelLoadingSafetyTimeout()
        
        // Stop the background service
        AudioPlaybackService.stopPlayback(context)
        
        // Unbind from service
        if (isServiceBound) {
            try {
                context.unbindService(serviceConnection)
                isServiceBound = false
            } catch (e: Exception) {
                Log.e("AudioPlayerManager", "Error unbinding service: ${e.message}")
            }
        }
        audioService = null
        pendingPlayRequest = null
        playbackCompletionCallback = null
        playbackErrorCallback = null
        
        _isPlaying.value = false
        _isLoading.value = false
        _currentSurahNumber.value = -1
        _currentProgress.value = 0
        _duration.value = 0
    }

    private fun startLoadingSafetyTimeout() {
        loadingSafetyJob?.cancel()
        loadingSafetyJob = scope.launch {
            delay(20000)
            if (_isLoading.value && !_isPlaying.value) {
                // Avoid false errors if service playback state arrived late.
                val service = activeService()
                val servicePlaying = service?.isPlaying?.value == true
                if (servicePlaying) {
                    _isPlaying.value = true
                    _isLoading.value = false
                    return@launch
                }

                Log.w("AudioPlayerManager", "⚠️ Audio loading safety timeout reached")
                _isLoading.value = false
            }
        }
    }

    private fun cancelLoadingSafetyTimeout() {
        loadingSafetyJob?.cancel()
        loadingSafetyJob = null
    }

    fun seekTo(position: Int) {
        val service = activeService()
        service?.seekTo(position)
        Log.d("AudioPlayerManager", "⏭️ Seeked to position: $position")
    }

    fun isPlayingSurah(surahNumber: Int): Boolean {
        return _isPlaying.value && _currentSurahNumber.value == surahNumber
    }

    fun isActuallyPlaying(): Boolean {
        val service = activeService()
        return service?.isPlaying?.value ?: _isPlaying.value
    }

    fun isCurrentSurah(surahNumber: Int): Boolean {
        return _currentSurahNumber.value == surahNumber
    }

    fun getCurrentPosition(): Int {
        return activeService()?.getCurrentPosition() ?: _currentProgress.value
    }

    fun getDuration(): Int {
        return activeService()?.getDurationMs() ?: _duration.value
    }

    private fun activeService(): AudioPlaybackService? {
        return audioService ?: AudioPlaybackService.getInstance()
    }

    private fun startDirectStateSync() {
        if (directStateSyncJob?.isActive == true) return

        directStateSyncJob = scope.launch {
            while (isActive) {
                val service = activeService()
                if (service != null) {
                    val playing = service.isPlaying.value
                    val loading = service.isLoading.value && !playing
                    _isPlaying.value = playing
                    _isLoading.value = loading
                    _currentSurahNumber.value = service.currentSurahNumber.value
                    _currentProgress.value = service.getCurrentPosition()
                    _duration.value = service.getDurationMs()
                    if (playing) {
                        cancelLoadingSafetyTimeout()
                    }
                }
                delay(350)
            }
        }
    }

    private fun formatTime(milliseconds: Int): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }

    // ========== Notification Methods ==========
    
    private fun showNotification() {
        val notification = buildNotification()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun updateNotification() {
        if (_currentSurahNumber.value > 0) {
            val notification = buildNotification()
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun hideNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun buildNotification(): Notification {
        // Intent to open the app
        val contentIntent = Intent(context, MainComposeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context, 0, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Play/Pause action
        val playPauseIcon = if (_isPlaying.value) R.drawable.ic_pause else R.drawable.ic_play_arrow
        val playPauseText = if (_isPlaying.value) {
            localizedText("إيقاف", "Pause")
        } else {
            localizedText("تشغيل", "Play")
        }
        
        val playPauseIntent = Intent(ACTION_PLAY_PAUSE).apply {
            setPackage(context.packageName)
        }
        val playPausePendingIntent = PendingIntent.getBroadcast(
            context, 1, playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Previous action
        val previousIntent = Intent(ACTION_PREVIOUS).apply {
            setPackage(context.packageName)
        }
        val previousPendingIntent = PendingIntent.getBroadcast(
            context, 2, previousIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Next action
        val nextIntent = Intent(ACTION_NEXT).apply {
            setPackage(context.packageName)
        }
        val nextPendingIntent = PendingIntent.getBroadcast(
            context, 3, nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Stop action
        val stopIntent = Intent(ACTION_STOP).apply {
            setPackage(context.packageName)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, 4, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (currentSurahName.isNotEmpty()) {
            currentSurahName
        } else {
            localizedSurahFallback(_currentSurahNumber.value)
        }
        val subtitle = if (currentReciterName.isNotEmpty()) {
            currentReciterName
        } else {
            localizedText("القرآن الكريم", "Holy Quran")
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setSmallIcon(R.drawable.ic_quran_notification)
            .setContentIntent(contentPendingIntent)
            .setDeleteIntent(stopPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(_isPlaying.value)
            .setOnlyAlertOnce(true)
            .addAction(
                R.drawable.ic_previous,
                localizedText("السابق", "Previous"),
                previousPendingIntent
            )
            .addAction(playPauseIcon, playPauseText, playPausePendingIntent)
            .addAction(
                R.drawable.ic_next,
                localizedText("التالي", "Next"),
                nextPendingIntent
            )
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession?.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .build()
    }

    private fun updateMediaSessionState() {
        val state = if (_isPlaying.value) {
            PlaybackStateCompat.STATE_PLAYING
        } else {
            PlaybackStateCompat.STATE_PAUSED
        }

        val position = _currentProgress.value.toLong()

        val playbackState = PlaybackStateCompat.Builder()
            .setState(state, position, 1f)
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_STOP or
                PlaybackStateCompat.ACTION_SEEK_TO
            )
            .build()

        mediaSession?.setPlaybackState(playbackState)

        val title = if (currentSurahName.isNotEmpty()) {
            currentSurahName
        } else {
            localizedSurahFallback(_currentSurahNumber.value)
        }
        val subtitle = if (currentReciterName.isNotEmpty()) {
            currentReciterName
        } else {
            localizedText("القرآن الكريم", "Holy Quran")
        }

        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, subtitle)
            .putString(
                MediaMetadataCompat.METADATA_KEY_ALBUM,
                localizedText("القرآن الكريم", "Holy Quran")
            )
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, _duration.value.toLong())
            .build()

        mediaSession?.setMetadata(metadata)
    }

    fun release() {
        // Keep singleton manager alive for the whole app process.
        // Releasing/canceling here causes UI desync after reopening Quran screens.
        Log.d("AudioPlayerManager", "🧹 release() ignored for app-scoped singleton manager")
    }

    private fun localizedText(arabic: String, english: String): String {
        return if (LocaleHelper.isArabic(context)) arabic else english
    }

    private fun localizedSurahFallback(surahNumber: Int): String {
        return localizedText("سورة $surahNumber", "Surah $surahNumber")
    }

    private fun localizedDefaultReciterName(): String {
        return localizedText("مشاري العفاسي", "Mishary Alafasy")
    }
}
