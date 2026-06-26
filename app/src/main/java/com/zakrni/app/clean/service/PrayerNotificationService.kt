package com.zakrni.app.clean.service

import android.app.ActivityOptions
import android.app.BackgroundServiceStartNotAllowedException
import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.zakrni.app.R
import com.zakrni.app.clean.App
import com.zakrni.app.clean.ui.utils.LocaleHelper
import com.zakrni.app.clean.ui.utils.NetworkManager
import com.zakrni.app.clean.ui.utils.PrayerStorageManager
import com.zakrni.app.clean.ui.utils.PrayerTimeUtils
import com.zakrni.app.clean.ui.compose.MainComposeActivity
import com.zakrni.app.clean.ui.views.PrayerAlertActivity
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
            try {
                context.startForegroundService(intent)
            } catch (e: ForegroundServiceStartNotAllowedException) {
                Log.w(TAG, "Foreground service start blocked for prayer countdown", e)
            } catch (e: BackgroundServiceStartNotAllowedException) {
                Log.w(TAG, "Background service start blocked for prayer countdown", e)
            } catch (e: IllegalStateException) {
                Log.w(TAG, "Service start blocked by system state for prayer countdown", e)
            }
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

    // Monitor network changes - only log, don't stop service
    private fun startNetworkObserver() {
        if (isServiceDestroyed) return

        networkObserverJob?.cancel()
        networkObserverJob = serviceScope.launch {
            try {
                networkManager.isConnected.collect { isConnected ->
                    if (!isConnected && isServiceRunning) {
                        Log.w(TAG, "📵 Network disconnected - service continues running (prayer alerts don't need network)")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error in network observer", e)
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
                if (isServiceDestroyed) {
                    Log.w(TAG, "⚠️ Service destroyed - stopping service")
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
                if (isServiceDestroyed) {
                    Log.w(TAG, "⚠️ Service destroyed - cannot show prayer alert")
                    return START_STICKY
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

        return START_STICKY
    }

    /**
     * Android 14+ foreground-service timeout. A long-lived `dataSync` FGS (the prayer countdown)
     * must stop itself when the system calls this, otherwise Android throws
     * [android.app.RemoteServiceException.ForegroundServiceDidNotStopInTimeException]. The countdown
     * is re-armed on the next prayer update / app open.
     */
    override fun onTimeout(startId: Int) {
        Log.w(TAG, "⏱️ FGS timeout — stopping prayer countdown service")
        try { stopForeground(true) } catch (_: Exception) {}
        stopSelf()
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        Log.w(TAG, "⏱️ FGS timeout ($fgsType) — stopping prayer countdown service")
        try { stopForeground(true) } catch (_: Exception) {}
        stopSelf()
    }

    private fun createLoadingNotification(): Notification {
        val context = contextRef?.get() ?: this

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainComposeActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, App.PRAYER_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_loading_prayer_times))
            .setContentText(getString(R.string.notification_loading_prayer_times))
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

        // Calculate the exact target time from wall clock
        val targetTimeMillis = System.currentTimeMillis() + (initialSeconds * 1000)

        countdownJob = serviceScope.launch {
            try {
                while (!isServiceDestroyed) {
                    // Always recalculate from wall clock — never drift
                    val remainingSeconds = ((targetTimeMillis - System.currentTimeMillis()) / 1000).coerceAtLeast(0)

                    if (remainingSeconds <= 0) break

                    val formattedTime = PrayerTimeUtils.formatTimeRemaining(remainingSeconds)

                    // Check if service is still valid before updating notification
                    if (!isServiceDestroyed && isServiceRunning) {
                        updatePersistentNotification(prayerName, prayerNameArabic, prayerTime, formattedTime)
                    }

                    delay(1000)
                }

                // When countdown reaches 0, transition to next prayer.
                // Don't show prayer alert here — PrayerAlarmReceiver handles it
                // from the scheduled exact prayer alarm to avoid duplicates.
                if (!isServiceDestroyed) {
                    updatePersistentNotification(prayerName, prayerNameArabic, prayerTime, "00:00")

                    // Wait then automatically start countdown for next prayer
                    delay(3000)

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
                context, 0, Intent(context, MainComposeActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val remoteView = RemoteViews(packageName, R.layout.notification_custom).apply {
                val displayPrayerName = getDisplayPrayerName(context, prayerName, prayerNameArabic)
                setTextViewText(
                    R.id.prayer_title,
                    context.getString(R.string.notification_next_prayer_format, displayPrayerName)
                )
                setTextViewText(R.id.prayer_time, prayerTime)
                setTextViewText(R.id.countdown_text, "-$countdown")
                setTextColor(
                    R.id.countdown_text,
                    ContextCompat.getColor(context, R.color.notif_accent)
                )
            }

            val notification = NotificationCompat.Builder(context, App.PRAYER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_dua)
                .setColor(ContextCompat.getColor(context, R.color.notif_accent))
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

        try {
            val context = contextRef?.get() ?: this

            // Check if prayer notifications are enabled
            if (!com.zakrni.app.clean.ui.utils.ThemeManager.isPrayerNotificationsEnabled(context)) {
                Log.d(TAG, "⚠️ Prayer notifications disabled, skipping alert for $prayerName")
                return
            }

            // Full-screen intent → opens PrayerAlertActivity
            val fullScreenIntent = Intent(context, PrayerAlertActivity::class.java).apply {
                putExtra(PrayerAlertActivity.EXTRA_PRAYER_NAME, prayerName)
                putExtra(PrayerAlertActivity.EXTRA_PRAYER_NAME_ARABIC, prayerNameArabic)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            }

            val fullScreenPendingIntent = createFullScreenPendingIntent(context, fullScreenIntent)

            // Try direct launch as fallback
            try {
                PrayerAlertActivity.start(context, prayerName, prayerNameArabic)
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Direct activity start failed: ${e.message}")
            }

            val notification = android.app.Notification.Builder(context, App.PRAYER_ALERT_CHANNEL_ID)
                .setContentTitle(
                    getString(
                        R.string.notification_prayer_alert_title_format,
                        getDisplayPrayerName(context, prayerName, prayerNameArabic)
                    )
                )
                .setContentText(getString(R.string.notification_prayer_alert_body))
                .setSmallIcon(R.drawable.ic_dua)
                .setCategory(android.app.Notification.CATEGORY_REMINDER)
                .setVisibility(android.app.Notification.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setContentIntent(fullScreenPendingIntent)
                .build()

            notificationManager.notify(PRAYER_ALERT_NOTIFICATION_ID, notification)
            Log.d(TAG, "✅ Full-screen prayer alert notification shown for $prayerName")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error showing prayer alert notification", e)
        }
    }

    private fun getDisplayPrayerName(
        context: Context,
        prayerName: String,
        prayerNameArabic: String
    ): String {
        return if (LocaleHelper.isArabic(context)) prayerNameArabic else prayerName
    }

    private fun createFullScreenPendingIntent(
        context: Context,
        intent: Intent
    ): PendingIntent {
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val options = ActivityOptions.makeBasic().apply {
                setPendingIntentCreatorBackgroundActivityStartMode(
                    ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                )
            }
            PendingIntent.getActivity(
                context,
                PRAYER_ALERT_NOTIFICATION_ID,
                intent,
                flags,
                options.toBundle()
            )
        } else {
            PendingIntent.getActivity(
                context,
                PRAYER_ALERT_NOTIFICATION_ID,
                intent,
                flags
            )
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
        Log.d(TAG, "📱 App task removed - keeping notification running")
        // Don't stop the service - keep the notification running
        // The service will continue counting down even after app is killed
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "⚠️ Low memory warning")
        // Don't stop on low memory, just log it
    }
}
