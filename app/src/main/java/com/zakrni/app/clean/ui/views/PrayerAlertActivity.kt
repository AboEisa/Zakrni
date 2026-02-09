package com.zakrni.app.clean.ui.views

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.zakrni.app.R
import com.zakrni.app.clean.ui.utils.LocaleHelper
import com.zakrni.app.databinding.ActivityPrayerAlertBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrayerAlertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrayerAlertBinding
    private val clockHandler = Handler(Looper.getMainLooper())
    private lateinit var clockRunnable: Runnable

    companion object {
        const val EXTRA_PRAYER_NAME = "prayer_name"
        const val EXTRA_PRAYER_NAME_ARABIC = "prayer_name_arabic"
        const val EXTRA_PRAYER_TIME = "prayer_time"

        data class PrayerVerse(
            val arabicText: String,
            val arabicReference: String,
            val englishText: String,
            val englishReference: String
        )

        // Quran verses about prayer
        private val PRAYER_VERSES = listOf(
            PrayerVerse(
                "﴿ إِنَّ الصَّلَاةَ كَانَتْ عَلَى الْمُؤْمِنِينَ كِتَابًا مَّوْقُوتًا ﴾",
                "سورة النساء - آية 103",
                "Indeed, prayer has been decreed upon the believers at fixed times.",
                "Surah An-Nisa - Ayah 103"
            ),
            PrayerVerse(
                "﴿ حَافِظُوا عَلَى الصَّلَوَاتِ وَالصَّلَاةِ الْوُسْطَىٰ ﴾",
                "سورة البقرة - آية 238",
                "Guard strictly all prayers, especially the middle prayer.",
                "Surah Al-Baqarah - Ayah 238"
            ),
            PrayerVerse(
                "﴿ وَأَقِمِ الصَّلَاةَ طَرَفَيِ النَّهَارِ وَزُلَفًا مِّنَ اللَّيْلِ ﴾",
                "سورة هود - آية 114",
                "Establish prayer at both ends of the day and in parts of the night.",
                "Surah Hud - Ayah 114"
            ),
            PrayerVerse(
                "﴿ إِنَّ الصَّلَاةَ تَنْهَىٰ عَنِ الْفَحْشَاءِ وَالْمُنكَرِ ﴾",
                "سورة العنكبوت - آية 45",
                "Indeed, prayer restrains from indecency and wrongdoing.",
                "Surah Al-Ankabut - Ayah 45"
            ),
            PrayerVerse(
                "﴿ وَأَقِيمُوا الصَّلَاةَ وَآتُوا الزَّكَاةَ وَارْكَعُوا مَعَ الرَّاكِعِينَ ﴾",
                "سورة البقرة - آية 43",
                "Establish prayer, give zakah, and bow with those who bow.",
                "Surah Al-Baqarah - Ayah 43"
            ),
            PrayerVerse(
                "﴿ قَدْ أَفْلَحَ الْمُؤْمِنُونَ • الَّذِينَ هُمْ فِي صَلَاتِهِمْ خَاشِعُونَ ﴾",
                "سورة المؤمنون - آية 1-2",
                "Successful indeed are the believers, those who are humble in their prayers.",
                "Surah Al-Mu'minun - Ayah 1-2"
            )
        )

        fun start(
            context: Context,
            prayerName: String,
            prayerNameArabic: String,
            prayerTime: String = ""
        ) {
            val intent = Intent(context, PrayerAlertActivity::class.java).apply {
                putExtra(EXTRA_PRAYER_NAME, prayerName)
                putExtra(EXTRA_PRAYER_NAME_ARABIC, prayerNameArabic)
                putExtra(EXTRA_PRAYER_TIME, prayerTime)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            }
            context.startActivity(intent)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleHelper.enforceLtr(this)

        // Show on lock screen and turn screen on
        setupWindowFlags()

        binding = ActivityPrayerAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get prayer details from intent
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME)
            ?: getString(R.string.prayer_alert_default_prayer_name)
        val prayerNameArabic = intent.getStringExtra(EXTRA_PRAYER_NAME_ARABIC)
            ?: getString(R.string.prayer_alert_default_prayer_name_ar)
        val prayerTime = intent.getStringExtra(EXTRA_PRAYER_TIME) ?: ""

        setupUI(prayerName, prayerNameArabic, prayerTime)
        setupClickListeners()
        startAnimations()
    }

    private fun setupWindowFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Make fullscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }
    }

    private fun setupUI(prayerName: String, prayerNameArabic: String, prayerTime: String) {
        val currentLocale = resources.configuration.locales[0] ?: Locale.getDefault()
        val isArabic = LocaleHelper.isArabic(this)
        val randomVerse = PRAYER_VERSES.random()
        val timeFormat = SimpleDateFormat(
            getString(R.string.prayer_alert_time_pattern),
            currentLocale
        )
        val dateFormat = SimpleDateFormat(
            getString(R.string.prayer_alert_date_pattern),
            currentLocale
        )

        binding.apply {
            // Set prayer names
            tvPrayerNameArabic.text = getString(
                R.string.prayer_alert_prayer_name_arabic_format,
                prayerNameArabic
            )
            tvPrayerNameEnglish.text = getString(
                R.string.prayer_alert_prayer_name_english_format,
                prayerName
            )

            // Set current time (initial)
            tvCurrentTime.text = timeFormat.format(Date())

            // Set current date
            tvCurrentDate.text = dateFormat.format(Date())

            // Set random Quran verse
            tvQuranVerse.text = if (isArabic) randomVerse.arabicText else randomVerse.englishText
        }

        findViewById<TextView?>(R.id.tvQuranVerseReference)?.text = if (isArabic) {
            randomVerse.arabicReference
        } else {
            randomVerse.englishReference
        }

        // Start live clock - updates every second
        clockRunnable = object : Runnable {
            override fun run() {
                binding.tvCurrentTime.text = timeFormat.format(Date())
                clockHandler.postDelayed(this, 1000)
            }
        }
        clockHandler.postDelayed(clockRunnable, 1000)
    }

    private fun setupClickListeners() {
        binding.apply {
            btnClose.setOnClickListener {
                stopAlertAndFinish()
            }

            btnDismiss.setOnClickListener {
                stopAlertAndFinish()
            }

            btnOpenApp.setOnClickListener {
                stopAlert()
                // Open main app
                val intent = Intent(this@PrayerAlertActivity, HomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(intent)
                finish()
            }
        }
    }

    private fun startAnimations() {
        binding.apply {
            // Icon card animation - scale and fade in
            iconCard.alpha = 0f
            iconCard.scaleX = 0.5f
            iconCard.scaleY = 0.5f

            val iconScaleX = ObjectAnimator.ofFloat(iconCard, "scaleX", 0.5f, 1f)
            val iconScaleY = ObjectAnimator.ofFloat(iconCard, "scaleY", 0.5f, 1f)
            val iconAlpha = ObjectAnimator.ofFloat(iconCard, "alpha", 0f, 1f)

            AnimatorSet().apply {
                playTogether(iconScaleX, iconScaleY, iconAlpha)
                duration = 600
                interpolator = OvershootInterpolator(1.5f)
                start()
            }

            // Prayer name animation - slide in from bottom
            tvPrayerLabel.alpha = 0f
            tvPrayerLabel.translationY = 50f
            tvPrayerNameArabic.alpha = 0f
            tvPrayerNameArabic.translationY = 50f
            tvPrayerNameEnglish.alpha = 0f
            tvPrayerNameEnglish.translationY = 50f

            tvPrayerLabel.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(200)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

            tvPrayerNameArabic.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

            tvPrayerNameEnglish.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(400)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

            // Time card animation
            timeCard.alpha = 0f
            timeCard.translationY = 80f
            timeCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(500)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

            // Verse card animation
            verseCard.alpha = 0f
            verseCard.translationY = 80f
            verseCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(600)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

            // Buttons animation
            buttonsContainer.alpha = 0f
            buttonsContainer.translationY = 50f
            buttonsContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(700)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

            // Decorative circles rotation
            decorCircle1.animate()
                .rotation(360f)
                .setDuration(20000)
                .withEndAction {
                    decorCircle1.rotation = 0f
                    decorCircle1.animate()
                        .rotation(360f)
                        .setDuration(20000)
                        .start()
                }
                .start()

            decorCircle2.animate()
                .rotation(-360f)
                .setDuration(25000)
                .withEndAction {
                    decorCircle2.rotation = 0f
                    decorCircle2.animate()
                        .rotation(-360f)
                        .setDuration(25000)
                        .start()
                }
                .start()

            // Pulse animation for icon
            startPulseAnimation()
        }
    }

    private fun startPulseAnimation() {
        val pulseScaleX = ObjectAnimator.ofFloat(binding.iconCard, "scaleX", 1f, 1.05f, 1f)
        val pulseScaleY = ObjectAnimator.ofFloat(binding.iconCard, "scaleY", 1f, 1.05f, 1f)

        AnimatorSet().apply {
            playTogether(pulseScaleX, pulseScaleY)
            duration = 1500
            interpolator = AccelerateDecelerateInterpolator()
            addListener(object : android.animation.Animator.AnimatorListener {
                override fun onAnimationStart(animation: android.animation.Animator) {}
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    if (!isFinishing && !isDestroyed) {
                        start()
                    }
                }
                override fun onAnimationCancel(animation: android.animation.Animator) {}
                override fun onAnimationRepeat(animation: android.animation.Animator) {}
            })
            start()
        }
    }



    private fun stopAlert() {
        // No sound or vibration to stop
    }

    private fun stopAlertAndFinish() {
        stopAlert()
        // Cancel prayer alert notifications from both receiver (3001) and service (1002)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.cancel(3001) // PrayerAlarmReceiver.PRAYER_ALERT_NOTIFICATION_ID
        notificationManager.cancel(1002) // PrayerNotificationService.PRAYER_ALERT_NOTIFICATION_ID
        finish()
    }

    override fun onDestroy() {
        clockHandler.removeCallbacks(clockRunnable)
        stopAlert()
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        // Stop sound and vibration when activity goes to background
        stopAlert()
    }

    override fun onBackPressed() {
        stopAlertAndFinish()
    }
}
