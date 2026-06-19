package com.zakrni.app.clean

import android.app.Application
import android.content.Context
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import android.webkit.WebView
import com.zakrni.app.R
import com.zakrni.app.clean.ads.AdManager
import com.zakrni.app.clean.ads.SubscriptionManager
import com.zakrni.app.clean.ui.utils.LocaleHelper
import com.zakrni.app.clean.ui.utils.ThemeManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App: Application() {

    @Inject lateinit var adManager: AdManager
    @Inject lateinit var subscriptionManager: SubscriptionManager

    companion object {
        const val PRAYER_CHANNEL_ID = "PRAYER_TIMES_CHANNEL"
        const val PRAYER_ALERT_CHANNEL_ID = "PRAYER_ALERT_CHANNEL_V2"
        const val AZKAR_CHANNEL_ID = "AZKAR_REMINDER_CHANNEL"
        const val NOTIFICATION_CHANNEL_ID = "APP_CHANNEL"
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.wrap(base))
    }

    override fun onCreate() {
        super.onCreate()

        // Apply saved app language before creating UI-facing channels/screens.
        ThemeManager.applySavedLanguage(this)

        // Apply saved theme FIRST before anything else
        ThemeManager.applySavedTheme(this)
        
        createNotificationChannels()

        // Warm up WebView to help JavascriptEngine initialization on Android 14+
        try {
            WebView(this).destroy()
        } catch (_: Exception) {
            // WebView warmup failed — non-critical
        }

        // Initialize subscription manager first (to check if ads should show)
        subscriptionManager.initialize()

        // Initialize AdMob as early as possible.
        adManager.initialize()

        // Start azkar reminders if enabled
        if (ThemeManager.isAzkarRemindersEnabled(this)) {
            com.zakrni.app.clean.service.AzkarReminderWorker.schedule(this)
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Prayer times channel for persistent countdown notification (low importance - silent)
            val prayerChannel = NotificationChannel(
                PRAYER_CHANNEL_ID,
                getString(R.string.notification_channel_prayer_times_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_prayer_times_desc)
                lightColor = Color.GREEN
                setSound(null, null)
                enableVibration(false)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            // Prayer ALERT channel - HIGH importance with sound & vibration for actual prayer time alerts
            val prayerAlertChannel = NotificationChannel(
                PRAYER_ALERT_CHANNEL_ID,
                getString(R.string.notification_channel_prayer_alert_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_prayer_alert_desc)
                lightColor = Color.GREEN
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                // Keep notification silent to avoid duplicate audio.
                // The PrayerAlertActivity plays the adhan sound directly.
                setSound(null, null)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setBypassDnd(false)
            }

            // Azkar reminder channel
            val azkarChannel = NotificationChannel(
                AZKAR_CHANNEL_ID,
                getString(R.string.notification_channel_azkar_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_azkar_desc)
                lightColor = Color.GREEN
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            // General app notifications channel
            val generalChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_general_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_general_desc)
                lightColor = Color.BLUE
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(prayerChannel)
            notificationManager.createNotificationChannel(prayerAlertChannel)
            notificationManager.createNotificationChannel(azkarChannel)
            notificationManager.createNotificationChannel(generalChannel)
        }
    }
}
