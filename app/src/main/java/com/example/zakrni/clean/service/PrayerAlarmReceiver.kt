package com.example.zakrni.clean.service

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.zakrni.R
import com.example.zakrni.clean.App
import com.example.zakrni.clean.ui.views.HomeActivity
import com.example.zakrni.clean.ui.utils.PrayerStorageManager

class PrayerAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PRAYER_ALERT = "com.example.zakrni.PRAYER_ALERT"
        const val ACTION_COUNTDOWN_NOTIFICATION = "com.example.zakrni.COUNTDOWN_NOTIFICATION"

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
                showPrayerAlertNotification(context, prayerName, prayerNameArabic)
            }

            ACTION_COUNTDOWN_NOTIFICATION -> {
                val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: return
                val prayerNameArabic = intent.getStringExtra(EXTRA_PRAYER_NAME_ARABIC) ?: return
                val prayerTime = intent.getStringExtra(EXTRA_PRAYER_TIME) ?: return
                showCountdownNotification(context, prayerName, prayerNameArabic, prayerTime)
            }

            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                rescheduleAlarms(context)
            }
        }
    }

    /**
     * Show prayer alert notification with sound and vibration
     */
    @SuppressLint("FullScreenIntentPolicy")
    private fun showPrayerAlertNotification(
        context: Context,
        prayerName: String,
        prayerNameArabic: String
    ) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create intent to open app when notification is tapped
            val intent = Intent(context, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Get default notification sound
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val notification = NotificationCompat.Builder(context, App.PRAYER_CHANNEL_ID)
                .setContentTitle("🕌 وقت الصلاة - Prayer Time")
                .setContentText("حان وقت صلاة $prayerNameArabic - Time for $prayerName prayer")
                .setSmallIcon(R.drawable.ic_dua)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(defaultSoundUri)
                .setVibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000)) // Enhanced vibration pattern
                .setLights(0xFF00FF00.toInt(), 1000, 500) // Green light
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("حان الآن وقت صلاة $prayerNameArabic ($prayerName). أدِ الصلاة في وقتها المبارك.")
                )
                .setFullScreenIntent(pendingIntent, true)
                .build()

            // Use unique ID for each prayer to avoid replacing previous notifications
            val notificationId = PRAYER_ALERT_NOTIFICATION_ID + prayerName.hashCode()
            notificationManager.notify(notificationId, notification)
        } catch (e: Exception) {
            Toast.makeText(context, "Error showing prayer alert notification", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Show countdown notification (10 minutes before prayer)
     */
    private fun showCountdownNotification(
        context: Context,
        prayerName: String,
        prayerNameArabic: String,
        prayerTime: String
    ) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create intent to open app
            val intent = Intent(context, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, App.PRAYER_CHANNEL_ID)
                .setContentTitle("⏰ تنبيه قبل الصلاة - Prayer Reminder")
                .setContentText("صلاة $prayerNameArabic خلال 10 دقائق - $prayerName prayer in 10 minutes")
                .setSmallIcon(R.drawable.ic_dua)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("صلاة $prayerNameArabic ستبدأ في الساعة $prayerTime. استعد للصلاة.")
                )
                .build()

            val notificationId = COUNTDOWN_NOTIFICATION_ID + prayerName.hashCode()
            notificationManager.notify(notificationId, notification)
        } catch (e: Exception) {
            Toast.makeText(context, "Error showing countdown notification", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Reschedule alarms after device reboot or app update
     */
    private fun rescheduleAlarms(context: Context) {
        try {
            val storageManager = PrayerStorageManager(context)
            val savedTimings = storageManager.getSavedPrayerTimes()

            if (savedTimings != null && storageManager.areSavedTimesValid()) {
                val alarmManager = PrayerAlarmManager(context)
                alarmManager.scheduleAllPrayerAlarms(savedTimings)
            } else {
               Toast.makeText(context, "❌ Error rescheduling alarms after reboot", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
           Toast.makeText(context, "❌ Error rescheduling alarms after reboot", Toast.LENGTH_SHORT).show()
        }
    }
}