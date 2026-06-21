package com.zakrni.app.clean.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.zakrni.app.R
import com.zakrni.app.clean.App
import com.zakrni.app.clean.ui.compose.MainComposeActivity
import com.zakrni.app.clean.ui.utils.LocaleHelper
import com.zakrni.app.clean.ui.utils.ThemeManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Once-a-day helpful nudge: encourages finishing a reading/khatma, tasbih, dua, etc., and on
 * Mondays/Thursdays reminds the user it's a sunnah fasting day. Rotates the message daily.
 */
class DailyReminderWorker(
    private val context: Context,
    params: WorkerParameters,
) : Worker(context, params) {

    companion object {
        const val TAG = "DailyReminderWorker"
        const val WORK_NAME = "daily_reminder_work"
        private const val NOTIFICATION_ID = 4101
        private const val PREFS = "daily_reminder_prefs"
        private const val PREF_INDEX = "idx"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder().setRequiresBatteryNotLow(false).build()
            val request = PeriodicWorkRequest.Builder(
                DailyReminderWorker::class.java,
                1, TimeUnit.DAYS,
                3, TimeUnit.HOURS,
            )
                .setConstraints(constraints)
                .setInitialDelay(2, TimeUnit.HOURS)
                .addTag(WORK_NAME)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
            Log.d(TAG, "✅ Daily reminder scheduled")
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override fun doWork(): Result {
        if (!ThemeManager.isNotificationsEnabled(context)) {
            return Result.success()
        }
        val arabic = LocaleHelper.isArabic(context)
        val (title, text) = pickMessage(arabic)
        showNotification(title, text)
        return Result.success()
    }

    private fun pickMessage(arabic: Boolean): Pair<String, String> {
        val dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        if (dow == Calendar.MONDAY || dow == Calendar.THURSDAY) {
            return if (arabic) {
                "🌙 صيام السنة" to "النهاردة سنة صيام (الاثنين والخميس) — تقبّل الله طاعتك."
            } else {
                "🌙 Sunnah fasting" to "Today is a sunnah fasting day (Monday / Thursday)."
            }
        }
        val nudges = if (arabic) arabicNudges else englishNudges
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val index = prefs.getInt(PREF_INDEX, 0)
        prefs.edit().putInt(PREF_INDEX, index + 1).apply()
        return nudges[index % nudges.size]
    }

    private fun showNotification(title: String, text: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, MainComposeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, App.AZKAR_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setSmallIcon(R.drawable.ic_dua)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    private val arabicNudges = listOf(
        "📖 وِردك في انتظارك" to "افتح المصحف وكمّل قراءتك النهاردة — ولو صفحة.",
        "🏁 كمّل ختمتك" to "جزء النهاردة من ختمتك مستنيك في القرآن.",
        "📿 سبّح وكبّر" to "خُد لحظة دلوقتي وافتح السبحة واذكر الله.",
        "🤲 ادعُ الله" to "ارفع إيدك بالدعاء — افتح الأدعية واختار اللي يناسبك.",
        "🌅 أذكار اليوم" to "لا تنسَ أذكار الصباح/المساء، حصّن نفسك بذكر الله.",
        "💬 حديث اليوم" to "اقرأ حديثًا شريفًا يقرّبك من السنة.",
    )

    private val englishNudges = listOf(
        "📖 Your daily reading" to "Open the Quran and continue your reading today — even one page.",
        "🏁 Continue your khatma" to "Today's juz of your khatma is waiting for you.",
        "📿 Tasbih time" to "Take a moment now and remember Allah with the tasbih.",
        "🤲 Make dua" to "Raise your hands in dua — open the supplications.",
        "🌅 Today's azkar" to "Don't forget your morning/evening azkar.",
        "💬 Hadith of the day" to "Read a hadith and connect with the Sunnah.",
    )
}
