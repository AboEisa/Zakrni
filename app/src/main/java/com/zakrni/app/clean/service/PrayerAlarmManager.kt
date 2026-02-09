package com.zakrni.app.clean.service

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.zakrni.app.clean.ui.models.PresentationTimings
import com.zakrni.app.clean.ui.utils.NetworkManager
import com.zakrni.app.clean.ui.utils.PrayerStorageManager
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrayerAlarmManager @Inject constructor(
    private val context: Context,
    private val networkManager: NetworkManager,
    private val prayerStorageManager: PrayerStorageManager
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        private const val TAG = "PrayerAlarmManager"

        // Request codes for different prayers
        const val FAJR_REQUEST_CODE = 1001
        const val DHUHR_REQUEST_CODE = 1002
        const val ASR_REQUEST_CODE = 1003
        const val MAGHRIB_REQUEST_CODE = 1004
        const val ISHA_REQUEST_CODE = 1005

        // Request codes for countdown notifications (10 minutes before)
        const val FAJR_COUNTDOWN_REQUEST_CODE = 2001
        const val DHUHR_COUNTDOWN_REQUEST_CODE = 2002
        const val ASR_COUNTDOWN_REQUEST_CODE = 2003
        const val MAGHRIB_COUNTDOWN_REQUEST_CODE = 2004
        const val ISHA_COUNTDOWN_REQUEST_CODE = 2005

        // Request codes for automatic next prayer scheduling
        const val AUTO_SCHEDULE_REQUEST_CODE = 3001
    }

    /**
     * Schedule all prayer alarms for today (only if online)
     */
    fun scheduleAllPrayerAlarms(timings: PresentationTimings) {
        try {
            Log.d(TAG, "📅 Starting to schedule prayer alarms")

            // Save prayer times for later use (always save regardless of notification setting)
            prayerStorageManager.savePrayerTimes(timings)

            // Check if prayer notifications are enabled
            if (!com.zakrni.app.clean.ui.utils.ThemeManager.isPrayerNotificationsEnabled(context)) {
                Log.d(TAG, "⚠️ Prayer notifications disabled — skipping alarm scheduling")
                return
            }

            // Cancel previous alarms first
            cancelAllAlarms()

            val prayers = listOf(
                Triple("Fajr", "الفجر", timings.Fajr) to Pair(FAJR_REQUEST_CODE, FAJR_COUNTDOWN_REQUEST_CODE),
                Triple("Dhuhr", "الظهر", timings.Dhuhr) to Pair(DHUHR_REQUEST_CODE, DHUHR_COUNTDOWN_REQUEST_CODE),
                Triple("Asr", "العصر", timings.Asr) to Pair(ASR_REQUEST_CODE, ASR_COUNTDOWN_REQUEST_CODE),
                Triple("Maghrib", "المغرب", timings.Maghrib) to Pair(MAGHRIB_REQUEST_CODE, MAGHRIB_COUNTDOWN_REQUEST_CODE),
                Triple("Isha", "العشاء", timings.Isha) to Pair(ISHA_REQUEST_CODE, ISHA_COUNTDOWN_REQUEST_CODE)
            )

            val currentTime = System.currentTimeMillis()
            var scheduledCount = 0

            prayers.forEach { (prayerInfo, requestCodes) ->
                val (name, nameArabic, timeString) = prayerInfo
                val (prayerRequestCode, countdownRequestCode) = requestCodes

                val prayerTimeMillis = getPrayerTimeInMillis(timeString)

                // Only schedule if prayer time is in the future
                if (prayerTimeMillis > currentTime) {
                    // Schedule prayer alert
                    val prayerScheduled = schedulePrayerAlert(
                        name = name,
                        nameArabic = nameArabic,
                        timeInMillis = prayerTimeMillis,
                        requestCode = prayerRequestCode
                    )

                    // Schedule countdown notification (10 minutes before)
                    val countdownTime = prayerTimeMillis - (10 * 60 * 1000) // 10 minutes before
                    var countdownScheduled = false
                    if (countdownTime > currentTime) {
                        countdownScheduled = scheduleCountdownNotification(
                            name = name,
                            nameArabic = nameArabic,
                            timeInMillis = countdownTime,
                            prayerTimeString = formatTime(prayerTimeMillis),
                            requestCode = countdownRequestCode
                        )
                    }

                    if (prayerScheduled) {
                        scheduledCount++
                        Log.d(TAG, "✅ Scheduled $name prayer at ${formatTime(prayerTimeMillis)} (Prayer: $prayerScheduled, Countdown: $countdownScheduled)")
                    }
                } else {
                    Log.d(TAG, "⏰ Skipped $name prayer (already passed) - Time was: ${formatTime(prayerTimeMillis)}")
                }
            }

            Log.d(TAG, "📅 Total prayers scheduled for today: $scheduledCount")

            // Schedule next day's Fajr if all today's prayers have passed
            scheduleNextDayFajrIfNeeded(timings)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error scheduling prayer alarms", e)
        }
    }

    /**
     * 🆕 Schedule automatic alarm to reschedule next day prayers at midnight
     */
    fun scheduleAutoRescheduleForNextDay() {
        try {
            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 5) // 5 minutes after midnight
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = PrayerAlarmReceiver.ACTION_AUTO_RESCHEDULE
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                AUTO_SCHEDULE_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            scheduleExactAlarm(calendar.timeInMillis, pendingIntent)
            Log.d(TAG, "⏰ Auto reschedule set for tomorrow at ${formatTime(calendar.timeInMillis)}")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error scheduling auto reschedule", e)
        }
    }

    /**
     * 🚀 Schedule prayer alarms when back online
     */
    fun scheduleAlarmsWhenBackOnline(timings: PresentationTimings) {
        Log.d(TAG, "🌐 Back online! Rescheduling prayer alarms...")
        scheduleAllPrayerAlarms(timings)
    }

    /**
     * Schedule a specific prayer alert (only if online)
     */
    private fun schedulePrayerAlert(
        name: String,
        nameArabic: String,
        timeInMillis: Long,
        requestCode: Int
    ): Boolean {
        return try {
            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = PrayerAlarmReceiver.ACTION_PRAYER_ALERT
                putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, name)
                putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME_ARABIC, nameArabic)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Use setAlarmClock for prayer alerts - this guarantees:
            // 1. Device will wake up from doze
            // 2. App gets BAL (Background Activity Launch) exemption
            // 3. Full-screen intent will work reliably
            val showIntent = PendingIntent.getActivity(
                context,
                requestCode + 1000,
                Intent(context, com.zakrni.app.clean.ui.views.HomeActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(timeInMillis, showIntent),
                pendingIntent
            )
            Log.d(TAG, "🔔 Prayer alert scheduled (AlarmClock) for $name at ${formatTime(timeInMillis)}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to schedule prayer alert for $name", e)
            false
        }
    }

    /**
     * Schedule countdown notification (10 minutes before prayer) - only if online
     */
    private fun scheduleCountdownNotification(
        name: String,
        nameArabic: String,
        timeInMillis: Long,
        prayerTimeString: String,
        requestCode: Int
    ): Boolean {
        return try {
            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = PrayerAlarmReceiver.ACTION_COUNTDOWN_NOTIFICATION
                putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, name)
                putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME_ARABIC, nameArabic)
                putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_TIME, prayerTimeString)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            scheduleExactAlarm(timeInMillis, pendingIntent)
            Log.d(TAG, "⏰ Countdown notification scheduled for $name at ${formatTime(timeInMillis)}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to schedule countdown notification for $name", e)
            false
        }
    }

    /**
     * Schedule exact alarm with proper API handling
     */
    private fun scheduleExactAlarm(timeInMillis: Long, pendingIntent: PendingIntent) {
        try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    // Android 12+ - Check if exact alarms are allowed
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            timeInMillis,
                            pendingIntent
                        )
                        Log.d(TAG, "✅ Exact alarm scheduled successfully")
                    } else {
                        // Fallback to inexact alarm if exact alarms not allowed
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            timeInMillis,
                            pendingIntent
                        )
                        Log.w(TAG, "⚠️ Exact alarms not allowed, using inexact alarm")
                    }
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        timeInMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "✅ Exact alarm scheduled for API 23+")
                }
                else -> {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        timeInMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "✅ Exact alarm scheduled for API < 23")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error scheduling exact alarm", e)
            throw e
        }
    }

    /**
     * Schedule next day's Fajr if all today's prayers have passed (only if online)
     */
    private fun scheduleNextDayFajrIfNeeded(timings: PresentationTimings) {
        val currentTime = System.currentTimeMillis()
        val ishaTime = getPrayerTimeInMillis(timings.Isha)

        // If Isha has passed, schedule tomorrow's Fajr
        if (currentTime > ishaTime) {
            val tomorrowFajrTime = getTomorrowFajrTime(timings.Fajr)

            val prayerScheduled = schedulePrayerAlert(
                name = "Fajr",
                nameArabic = "الفجر",
                timeInMillis = tomorrowFajrTime,
                requestCode = FAJR_REQUEST_CODE + 100 // Different request code for tomorrow
            )

            // Schedule countdown for tomorrow's Fajr
            val countdownTime = tomorrowFajrTime - (10 * 60 * 1000)
            var countdownScheduled = false
            if (countdownTime > currentTime) {
                countdownScheduled = scheduleCountdownNotification(
                    name = "Fajr",
                    nameArabic = "الفجر",
                    timeInMillis = countdownTime,
                    prayerTimeString = formatTime(tomorrowFajrTime),
                    requestCode = FAJR_COUNTDOWN_REQUEST_CODE + 100
                )
            }

            Log.d(TAG, "🌅 Scheduled tomorrow's Fajr at ${formatTime(tomorrowFajrTime)} (Prayer: $prayerScheduled, Countdown: $countdownScheduled)")
        }

        // Schedule auto reschedule for next day
        scheduleAutoRescheduleForNextDay()
    }

    /**
     * Cancel all scheduled prayer alarms
     */
    fun cancelAllAlarms() {
        val requestCodes = listOf(
            FAJR_REQUEST_CODE, DHUHR_REQUEST_CODE, ASR_REQUEST_CODE,
            MAGHRIB_REQUEST_CODE, ISHA_REQUEST_CODE,
            FAJR_COUNTDOWN_REQUEST_CODE, DHUHR_COUNTDOWN_REQUEST_CODE,
            ASR_COUNTDOWN_REQUEST_CODE, MAGHRIB_COUNTDOWN_REQUEST_CODE,
            ISHA_COUNTDOWN_REQUEST_CODE,
            // Tomorrow's codes
            FAJR_REQUEST_CODE + 100, FAJR_COUNTDOWN_REQUEST_CODE + 100,
            // Auto schedule code
            AUTO_SCHEDULE_REQUEST_CODE
        )

        requestCodes.forEach { requestCode ->
            try {
                val intent = Intent(context, PrayerAlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Error cancelling alarm with request code: $requestCode", e)
            }
        }

        Log.d(TAG, "🗑️ Cancelled all prayer alarms")
    }

    /**
     * Check if exact alarms permission is granted (Android 12+)
     */
    fun hasExactAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true // No permission needed for older versions
        }
    }

    /**
     * Check if full-screen intent permission is granted (Android 14+)
     * Required for prayer alert to appear as full-screen activity over lock screen.
     */
    fun hasFullScreenIntentPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.canUseFullScreenIntent()
        } else {
            true // Automatically granted on Android 13 and below
        }
    }

    /**
     * Request full-screen intent permission (Android 14+)
     */
    fun requestFullScreenIntentPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && !hasFullScreenIntentPermission()) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    /**
     * Request exact alarm permission (Android 12+)
     */
    fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !hasExactAlarmPermission()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    /**
     * Convert prayer time string to milliseconds for today
     */
    private fun getPrayerTimeInMillis(timeString: String): Long {
        return try {
            val cleanTime = timeString.split(" ")[0] // Remove timezone if present
            val timeParts = cleanTime.split(":")
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()

            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing time: $timeString", e)
            0L
        }
    }

    /**
     * Get tomorrow's Fajr time
     */
    private fun getTomorrowFajrTime(fajrTimeString: String): Long {
        return try {
            val cleanTime = fajrTimeString.split(" ")[0]
            val timeParts = cleanTime.split(":")
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()

            Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 1) // Tomorrow
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating tomorrow's Fajr time", e)
            0L
        }
    }

    /**
     * Format time in milliseconds to readable 12-hour format
     */
    private fun formatTime(timeInMillis: Long): String {
        val calendar = Calendar.getInstance().apply { this.timeInMillis = timeInMillis }
        return java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ENGLISH).format(calendar.time)
    }
}