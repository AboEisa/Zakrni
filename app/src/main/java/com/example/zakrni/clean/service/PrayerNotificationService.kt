package com.example.zakrni.clean.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.zakrni.R
import com.example.zakrni.clean.App
import com.example.zakrni.clean.ui.utils.NetworkManager
import com.example.zakrni.clean.ui.utils.PrayerStorageManager
import com.example.zakrni.clean.ui.utils.PrayerTimeUtils
import com.example.zakrni.clean.ui.views.HomeActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import javax.inject.Inject

@AndroidEntryPoint
class PrayerNotificationService : Service() {

    @Inject
    lateinit var networkManager: NetworkManager

    @Inject
    lateinit var prayerStorageManager: PrayerStorageManager

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var countdownJob: Job? = null
    private var networkObserverJob: Job? = null
    private lateinit var notificationManager: NotificationManager

    // Use WeakReference to prevent memory leaks
    private var contextRef: WeakReference<Context>? = null

    // Service state management
    private var isServiceRunning = false
    private var isServiceDestroyed = false

    companion object {
        const val NOTIFICATION_ID = 1001
        const val PRAYER_ALERT_NOTIFICATION_ID = 1002
        private const val TAG = "PrayerNotificationService"

        const val ACTION_START_COUNTDOWN = "START_COUNTDOWN"
        const val ACTION_STOP_SERVICE = "STOP_SERVICE"
        const val ACTION_SHOW_PRAYER_ALERT = "SHOW_PRAYER_ALERT"
        const val ACTION_FORCE_STOP = "FORCE_STOP"

        const val EXTRA_PRAYER_NAME = "prayer_name"
        const val EXTRA_PRAYER_NAME_ARABIC = "prayer_name_arabic"
        const val EXTRA_PRAYER_TIME = "prayer_time"
        const val EXTRA_REMAINING_SECONDS = "remaining_seconds"

        fun startService(
            context: Context,
            prayerName: String,
            prayerNameArabic: String,
            prayerTime: String,
            remainingSeconds: Long
        ) {
            val intent = Intent(context, PrayerNotificationService::class.java).apply {
                action = ACTION_START_COUNTDOWN
                putExtra(EXTRA_PRAYER_NAME, prayerName)
                putExtra(EXTRA_PRAYER_NAME_ARABIC, prayerNameArabic)
                putExtra(EXTRA_PRAYER_TIME, prayerTime)
                putExtra(EXTRA_REMAINING_SECONDS, remainingSeconds)
            }
            context.startForegroundService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, PrayerNotificationService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }

        fun forceStopService(context: Context) {
            val intent = Intent(context, PrayerNotificationService::class.java).apply {
                action = ACTION_FORCE_STOP
            }
            context.startService(intent)
        }

        fun showPrayerAlert(context: Context, prayerName: String, prayerNameArabic: String) {
            val intent = Intent(context, PrayerNotificationService::class.java).apply {
                action = ACTION_SHOW_PRAYER_ALERT
                putExtra(EXTRA_PRAYER_NAME, prayerName)
                putExtra(EXTRA_PRAYER_NAME_ARABIC, prayerNameArabic)
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()

        try {
            notificationManager = getSystemService(NotificationManager::class.java)
            contextRef = WeakReference(this)
            isServiceRunning = true
            isServiceDestroyed = false

            Log.d(TAG, "🚀 PrayerNotificationService created")

            // Start observing network changes
            startNetworkObserver()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error in onCreate", e)
            cleanupAndStop()
        }
    }

    // Monitor network changes to stop service when offline
    private fun startNetworkObserver() {
        if (isServiceDestroyed) return

        networkObserverJob?.cancel()
        networkObserverJob = serviceScope.launch {
            try {
                networkManager.isConnected.collect { isConnected ->
                    if (!isConnected && isServiceRunning) {
                        Log.w(TAG, "📵 Network disconnected - stopping service")
                        cleanupAndStop()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error in network observer", e)
                cleanupAndStop()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isServiceDestroyed) {
            Log.w(TAG, "⚠️ Service already destroyed, ignoring command")
            return START_NOT_STICKY
        }

        when (intent?.action) {
            ACTION_START_COUNTDOWN -> {
                // Only start countdown if online and service is healthy
                if (!networkManager.isNetworkAvailable() || isServiceDestroyed) {
                    Log.w(TAG, "📵 Device offline or service destroyed - stopping service")
                    cleanupAndStop()
                    return START_NOT_STICKY
                }

                val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: ""
                val prayerNameArabic = intent.getStringExtra(EXTRA_PRAYER_NAME_ARABIC) ?: ""
                val prayerTime = intent.getStringExtra(EXTRA_PRAYER_TIME) ?: ""
                val remainingSeconds = intent.getLongExtra(EXTRA_REMAINING_SECONDS, 0)

                try {
                    startForeground(NOTIFICATION_ID, createLoadingNotification())
                    startCountdown(prayerName, prayerNameArabic, prayerTime, remainingSeconds)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error starting countdown", e)
                    cleanupAndStop()
                    return START_NOT_STICKY
                }
            }

            ACTION_STOP_SERVICE -> {
                Log.d(TAG, "🛑 Stop service requested")
                cleanupAndStop()
            }

            ACTION_FORCE_STOP -> {
                Log.d(TAG, "🛑 Force stop requested")
                forceCleanupAndStop()
            }

            ACTION_SHOW_PRAYER_ALERT -> {
                // Only show prayer alert if online and service is healthy
                if (!networkManager.isNetworkAvailable() || isServiceDestroyed) {
                    Log.w(TAG, "📵 Device offline or service destroyed - cannot show prayer alert")
                    return START_NOT_STICKY
                }

                val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: ""
                val prayerNameArabic = intent.getStringExtra(EXTRA_PRAYER_NAME_ARABIC) ?: ""

                try {
                    showPrayerAlertNotification(prayerName, prayerNameArabic)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error showing prayer alert", e)
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun createLoadingNotification(): Notification {
        val context = contextRef?.get() ?: this

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, HomeActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, App.PRAYER_CHANNEL_ID)
            .setContentTitle("جاري تحميل أوقات الصلاة...")
            .setContentText("Loading prayer times...")
            .setSmallIcon(R.drawable.ic_dua)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun startCountdown(
        prayerName: String,
        prayerNameArabic: String,
        prayerTime: String,
        initialSeconds: Long
    ) {
        // Cancel any existing countdown
        countdownJob?.cancel()

        countdownJob = serviceScope.launch {
            var remainingSeconds = initialSeconds

            try {
                while (remainingSeconds > 0 && !isServiceDestroyed) {
                    // Check network status during countdown
                    if (!networkManager.isNetworkAvailable()) {
                        Log.w(TAG, "📵 Device went offline during countdown - stopping service")
                        cleanupAndStop()
                        return@launch
                    }

                    val formattedTime = PrayerTimeUtils.formatTimeRemaining(remainingSeconds)

                    // Check if service is still valid before updating notification
                    if (!isServiceDestroyed && isServiceRunning) {
                        updatePersistentNotification(prayerName, prayerNameArabic, prayerTime, formattedTime)
                    }

                    delay(1000)
                    remainingSeconds--
                }

                // **KEY FIX: When countdown reaches 0, transition to next prayer**
                if (remainingSeconds <= 0 && networkManager.isNetworkAvailable() && !isServiceDestroyed) {
                    updatePersistentNotification(prayerName, prayerNameArabic, prayerTime, "00:00")
                    showPrayerAlertNotification(prayerName, prayerNameArabic)

                    // **Wait then automatically start countdown for next prayer**
                    delay(3000) // Give time for user to see the notification

                    startCountdownForNextPrayer()
                }

            } catch (e: CancellationException) {
                Log.d(TAG, "Countdown cancelled")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error in countdown", e)
                cleanupAndStop()
            }
        }
    }

    // **NEW METHOD: Automatically transition to next prayer**
    private suspend fun startCountdownForNextPrayer() {
        try {
            val savedTimings = prayerStorageManager.getSavedPrayerTimes()
            if (savedTimings != null && prayerStorageManager.areSavedTimesValid()) {
                val (_, nextPrayer) = PrayerTimeUtils.getCurrentAndNextPrayer(savedTimings)

                nextPrayer?.let { next ->
                    Log.d(TAG, "🔄 Automatically transitioning to next prayer: ${next.name}")

                    // Start countdown for next prayer without stopping service
                    startCountdown(
                        next.name,
                        next.nameArabic,
                        next.time,
                        next.timeRemainingInSeconds
                    )
                }
            } else {
                Log.w(TAG, "⚠️ No valid saved timings found, stopping service")
                cleanupAndStop()
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error transitioning to next prayer", e)
            cleanupAndStop()
        }
    }

    private fun updatePersistentNotification(
        prayerName: String,
        prayerNameArabic: String,
        prayerTime: String,
        countdown: String,
    ) {
        if (isServiceDestroyed) return

        try {
            val context = contextRef?.get() ?: this

            val pendingIntent = PendingIntent.getActivity(
                context, 0, Intent(context, HomeActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val remoteView = RemoteViews(packageName, R.layout.notification_custom).apply {
                setTextViewText(R.id.prayer_title, "Next $prayerName")
                setTextViewText(R.id.prayer_time, prayerTime)
                setTextViewText(R.id.countdown_text, "-$countdown")
                setTextColor(
                    R.id.countdown_text,
                    ContextCompat.getColor(context, R.color.primary_color)
                )
            }

            val notification = NotificationCompat.Builder(context, App.PRAYER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_dua)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setCustomContentView(remoteView)
                .setCustomBigContentView(remoteView)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating notification", e)
        }
    }

    private fun showPrayerAlertNotification(prayerName: String, prayerNameArabic: String) {
        if (isServiceDestroyed) return

        // Final check before showing prayer alert
        if (!networkManager.isNetworkAvailable()) {
            Log.w(TAG, "📵 Cannot show prayer alert - device offline")
            return
        }

        try {
            val context = contextRef?.get() ?: this

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, HomeActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alertNotification = NotificationCompat.Builder(context, App.NOTIFICATION_CHANNEL_ID)
                .setContentTitle("وقت الصلاة - Prayer Time")
                .setContentText("حان وقت صلاة $prayerNameArabic - Time for $prayerName prayer")
                .setSmallIcon(R.drawable.ic_dua)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build()

            notificationManager.notify(PRAYER_ALERT_NOTIFICATION_ID + prayerName.hashCode(), alertNotification)
            Log.d(TAG, "✅ Prayer alert notification shown for $prayerName")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error showing prayer alert", e)
        }
    }

    // Clean shutdown method
    private fun cleanupAndStop() {
        if (isServiceDestroyed) return

        Log.d(TAG, "🧹 Cleaning up service...")

        isServiceRunning = false

        try {
            // Cancel all jobs
            countdownJob?.cancel()
            networkObserverJob?.cancel()

            // Clear notifications
            notificationManager.cancel(NOTIFICATION_ID)

            // Stop foreground
            stopForeground(true)

            // Stop service
            stopSelf()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error during cleanup", e)
        }
    }

    // Force cleanup for app destruction
    private fun forceCleanupAndStop() {
        Log.d(TAG, "🧹 Force cleaning up service...")

        isServiceDestroyed = true
        isServiceRunning = false

        try {
            // Cancel all coroutines immediately
            serviceScope.cancel()
            countdownJob?.cancel()
            networkObserverJob?.cancel()

            // Clear all notifications
            notificationManager.cancelAll()

            // Stop foreground and service
            stopForeground(true)
            stopSelf()

            // Clear context reference
            contextRef?.clear()
            contextRef = null

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error during force cleanup", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()

        Log.d(TAG, "🛑 PrayerNotificationService onDestroy called")

        isServiceDestroyed = true
        isServiceRunning = false

        try {
            // Cancel all coroutines
            serviceScope.cancel()
            countdownJob?.cancel()
            networkObserverJob?.cancel()

            // Clear notifications
            notificationManager.cancel(NOTIFICATION_ID)

            // Clear context reference to prevent memory leaks
            contextRef?.clear()
            contextRef = null

            Log.d(TAG, "✅ Service cleanup completed")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error in onDestroy", e)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "📱 App task removed - cleaning up service")
        forceCleanupAndStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "⚠️ Low memory - cleaning up service")
        cleanupAndStop()
    }
}