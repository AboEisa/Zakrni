package com.example.zakrni.clean

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
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

            // Prayer times channel for persistent notification
            val prayerChannel = NotificationChannel(
                PRAYER_CHANNEL_ID,
                "أوقات الصلاة",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "إشعارات أوقات الصلاة مع العد التنازلي"
                lightColor = Color.GREEN
                setSound(null, null)
                enableVibration(false)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            // General app notifications channel
            val generalChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "إشعارات التطبيق",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "الإشعارات العامة للتطبيق"
                lightColor = Color.BLUE
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(prayerChannel)
            notificationManager.createNotificationChannel(generalChannel)
        }
    }
}