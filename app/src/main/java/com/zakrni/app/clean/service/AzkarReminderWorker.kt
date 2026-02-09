package com.zakrni.app.clean.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.zakrni.app.R
import com.zakrni.app.clean.App
import com.zakrni.app.clean.ui.utils.LocaleHelper
import com.zakrni.app.clean.ui.utils.ThemeManager
import com.zakrni.app.clean.ui.views.HomeActivity
import java.util.concurrent.TimeUnit

/**
 * WorkManager Worker that sends periodic azkar/dua notification reminders.
 * Runs every hour and sends a random dhikr or dua as a notification.
 */
class AzkarReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        const val TAG = "AzkarReminderWorker"
        const val WORK_NAME = "azkar_reminder_work"
        private const val NOTIFICATION_ID = 4001
        private const val PREF_LAST_INDEX = "azkar_last_index"

        /**
         * Schedule periodic azkar reminders (every 1 hour)
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .build()

            val workRequest = PeriodicWorkRequest.Builder(
                AzkarReminderWorker::class.java,
                1, TimeUnit.HOURS,          // repeat every 1 hour
                15, TimeUnit.MINUTES        // flex interval
            )
                .setConstraints(constraints)
                .setInitialDelay(30, TimeUnit.MINUTES) // first reminder after 30 min
                .addTag(WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Don't restart if already running
                workRequest
            )

            Log.d(TAG, "✅ Azkar reminder scheduled (every 1 hour)")
        }

        /**
         * Cancel all azkar reminders
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "❌ Azkar reminders cancelled")
        }
    }

    override fun doWork(): Result {
        // Check if reminders are still enabled
        if (!ThemeManager.isNotificationsEnabled(context) ||
            !ThemeManager.isAzkarRemindersEnabled(context)) {
            Log.d(TAG, "⚠️ Azkar reminders disabled, skipping")
            return Result.success()
        }

        // Get next dhikr (rotating through all azkar to avoid repetition)
        val prefs = context.getSharedPreferences("azkar_prefs", Context.MODE_PRIVATE)
        val lastIndex = prefs.getInt(PREF_LAST_INDEX, 0)
        val dhikr = AzkarReminderData.getDhikrByIndex(lastIndex)
        prefs.edit().putInt(PREF_LAST_INDEX, lastIndex + 1).apply()

        showNotification(dhikr)

        Log.d(TAG, "✅ Azkar reminder sent: ${dhikr.text.take(50)}...")
        return Result.success()
    }

    private fun showNotification(dhikr: AzkarReminderData.Dhikr) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val isArabic = LocaleHelper.isArabic(context)

        // Intent to open app when notification is tapped
        val intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (isArabic) {
            dhikr.text
        } else {
            "Remember Allah and keep your heart connected."
        }

        val summaryText = if (isArabic) {
            dhikr.source
        } else {
            "Zikr reminder"
        }

        val notification = NotificationCompat.Builder(context, App.AZKAR_CHANNEL_ID)
            .setContentTitle(if (isArabic) "📿 ذِكْر" else "📿 Zikr")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(contentText)
                .setSummaryText(summaryText))
            .setSmallIcon(R.drawable.ic_dua)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setGroup("azkar_group")
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
