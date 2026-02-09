package com.zakrni.app.clean.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.zakrni.app.R
import com.zakrni.app.clean.App
import com.zakrni.app.clean.ui.utils.NetworkManager
import com.zakrni.app.clean.ui.utils.LocaleHelper
import com.zakrni.app.clean.ui.utils.PrayerStorageManager
import com.zakrni.app.clean.ui.utils.PrayerTimeUtils
import com.zakrni.app.clean.ui.utils.ThemeManager
import com.zakrni.app.clean.ui.views.HomeActivity
import com.zakrni.app.clean.ui.views.PrayerAlertActivity
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrayerAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var networkManager: NetworkManager

    @Inject
    lateinit var prayerStorageManager: PrayerStorageManager

    companion object {
        const val ACTION_PRAYER_ALERT = "com.zakrni.app.PRAYER_ALERT"
        const val ACTION_COUNTDOWN_NOTIFICATION = "com.zakrni.app.COUNTDOWN_NOTIFICATION"
        const val ACTION_AUTO_RESCHEDULE = "com.zakrni.app.AUTO_RESCHEDULE"

        const val EXTRA_PRAYER_NAME = "prayer_name"
        const val EXTRA_PRAYER_NAME_ARABIC = "prayer_name_arabic"
        const val EXTRA_PRAYER_TIME = "prayer_time"

        private const val PRAYER_ALERT_NOTIFICATION_ID = 3001
        private const val COUNTDOWN_NOTIFICATION_ID = 3002

        private const val TAG = "PrayerAlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_PRAYER_ALERT -> {
                val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: return
                val prayerNameArabic = intent.getStringExtra(EXTRA_PRAYER_NAME_ARABIC) ?: return

                Log.d(TAG, "🕌 Prayer alert received for $prayerName")

                // Check if prayer notifications are enabled
                if (!ThemeManager.isPrayerNotificationsEnabled(context)) {
                    Log.d(TAG, "⚠️ Prayer notifications disabled by user, skipping alert for $prayerName")
                    // Still start service for next prayer countdown
                    startServiceForNextPrayer(context)
                    return
                }

                // Show prayer alert via full-screen notification (works on all Android versions)
                showPrayerAlertNotification(context, prayerName, prayerNameArabic)

                // Start service for next prayer automatically
                startServiceForNextPrayer(context)
            }

            ACTION_COUNTDOWN_NOTIFICATION -> {
                val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: return
                val prayerNameArabic = intent.getStringExtra(EXTRA_PRAYER_NAME_ARABIC) ?: return
                val prayerTime = intent.getStringExtra(EXTRA_PRAYER_TIME) ?: return

                // Check if notifications are enabled
                if (!ThemeManager.isNotificationsEnabled(context) ||
                    !ThemeManager.isPrayerNotificationsEnabled(context)) {
                    Log.d(TAG, "⚠️ Notifications disabled, skipping countdown for $prayerName")
                    return
                }

                showCountdownNotification(context, prayerName, prayerNameArabic, prayerTime)
            }

            ACTION_AUTO_RESCHEDULE -> {
                handleAutoReschedule(context)
            }

            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                rescheduleAlarms(context)
            }
        }
    }

    private fun startServiceForNextPrayer(context: Context) {
        try {
            val savedTimings = prayerStorageManager.getSavedPrayerTimes()
            if (savedTimings != null && prayerStorageManager.areSavedTimesValid()) {
                val (_, nextPrayer) = PrayerTimeUtils.getCurrentAndNextPrayer(savedTimings)

                nextPrayer?.let { next ->
                    Log.d(TAG, "🔄 Starting service for next prayer: ${next.name}")

                    PrayerNotificationService.startService(
                        context = context,
                        prayerName = next.name,
                        prayerNameArabic = next.nameArabic,
                        prayerTime = next.time,
                        remainingSeconds = next.timeRemainingInSeconds
                    )
                }
            } else {
                Log.w(TAG, "⚠️ No valid saved timings found for next prayer service")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error starting service for next prayer", e)
        }
    }

    private fun handleAutoReschedule(context: Context) {
        try {
            Log.d(TAG, "🔄 Auto reschedule triggered for new day")

            val savedTimings = prayerStorageManager.getSavedPrayerTimes()
            if (savedTimings != null) {
                val alarmManager = PrayerAlarmManager(context, networkManager, prayerStorageManager)
                alarmManager.scheduleAllPrayerAlarms(savedTimings)
                startServiceForNextPrayer(context)
                Log.d(TAG, "✅ Auto reschedule completed for new day")
            } else {
                Log.w(TAG, "⚠️ No saved timings available for auto reschedule")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error in auto reschedule", e)
        }
    }

    /**
     * Show prayer alert using a FULL-SCREEN NOTIFICATION.
     * On Android 10+ (API 29+), starting activities from background is restricted.
     * A full-screen intent notification is the correct way to display urgent alerts.
     * If the phone is locked → shows the activity full screen.
     * If the phone is unlocked → shows a heads-up notification that opens the activity.
     */
    private fun showPrayerAlertNotification(
        context: Context,
        prayerName: String,
        prayerNameArabic: String
    ) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Cancel any existing prayer alert notification first
            // This ensures the system treats the new one as fresh and triggers full-screen intent
            notificationManager.cancel(PRAYER_ALERT_NOTIFICATION_ID)

            val fullScreenIntent = Intent(context, PrayerAlertActivity::class.java).apply {
                putExtra(PrayerAlertActivity.EXTRA_PRAYER_NAME, prayerName)
                putExtra(PrayerAlertActivity.EXTRA_PRAYER_NAME_ARABIC, prayerNameArabic)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            }

            val fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                PRAYER_ALERT_NOTIFICATION_ID,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Build full-screen notification with alarm sound
            val displayPrayerName = getDisplayPrayerName(context, prayerName, prayerNameArabic)
            val notification = NotificationCompat.Builder(context, App.PRAYER_ALERT_CHANNEL_ID)
                .setContentTitle(
                    context.getString(
                        R.string.notification_prayer_alert_title_format,
                        displayPrayerName
                    )
                )
                .setContentText(context.getString(R.string.notification_prayer_alert_body))
                .setSmallIcon(R.drawable.ic_dua)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setContentIntent(fullScreenPendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setOngoing(true)
                .setAutoCancel(false)
                .build()

            // Post notification with full-screen intent
            // On locked screen → system shows PrayerAlertActivity full-screen
            // On unlocked screen → shows heads-up notification (tap to open)
            // NOTE: Do NOT call startActivity() or acquire WakeLock here —
            // on Android 14+/15, direct BAL is blocked and WakeLock wakes the
            // screen before the system processes fullScreenIntent, which causes
            // it to show as heads-up instead of full-screen.
            notificationManager.notify(PRAYER_ALERT_NOTIFICATION_ID, notification)

            // User requirement: when phone is already unlocked, open alert screen immediately.
            maybeForceOpenAlertScreen(context, fullScreenPendingIntent, prayerName)

            Log.d(TAG, "✅ Full-screen prayer alert notification posted for $prayerName")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error showing prayer alert notification", e)
        }
    }

    private fun maybeForceOpenAlertScreen(
        context: Context,
        fullScreenPendingIntent: PendingIntent,
        prayerName: String
    ) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager

        val isScreenInteractive = powerManager?.isInteractive == true
        val isKeyguardLocked = keyguardManager?.isKeyguardLocked == true

        // If the user is actively using the phone, force-open the alert activity now.
        if (isScreenInteractive && !isKeyguardLocked) {
            try {
                fullScreenPendingIntent.send()
                Log.d(TAG, "🚀 Forced PrayerAlertActivity launch on unlocked screen for $prayerName")
            } catch (e: PendingIntent.CanceledException) {
                Log.w(TAG, "⚠️ PendingIntent canceled while forcing alert screen", e)
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Failed to force-open alert screen", e)
            }
        }
    }

    private fun showCountdownNotification(
        context: Context,
        prayerName: String,
        prayerNameArabic: String,
        prayerTime: String
    ) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val intent = Intent(context, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val displayPrayerName = getDisplayPrayerName(context, prayerName, prayerNameArabic)
            val notification = NotificationCompat.Builder(context, App.PRAYER_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notification_countdown_title))
                .setContentText(
                    context.getString(
                        R.string.notification_countdown_body_format,
                        displayPrayerName
                    )
                )
                .setSmallIcon(R.drawable.ic_dua)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            context.getString(
                                R.string.notification_countdown_big_text_format,
                                displayPrayerName,
                                prayerTime
                            )
                        )
                )
                .build()

            val notificationId = COUNTDOWN_NOTIFICATION_ID + prayerName.hashCode()
            notificationManager.notify(notificationId, notification)

            Log.d(TAG, "✅ Countdown notification shown for $prayerName")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error showing countdown notification", e)
        }
    }

    private fun rescheduleAlarms(context: Context) {
        try {
            Log.d(TAG, "🔄 Rescheduling alarms after boot/update")

            val savedTimings = prayerStorageManager.getSavedPrayerTimes()

            if (savedTimings != null && prayerStorageManager.areSavedTimesValid()) {
                val alarmManager = PrayerAlarmManager(context, networkManager, prayerStorageManager)
                alarmManager.scheduleAllPrayerAlarms(savedTimings)
                startServiceForNextPrayer(context)
                Log.d(TAG, "✅ Alarms rescheduled successfully after reboot")
            } else {
                Log.w(TAG, "⚠️ No valid saved timings found for rescheduling")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error rescheduling alarms after reboot", e)
        }
    }

    private fun getDisplayPrayerName(
        context: Context,
        prayerName: String,
        prayerNameArabic: String
    ): String {
        return if (LocaleHelper.isArabic(context)) prayerNameArabic else prayerName
    }
}
