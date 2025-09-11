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
import com.example.zakrni.clean.ui.utils.PrayerTimeUtils
import com.example.zakrni.clean.ui.views.HomeActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class PrayerNotificationService : Service() {

    @Inject
    lateinit var networkManager: NetworkManager

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var countdownJob: Job? = null
    private lateinit var notificationManager: NotificationManager

    companion object {
        const val NOTIFICATION_ID = 1001
        const val PRAYER_ALERT_NOTIFICATION_ID = 1002
        private const val TAG = "PrayerNotificationService"

        const val ACTION_START_COUNTDOWN = "START_COUNTDOWN"
        const val ACTION_STOP_SERVICE = "STOP_SERVICE"
        const val ACTION_SHOW_PRAYER_ALERT = "SHOW_PRAYER_ALERT"

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
        notificationManager = getSystemService(NotificationManager::class.java)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_COUNTDOWN -> {
                // 🚀 Only start countdown if online
                if (!networkManager.isNetworkAvailable()) {
                    Log.w(TAG, "📵 Device offline - stopping prayer notification service")
                    stopSelf()
                    return START_NOT_STICKY
                }

                val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: ""
                val prayerNameArabic = intent.getStringExtra(EXTRA_PRAYER_NAME_ARABIC) ?: ""
                val prayerTime = intent.getStringExtra(EXTRA_PRAYER_TIME) ?: ""
                val remainingSeconds = intent.getLongExtra(EXTRA_REMAINING_SECONDS, 0)

                startForeground(NOTIFICATION_ID, createLoadingNotification())
                startCountdown(prayerName, prayerNameArabic, prayerTime, remainingSeconds)
            }
            ACTION_STOP_SERVICE -> {
                stopSelf()
            }
            ACTION_SHOW_PRAYER_ALERT -> {
                // 🚀 Only show prayer alert if online
                if (!networkManager.isNetworkAvailable()) {
                    Log.w(TAG, "📵 Device offline - cannot show prayer alert")
                    return START_NOT_STICKY
                }

                val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: ""
                val prayerNameArabic = intent.getStringExtra(EXTRA_PRAYER_NAME_ARABIC) ?: ""
                showPrayerAlertNotification(prayerName, prayerNameArabic)
            }
        }
        return START_STICKY
    }

    private fun createLoadingNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, HomeActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, App.PRAYER_CHANNEL_ID)
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
        countdownJob?.cancel()

        countdownJob = serviceScope.launch {
            var remainingSeconds = initialSeconds

            while (remainingSeconds > 0) {
                // 🚀 Check network status during countdown
                if (!networkManager.isNetworkAvailable()) {
                    Log.w(TAG, "📵 Device went offline during countdown - stopping service")
                    stopSelf()
                    return@launch
                }

                val formattedTime = PrayerTimeUtils.formatTimeRemaining(remainingSeconds)
                updatePersistentNotification(prayerName, prayerNameArabic, prayerTime, formattedTime)

                delay(1000)
                remainingSeconds--
            }

            // When countdown reaches 0, show prayer alert (only if still online)
            if (remainingSeconds <= 0 && networkManager.isNetworkAvailable()) {
                updatePersistentNotification(prayerName, prayerNameArabic, prayerTime, "00:00")
                showPrayerAlertNotification(prayerName, prayerNameArabic)
            }
        }
    }

    private fun updatePersistentNotification(
        prayerName: String,
        prayerNameArabic: String,
        prayerTime: String,
        countdown: String,
    ) {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, HomeActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val remoteView = RemoteViews(packageName, R.layout.notification_custom).apply {
            setTextViewText(R.id.prayer_title, "Next $prayerName")
            setTextViewText(R.id.prayer_time, prayerTime)
            setTextViewText(R.id.countdown_text, "-$countdown")
            setTextColor(
                R.id.countdown_text,
                ContextCompat.getColor(this@PrayerNotificationService, R.color.primary_color)
            )
        }

        val notification = NotificationCompat.Builder(this, App.PRAYER_CHANNEL_ID)
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
    }

    private fun showPrayerAlertNotification(prayerName: String, prayerNameArabic: String) {
        // Final check before showing prayer alert
        if (!networkManager.isNetworkAvailable()) {
            Log.w(TAG, "📵 Cannot show prayer alert - device offline")
            return
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, HomeActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alertNotification = NotificationCompat.Builder(this, App.NOTIFICATION_CHANNEL_ID)
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
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        countdownJob?.cancel()
        serviceScope.cancel()
        Log.d(TAG, "🛑 Prayer notification service destroyed")
    }
}