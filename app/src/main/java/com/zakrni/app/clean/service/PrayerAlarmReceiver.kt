package com.zakrni.app.clean.service

import android.app.ActivityOptions
import android.app.ActivityManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
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
import com.zakrni.app.clean.ui.compose.MainComposeActivity
import com.zakrni.app.clean.ui.views.PrayerAlertActivity
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs
import java.util.Locale

@AndroidEntryPoint
class PrayerAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var networkManager: NetworkManager

    @Inject
    lateinit var prayerStorageManager: PrayerStorageManager

    companion object {
        const val ACTION_PRAYER_ALERT = "com.zakrni.app.PRAYER_ALERT"
        const val ACTION_PRAYER_ALERT_SYNC = "com.zakrni.app.PRAYER_ALERT_SYNC"
        const val ACTION_COUNTDOWN_NOTIFICATION = "com.zakrni.app.COUNTDOWN_NOTIFICATION"
        const val ACTION_AUTO_RESCHEDULE = "com.zakrni.app.AUTO_RESCHEDULE"

        const val EXTRA_PRAYER_NAME = "prayer_name"
        const val EXTRA_PRAYER_NAME_ARABIC = "prayer_name_arabic"
        const val EXTRA_PRAYER_TIME = "prayer_time"
        const val EXTRA_SCHEDULED_TRIGGER_AT_MS = "scheduled_trigger_at_ms"

        private const val PRAYER_ALERT_NOTIFICATION_ID = 3001
        private const val COUNTDOWN_NOTIFICATION_ID = 3002

        private const val TAG = "PrayerAlarmReceiver"
        private const val DUPLICATE_ALERT_WINDOW_MS = 120_000L
        private const val VALID_ALERT_DRIFT_WINDOW_MS = 20 * 60 * 1000L

        @Volatile
        private var lastAlertKey: String? = null

        @Volatile
        private var lastAlertTimestampMs: Long = 0L
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_PRAYER_ALERT -> {
                val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: return
                val prayerNameArabic = intent.getStringExtra(EXTRA_PRAYER_NAME_ARABIC) ?: return
                val expectedTriggerAt = intent.getLongExtra(EXTRA_SCHEDULED_TRIGGER_AT_MS, -1L)

                Log.d(TAG, "🕌 Prayer alert received for $prayerName")

                if (!isAlertTimingValid(prayerName, expectedTriggerAt)) {
                    Log.w(
                        TAG,
                        "⏱️ Ignoring out-of-window prayer alert for $prayerName " +
                            "(expected=$expectedTriggerAt now=${System.currentTimeMillis()})"
                    )
                    startServiceForNextPrayer(context)
                    return
                }

                val alertKey = "$prayerName|$prayerNameArabic"
                if (isDuplicateAlert(alertKey)) {
                    Log.d(TAG, "⚠️ Duplicate prayer alert ignored for $prayerName")
                    return
                }

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

            ACTION_PRAYER_ALERT_SYNC -> {
                val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME).orEmpty()
                val prayerNameArabic = intent.getStringExtra(EXTRA_PRAYER_NAME_ARABIC).orEmpty()
                val expectedTriggerAt = intent.getLongExtra(EXTRA_SCHEDULED_TRIGGER_AT_MS, -1L)
                Log.d(TAG, "🔁 Prayer alert sync received for $prayerName / $prayerNameArabic")

                if (prayerName.isNotBlank() && prayerNameArabic.isNotBlank()) {
                    if (!isAlertTimingValid(prayerName, expectedTriggerAt)) {
                        Log.w(
                            TAG,
                            "⏱️ Ignoring out-of-window prayer sync for $prayerName " +
                                "(expected=$expectedTriggerAt now=${System.currentTimeMillis()})"
                        )
                        startServiceForNextPrayer(context)
                        return
                    }

                    if (!isPrayerAlertOnTop(context)) {
                        showPrayerAlertNotification(context, prayerName, prayerNameArabic)
                    } else {
                        Log.d(TAG, "✅ PrayerAlertActivity already visible, skipping sync notification")
                    }
                }

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

            val fullScreenPendingIntent = createFullScreenPendingIntent(
                context = context,
                intent = fullScreenIntent
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
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setContentIntent(fullScreenPendingIntent)
                .setOngoing(false)
                .setAutoCancel(true)
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
            maybeForceOpenAlertScreen(
                context = context,
                fullScreenPendingIntent = fullScreenPendingIntent,
                fullScreenIntent = fullScreenIntent,
                prayerName = prayerName
            )

            Log.d(TAG, "✅ Full-screen prayer alert notification posted for $prayerName")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error showing prayer alert notification", e)
        }
    }

    private fun maybeForceOpenAlertScreen(
        context: Context,
        fullScreenPendingIntent: PendingIntent,
        fullScreenIntent: Intent,
        prayerName: String
    ) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager

        val isScreenInteractive = powerManager?.isInteractive == true
        val isKeyguardLocked = keyguardManager?.isKeyguardLocked == true

        // If the user is actively using the phone, force-open the alert activity now.
        if (isScreenInteractive && !isKeyguardLocked) {
            tryDirectStart(context, fullScreenIntent, prayerName)

            var sendSucceeded = false
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val sendOptions = ActivityOptions.makeBasic().apply {
                        setPendingIntentBackgroundActivityStartMode(
                            ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                        )
                        setPendingIntentBackgroundActivityLaunchAllowed(true)
                    }
                    fullScreenPendingIntent.send(
                        context,
                        0,
                        null,
                        null,
                        null,
                        null,
                        sendOptions.toBundle()
                    )
                } else {
                    fullScreenPendingIntent.send()
                }
                sendSucceeded = true
                Log.d(TAG, "🚀 Forced PrayerAlertActivity launch on unlocked screen for $prayerName")
            } catch (e: PendingIntent.CanceledException) {
                Log.w(TAG, "⚠️ PendingIntent canceled while forcing alert screen", e)
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Failed to force-open alert screen", e)
            }

            // Some devices return success from PendingIntent.send() but still keep Home on top.
            // Run a guarded fallback only when the alert activity is not actually visible.
            if (sendSucceeded) {
                Handler(Looper.getMainLooper()).postDelayed({
                    if (!isPrayerAlertOnTop(context)) {
                        tryDirectStart(context, fullScreenIntent, prayerName)
                    } else {
                        Log.d(TAG, "✅ PrayerAlertActivity already on top, skipping direct fallback")
                    }
                }, 250L)
            } else {
                tryDirectStart(context, fullScreenIntent, prayerName)
            }
        }
    }

    private fun tryDirectStart(
        context: Context,
        fullScreenIntent: Intent,
        prayerName: String
    ) {
        try {
            context.startActivity(Intent(fullScreenIntent))
            Log.d(TAG, "🚀 Direct PrayerAlertActivity launch requested for $prayerName")
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Direct PrayerAlertActivity start failed for $prayerName", e)
        }
    }

    private fun isPrayerAlertOnTop(context: Context): Boolean {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val topActivity = activityManager
                ?.appTasks
                ?.firstOrNull()
                ?.taskInfo
                ?.topActivity

            topActivity?.className == PrayerAlertActivity::class.java.name
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Failed to check top activity for prayer alert", e)
            false
        }
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

    private fun showCountdownNotification(
        context: Context,
        prayerName: String,
        prayerNameArabic: String,
        prayerTime: String
    ) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val intent = Intent(context, MainComposeActivity::class.java).apply {
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

    private fun isDuplicateAlert(alertKey: String): Boolean {
        val now = System.currentTimeMillis()
        synchronized(PrayerAlarmReceiver::class.java) {
            val duplicate =
                lastAlertKey == alertKey &&
                    (now - lastAlertTimestampMs) < DUPLICATE_ALERT_WINDOW_MS
            if (!duplicate) {
                lastAlertKey = alertKey
                lastAlertTimestampMs = now
            }
            return duplicate
        }
    }

    private fun isAlertTimingValid(prayerName: String, expectedTriggerAtMs: Long): Boolean {
        val now = System.currentTimeMillis()

        if (expectedTriggerAtMs > 0L) {
            return abs(now - expectedTriggerAtMs) <= VALID_ALERT_DRIFT_WINDOW_MS
        }

        // Backward compatibility for old pending intents that don't contain expected time.
        val savedTimings = prayerStorageManager.getSavedPrayerTimes() ?: return true
        val targetTime = getPrayerMillisForName(prayerName, savedTimings) ?: return true

        val candidates = listOf(
            targetTime,
            targetTime + 24 * 60 * 60 * 1000L,
            targetTime - 24 * 60 * 60 * 1000L
        )
        val minDiff = candidates.minOf { candidate -> abs(now - candidate) }
        return minDiff <= VALID_ALERT_DRIFT_WINDOW_MS
    }

    private fun getPrayerMillisForName(
        prayerName: String,
        timings: com.zakrni.app.clean.ui.models.PresentationTimings
    ): Long? {
        val prayerTimeText = when (prayerName.lowercase(Locale.ROOT)) {
            "fajr" -> timings.Fajr
            "dhuhr", "zuhr", "duhr" -> timings.Dhuhr
            "asr" -> timings.Asr
            "maghrib" -> timings.Maghrib
            "isha", "ishaa" -> timings.Isha
            else -> null
        } ?: return null

        return parsePrayerTimeToMillis(prayerTimeText)
    }

    private fun parsePrayerTimeToMillis(timeText: String): Long? {
        return try {
            val cleanTime = timeText.split(" ")[0]
            val parts = cleanTime.split(":")
            if (parts.size < 2) return null
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, minute)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
        } catch (_: Exception) {
            null
        }
    }
}
