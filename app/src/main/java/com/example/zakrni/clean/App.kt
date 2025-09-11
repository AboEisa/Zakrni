package com.example.zakrni.clean

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application() {

    companion object {
        const val PRAYER_CHANNEL_ID = "PRAYER_TIMES_CHANNEL"
        const val NOTIFICATION_CHANNEL_ID = "APP_CHANNEL"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Prayer times channel for prayer alerts
            val prayerChannel = NotificationChannel(
                PRAYER_CHANNEL_ID,
                "أوقات الصلاة - Prayer Times",
                NotificationManager.IMPORTANCE_HIGH // Changed to HIGH for prayer alerts
            ).apply {
                description = "إشعارات أوقات الصلاة والتنبيهات"
                lightColor = Color.GREEN

                // Set custom sound
                val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                setSound(defaultSoundUri, AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build())

                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                enableLights(true)
            }

            // General app notifications channel
            val generalChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "إشعارات التطبيق - App Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "الإشعارات العامة للتطبيق"
                lightColor = Color.BLUE
                setShowBadge(true)
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(prayerChannel)
            notificationManager.createNotificationChannel(generalChannel)
        }
    }
}