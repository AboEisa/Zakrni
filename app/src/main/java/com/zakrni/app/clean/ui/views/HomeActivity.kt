package com.zakrni.app.clean.ui.views

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.zakrni.app.R
import com.zakrni.app.clean.ads.AdManager
import com.zakrni.app.clean.ads.SubscriptionManager
import com.zakrni.app.clean.ui.models.PresentationPrayerTimesResponse
import com.zakrni.app.clean.ui.utils.DialogStyler
import com.zakrni.app.clean.ui.utils.LocaleHelper
import com.zakrni.app.clean.ui.utils.NetworkManager
import com.zakrni.app.clean.ui.utils.PrayerTimeUtils
import com.zakrni.app.clean.ui.utils.ThemeManager
import com.zakrni.app.clean.ui.viewmodels.PrayerTimesViewModel
import com.zakrni.app.clean.ui.viewmodels.QuranViewModel
import com.zakrni.app.databinding.ActivityHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController

    private val prayerTimeViewModel: PrayerTimesViewModel by viewModels()
    private val quranViewModel: QuranViewModel by viewModels()

    @Inject
    lateinit var networkManager: NetworkManager

    @Inject
    lateinit var adManager: AdManager

    @Inject
    lateinit var subscriptionManager: SubscriptionManager

    // Navigation counter for interstitial ads
    private var navigationCount = 0
    private val INTERSTITIAL_EVERY_N_NAVIGATIONS = 3

    // Fast swipe gesture detector
    private lateinit var gestureDetector: GestureDetector

    // Dots indicator with sequential tracking
    private val dots = mutableListOf<ImageView>()
    private var currentDotIndex = 0 // Track current dot position independent of ayah index
    private val maxDotsToShow = 10 // Fixed number of visible dots

    // Fast animation properties
    private var isAnimating = false
    private var initialX = 0f
    private var isUserInteracting = false

    // Fast auto-swipe handler
    private val autoSwipeHandler = Handler(Looper.getMainLooper())
    private var autoSwipeRunnable: Runnable? = null
    private val autoSwipeDelay = 10000L // 10 seconds
    private var isAutoSwipeEnabled = true

    // Define which fragments should be full-screen
    private val fullScreenFragments = mapOf(
        R.id.hadithFragment to { HadithFragment() },
        R.id.duaFragment to { DuaFragment() },
        R.id.azkarFragment to { AzkarFragment() },
        R.id.quranFragment to { QuranFragment() },
        R.id.allahNamesFragment to { AllahNamesFragment() },
        R.id.tasbehFragment to { TasbehFragment() },
        R.id.quran2Fragment to { Quran2Fragment() },
        R.id.articleFragment to { ArticleFragment() },
        R.id.settingsFragment to { SettingsFragment() },
        R.id.allPrayerTimesFragment to { AllPrayerTimesFragment() }
    )

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                prayerTimeViewModel.checkLocationPermission()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                prayerTimeViewModel.checkLocationPermission()
            }
            else -> {
                showLocationPermissionDenied()
            }
        }
    }

    private val navOptions by lazy {
        NavOptions.Builder()
            .setEnterAnim(R.anim.animation)
            .setExitAnim(R.anim.animation2)
            .setPopEnterAnim(R.anim.animation3)
            .setPopExitAnim(R.anim.animation4)
            .build()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleHelper.enforceLtr(this)
        enableEdgeToEdge()
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        setupNavigation()
        setupCustomBottomNavigation()
        setupPrayerTimesObservers()
        setupDailyAyahObservers()
        setupNetworkObserver()
        setupDailyAyahSwipeGestures()
        setupSequentialDotsIndicator()
        startAutoSwipe()
        checkLocationPermissions()
        setupSettingsButton()
        checkFullScreenIntentPermission()

    }

    // Fast auto-swipe functionality
    private fun startAutoSwipe() {
        if (!isAutoSwipeEnabled) return

        stopAutoSwipe()

        autoSwipeRunnable = Runnable {
            if (!isUserInteracting && !isAnimating && isAutoSwipeEnabled) {
                performFastAutoSlideLeft()
            }
            startAutoSwipe()
        }

        autoSwipeHandler.postDelayed(autoSwipeRunnable!!, autoSwipeDelay)
    }

    private fun stopAutoSwipe() {
        autoSwipeRunnable?.let { runnable ->
            autoSwipeHandler.removeCallbacks(runnable)
        }
        autoSwipeRunnable = null
    }

    private fun resetAutoSwipeTimer() {
        if (isAutoSwipeEnabled) {
            startAutoSwipe()
        }
    }

    // Fast auto slide left
    private fun performFastAutoSlideLeft() {
        if (isAnimating || isUserInteracting) return

        isAnimating = true

        // Fast slide left animation for auto-swipe
        fastSlideCardLeft(isAutomatic = true) {
            quranViewModel.loadNextAyah()
            moveToNextDot() // 🆕 Move dot forward

            // Fast slide in new content from right
            fastSlideCardInFromRight(isAutomatic = true) {
                isAnimating = false
            }
        }
    }

    // Toggle auto-swipe feature
    private fun toggleAutoSwipe() {
        isAutoSwipeEnabled = !isAutoSwipeEnabled
        if (isAutoSwipeEnabled) {
            startAutoSwipe()
            showToast(getString(R.string.auto_swipe_enabled))
        } else {
            stopAutoSwipe()
            showToast(getString(R.string.auto_swipe_disabled))
        }
    }

    // Setup Daily Ayah Observers
    private fun setupDailyAyahObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    quranViewModel.dailyAyah.collectLatest { dailyAyah ->
                        dailyAyah?.let {
                            updateDailyAyahUI(it)
                            // Don't call updateDotsIndicator here - dots move independently
                        }
                    }
                }

                launch {
                    quranViewModel.isDailyAyahLoading.collectLatest { isLoading ->
                        if (isLoading) {
                            binding.dailyAyahTitle.text = getString(R.string.loading_text)
                        }
                    }
                }
            }
        }
    }

    // Setup Sequential Dots Indicator (independent of ayah count)
    private fun setupSequentialDotsIndicator() {
        binding.dotsContainer.removeAllViews()
        dots.clear()
        currentDotIndex = 0

        // Create fixed number of dots
        for (i in 0 until maxDotsToShow) {
            val dot = ImageView(this).apply {
                setImageResource(R.drawable.dot_inactive)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(6, 0, 6, 0)
                }
            }

            dots.add(dot)
            binding.dotsContainer.addView(dot)
        }

        // Set first dot as active
        updateSequentialDotsIndicator()
    }

    // Update dots indicator sequentially
    private fun updateSequentialDotsIndicator() {
        if (dots.isEmpty()) return

        // Reset all dots to inactive
        dots.forEach { it.setImageResource(R.drawable.dot_inactive) }

        // Activate current dot
        if (currentDotIndex in 0 until dots.size) {
            dots[currentDotIndex].setImageResource(R.drawable.dot_active)
            animateActiveDot(dots[currentDotIndex])
        }
    }

    // Move to next dot
    private fun moveToNextDot() {
        currentDotIndex = (currentDotIndex + 1) % maxDotsToShow
        updateSequentialDotsIndicator()
        animateDotsMovement(true)
    }

    // Move to previous dot
    private fun moveToPreviousDot() {
        currentDotIndex = if (currentDotIndex - 1 < 0) {
            maxDotsToShow - 1
        } else {
            currentDotIndex - 1
        }
        updateSequentialDotsIndicator()
        animateDotsMovement(false)
    }

    // Animate dots movement
    private fun animateDotsMovement(isNext: Boolean) {
        val slideDistance = if (isNext) -15f else 15f
        val safeBinding = _binding ?: return

        safeBinding.dotsContainer.animate()
            .translationX(slideDistance)
            .scaleX(0.95f)
            .alpha(0.8f)
            .setDuration(200L)
            .withEndAction {
                val currentBinding = _binding ?: return@withEndAction
                currentBinding.dotsContainer.animate()
                    .translationX(0f)
                    .scaleX(1f)
                    .alpha(1f)
                    .setDuration(250L)
                    .setInterpolator(OvershootInterpolator(0.3f))
                    .start()
            }
            .start()
    }

    // Animate active dot
    private fun animateActiveDot(dot: ImageView) {
        val scaleUp = ObjectAnimator.ofFloat(dot, "scaleX", 1f, 1.25f).apply {
            duration = 150
        }
        val scaleUpY = ObjectAnimator.ofFloat(dot, "scaleY", 1f, 1.25f).apply {
            duration = 150
        }
        val scaleDown = ObjectAnimator.ofFloat(dot, "scaleX", 1.25f, 1f).apply {
            duration = 150
            startDelay = 100
        }
        val scaleDownY = ObjectAnimator.ofFloat(dot, "scaleY", 1.25f, 1f).apply {
            duration = 150
            startDelay = 100
        }

        AnimatorSet().apply {
            playTogether(scaleUp, scaleUpY, scaleDown, scaleDownY)
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    // Update Daily Ayah UI
    private fun updateDailyAyahUI(dailyAyah: QuranViewModel.DailyAyahData) {
        with(binding) {
            val isArabicUi = LocaleHelper.isArabic(this@HomeActivity)
            dailyAyahTitle.text = getString(R.string.surah_format, dailyAyah.surahName)

            if (isArabicUi) {
                ayahArabic.visibility = View.VISIBLE
                ayahArabic.text = dailyAyah.ayahText
                ayahTranslation.visibility = View.VISIBLE
                ayahTranslation.text = dailyAyah.translation ?: "الترجمة غير متوفرة"
            } else {
                // In English mode keep the ayah card fully English.
                ayahArabic.visibility = View.VISIBLE
                ayahArabic.text = dailyAyah.translation ?: "Translation not available"
                ayahTranslation.visibility = View.GONE
            }

            val ayahSourceText = quranViewModel.getFormattedAyahSource(dailyAyah)
            ayahSource.text = ayahSourceText
        }
    }

    // Setup Super Fast Swipe Gestures
    private fun setupDailyAyahSwipeGestures() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {

            private val swipeThreshold = 80
            private val swipeVelocityThreshold = 80

            override fun onDown(e: MotionEvent): Boolean {
                isUserInteracting = true
                initialX = e.x
                return true
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (isAnimating) return false

                val deltaX = e2.x - initialX
                if (abs(deltaX) > abs(e2.y - (e1?.y ?: 0f))) {
                    // Faster, more responsive movement
                    val maxTranslation = 120f // Reduced max translation
                    val translation = deltaX.coerceIn(-maxTranslation, maxTranslation)

                    binding.dailyAyahCard.translationX = translation

                    // Faster alpha change
                    val alpha = 1f - (abs(translation) / maxTranslation) * 0.3f
                    binding.dailyAyahCard.alpha = alpha

                    return true
                }
                return false
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null || isAnimating) return false

                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x

                return if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > swipeThreshold && abs(velocityX) > swipeVelocityThreshold) {
                        resetAutoSwipeTimer()

                        if (diffX > 0) {
                            onFastSwipeRight()
                        } else {
                            onFastSwipeLeft()
                        }
                        true
                    } else {
                        fastResetCardPosition()
                        false
                    }
                } else {
                    fastResetCardPosition()
                    false
                }
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (!isAnimating) {
                    showAyahInteractionHint()
                    fastAnimateCardPulse()
                }
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                toggleAutoSwipe()
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                if (!isAnimating) {
                    quranViewModel.loadDailyAyah()
                    showToast(getString(R.string.daily_ayah_updated))
                    fastAnimateCardRefresh()
                    resetAutoSwipeTimer()
                }
            }
        })

        binding.dailyAyahCard.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)

            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                isUserInteracting = false
                if (!isAnimating && abs(binding.dailyAyahCard.translationX) < 120f) {
                    fastResetCardPosition()
                }
            }
            true
        }
    }

    // Super fast reset card position
    private fun fastResetCardPosition() {
        with(binding.dailyAyahCard) {
            animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(150) // Much faster
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
    }

    // Fast animate card pulse
    private fun fastAnimateCardPulse() {
        val scaleDown = ObjectAnimator.ofFloat(binding.dailyAyahCard, "scaleX", 1f, 0.98f).apply {
            duration = 60 // Super fast
        }
        val scaleDownY = ObjectAnimator.ofFloat(binding.dailyAyahCard, "scaleY", 1f, 0.98f).apply {
            duration = 60
        }
        val scaleUp = ObjectAnimator.ofFloat(binding.dailyAyahCard, "scaleX", 0.98f, 1f).apply {
            duration = 80
            startDelay = 60
        }
        val scaleUpY = ObjectAnimator.ofFloat(binding.dailyAyahCard, "scaleY", 0.98f, 1f).apply {
            duration = 80
            startDelay = 60
        }

        AnimatorSet().apply {
            playTogether(scaleDown, scaleDownY, scaleUp, scaleUpY)
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    // Fast animate card refresh
    private fun fastAnimateCardRefresh() {
        val rotate = ObjectAnimator.ofFloat(binding.dailyAyahCard, "rotation", 0f, 360f).apply {
            duration = 400 // Much faster
        }
        val scale = ObjectAnimator.ofFloat(binding.dailyAyahCard, "scaleX", 1f, 1.03f, 1f).apply {
            duration = 400
        }
        val scaleY = ObjectAnimator.ofFloat(binding.dailyAyahCard, "scaleY", 1f, 1.03f, 1f).apply {
            duration = 400
        }

        AnimatorSet().apply {
            playTogether(rotate, scale, scaleY)
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    // Fast manual swipe left with dot movement
    private fun onFastSwipeLeft() {
        if (isAnimating) return
        isAnimating = true

        fastSlideCardLeft(isAutomatic = false) {
            quranViewModel.loadNextAyah()
            moveToNextDot() // 🆕 Move to next dot

            fastSlideCardInFromRight(isAutomatic = false) {
                isAnimating = false
                isUserInteracting = false
            }
        }
    }

    // Fast manual swipe right with dot movement
    private fun onFastSwipeRight() {
        if (isAnimating) return
        isAnimating = true

        fastSlideCardRight(isAutomatic = false) {
            quranViewModel.loadPreviousAyah()
            moveToPreviousDot() // 🆕 Move to previous dot

            fastSlideCardInFromLeft(isAutomatic = false) {
                isAnimating = false
                isUserInteracting = false
            }
        }
    }

    // Fast slide card to the left
    private fun fastSlideCardLeft(isAutomatic: Boolean, onComplete: () -> Unit) {
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val targetX = -screenWidth

        // Super fast timing
        val duration = if (isAutomatic) 250L else 180L // Much faster
        val interpolator = if (isAutomatic) AccelerateInterpolator() else AccelerateInterpolator()

        val dailyAyahCard = _binding?.dailyAyahCard ?: return
        with(dailyAyahCard) {
            animate()
                .translationX(targetX)
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(interpolator)
                .withEndAction {
                    if (_binding != null && !isFinishing && !isDestroyed) {
                        onComplete()
                    }
                }
                .start()
        }
    }

    // 🚀 Fast slide card to the right
    private fun fastSlideCardRight(isAutomatic: Boolean, onComplete: () -> Unit) {
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val targetX = screenWidth

        val duration = if (isAutomatic) 250L else 180L
        val interpolator = if (isAutomatic) AccelerateInterpolator() else AccelerateInterpolator()

        val dailyAyahCard = _binding?.dailyAyahCard ?: return
        with(dailyAyahCard) {
            animate()
                .translationX(targetX)
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(interpolator)
                .withEndAction {
                    if (_binding != null && !isFinishing && !isDestroyed) {
                        onComplete()
                    }
                }
                .start()
        }
    }

    // 🚀 Fast slide card in from the right
    private fun fastSlideCardInFromRight(isAutomatic: Boolean, onComplete: () -> Unit) {
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val startX = screenWidth

        val duration = if (isAutomatic) 300L else 220L // Much faster
        val interpolator = DecelerateInterpolator()

        val dailyAyahCard = _binding?.dailyAyahCard ?: return
        with(dailyAyahCard) {
            translationX = startX
            alpha = 0f

            animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(interpolator)
                .withEndAction {
                    if (_binding != null && !isFinishing && !isDestroyed) {
                        onComplete()
                    }
                }
                .start()
        }
    }

    // 🚀 Fast slide card in from the left
    private fun fastSlideCardInFromLeft(isAutomatic: Boolean, onComplete: () -> Unit) {
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val startX = -screenWidth

        val duration = if (isAutomatic) 300L else 220L
        val interpolator = DecelerateInterpolator()

        val dailyAyahCard = _binding?.dailyAyahCard ?: return
        with(dailyAyahCard) {
            translationX = startX
            alpha = 0f

            animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(interpolator)
                .withEndAction {
                    if (_binding != null && !isFinishing && !isDestroyed) {
                        onComplete()
                    }
                }
                .start()
        }
    }

    // 🚀 Show interaction hint
    private fun showAyahInteractionHint() {
        val hint = if (isAutoSwipeEnabled) {
            getString(R.string.ayah_hint_auto_swipe_on)
        } else {
            getString(R.string.ayah_hint_auto_swipe_off)
        }
        showToast(hint)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        adManager.whenReady {
            adManager.loadInterstitialAd()
        }
        if (isAutoSwipeEnabled && !isUserInteracting) {
            startAutoSwipe()
        }
    }

    override fun onPause() {
        super.onPause()
        stopAutoSwipe()
    }

    private fun setupNetworkObserver() {
        lifecycleScope.launch {
            networkManager.isConnected.collect { isConnected ->
                binding.offlineBar.visibility = if (isConnected) View.GONE else View.VISIBLE
            }
        }
    }

    private fun checkLocationPermissions() {
        when {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                prayerTimeViewModel.checkLocationPermission()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                requestLocationPermissions()
            }

            else -> {
                requestLocationPermissions()
            }
        }
    }

    private fun requestLocationPermissions() {
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }
    
    /**
     * Check if full-screen intent permission is granted on Android 12+
     * This permission is required for the prayer alert screen to show up
     */
    private fun checkFullScreenIntentPermission() {
        // Only check if prayer notifications are enabled
        if (!ThemeManager.isPrayerNotificationsEnabled(this)) {
            return
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34+
            val notificationManager = getSystemService(NotificationManager::class.java)
            if (!notificationManager.canUseFullScreenIntent()) {
                // Check if we've already shown the dialog this session
                if (!hasShownFullScreenPermissionDialog) {
                    hasShownFullScreenPermissionDialog = true
                    showFullScreenIntentPermissionDialog()
                }
            }
        }
    }
    
    /**
     * Show dialog explaining the need for full-screen intent permission
     */
    private fun showFullScreenIntentPermissionDialog() {
        val dialog = DialogStyler.builder(this)
            .setTitle(R.string.fullscreen_permission_title)
            .setMessage(R.string.fullscreen_permission_message)
            .setIcon(R.drawable.ic_dua)
            .setPositiveButton(R.string.fullscreen_permission_open_settings) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                        data = Uri.parse("package:${packageName}")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    // Fallback to app settings if specific intent not supported
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${packageName}")
                    }
                    startActivity(intent)
                }
            }
            .setNegativeButton(R.string.fullscreen_permission_later, null)
            .create()

        showStyledDialog(dialog)
    }

    private fun showStyledDialog(dialog: AlertDialog) {
        dialog.show()
        DialogStyler.apply(dialog, this)
    }
    
    companion object {
        // Track if we've shown the full-screen permission dialog this session
        private var hasShownFullScreenPermissionDialog = false
    }

    private fun setupPrayerTimesObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    prayerTimeViewModel.prayerTimes.collect { prayerTimes ->
                        prayerTimes?.let { updatePrayerTimesUI(it) }
                    }
                }

                launch {
                    prayerTimeViewModel.currentPrayer.collect { currentPrayer ->
                        currentPrayer?.let { updateCurrentPrayerCard(it) }
                    }
                }

                launch {
                    prayerTimeViewModel.nextPrayer.collect { nextPrayer ->
                        nextPrayer?.let { updateNextPrayerInfo(it) }
                    }
                }

                launch {
                    prayerTimeViewModel.remainingTime.collect { remainingTime ->
                        updateCountdownTimer(remainingTime)
                    }
                }

                launch {
                    prayerTimeViewModel.error.collect { error ->
                        if (error != null) {
                            showError(error)
                        }
                    }
                }
            }
        }
    }

    private fun updateCurrentPrayerCard(currentPrayer: PrayerTimeUtils.PrayerInfo) {
        with(binding) {
            prayerName.text = if (LocaleHelper.isArabic(this@HomeActivity)) {
                currentPrayer.nameArabic
            } else {
                currentPrayer.name
            }
            prayerTime.text = currentPrayer.time
        }
    }

    private fun updateNextPrayerInfo(nextPrayer: PrayerTimeUtils.PrayerInfo) {
        with(binding) {
            val prayerDisplayName = if (LocaleHelper.isArabic(this@HomeActivity)) {
                nextPrayer.nameArabic
            } else {
                nextPrayer.name
            }
            nextPrayerLabel.text = getString(R.string.next_prayer_format, prayerDisplayName)
        }
    }

    private fun updateCountdownTimer(remainingTime: String) {
        binding.nextPrayerTime.text = remainingTime
    }

    private fun updatePrayerTimesUI(prayerTimes: PresentationPrayerTimesResponse) {
        // Update any additional UI elements with prayer times data
    }

    private fun showError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    private fun showLocationPermissionDenied() {
        Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
    }

    private fun setupSettingsButton() {
        binding.btnSettings.setOnClickListener {
            navController.navigate(R.id.settingsFragment, null, navOptions)
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        selectNavItem(R.id.nav_all_categories)

        navController.addOnDestinationChangedListener { _, destination, arguments ->
            if (fullScreenFragments.containsKey(destination.id)) {
                // Show interstitial ad every N navigations before opening screen
                navigationCount++
                if (navigationCount % INTERSTITIAL_EVERY_N_NAVIGATIONS == 0 && adManager.shouldShowAds()) {
                    adManager.showInterstitialAd(this) {
                        showFullScreenFragment(destination.id, arguments)
                    }
                } else {
                    showFullScreenFragment(destination.id, arguments)
                }
            } else {
                hideFullScreenFragment()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun selectNavItem(selectedId: Int) {
        binding.navPrayerTimes.background = null
        binding.navAllMedia.background = null
        binding.navAllCategories.background = null

        val unselectedColor = ContextCompat.getColor(this, android.R.color.white)
        val selectedColor = ContextCompat.getColor(this, R.color.selected_text_color)

        binding.navPrayerTimes.setTextColor(unselectedColor)
        binding.navAllMedia.setTextColor(unselectedColor)
        binding.navAllCategories.setTextColor(unselectedColor)

        val selectedBackground = ContextCompat.getDrawable(this, R.drawable.selected_nav_background)

        when (selectedId) {
            R.id.nav_prayer_times -> {
                binding.navPrayerTimes.background = selectedBackground
                binding.navPrayerTimes.setTextColor(selectedColor)
            }
            R.id.nav_all_media -> {
                binding.navAllMedia.background = selectedBackground
                binding.navAllMedia.setTextColor(selectedColor)
            }
            R.id.nav_all_categories -> {
                binding.navAllCategories.background = selectedBackground
                binding.navAllCategories.setTextColor(selectedColor)
            }
        }
    }

    private fun setupCustomBottomNavigation() {
        binding.navPrayerTimes.setOnClickListener {
            if (navController.currentDestination?.id != R.id.playerTimesFragment) {
                selectNavItem(R.id.nav_prayer_times)
                navController.navigate(R.id.playerTimesFragment, null, navOptions)
            }
        }
        binding.navAllMedia.setOnClickListener {
            if (navController.currentDestination?.id != R.id.allMediaFragment) {
                selectNavItem(R.id.nav_all_media)
                navController.navigate(R.id.allMediaFragment, null, navOptions)
            }
        }
        binding.navAllCategories.setOnClickListener {
            if (navController.currentDestination?.id != R.id.allCategoriesFragment) {
                selectNavItem(R.id.nav_all_categories)
                navController.navigate(R.id.allCategoriesFragment, null, navOptions)
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.playerTimesFragment -> selectNavItem(R.id.nav_prayer_times)
                R.id.allMediaFragment -> selectNavItem(R.id.nav_all_media)
                R.id.allCategoriesFragment -> selectNavItem(R.id.nav_all_categories)
            }
        }
    }

    private fun showFullScreenFragment(destinationId: Int, arguments: Bundle? = null) {
        stopAutoSwipe()

        android.util.Log.d("HomeActivity", "🖥️ showFullScreenFragment: destinationId=$destinationId, arguments=$arguments")
        arguments?.let { args ->
            android.util.Log.d("HomeActivity", "📦 Arguments keys: ${args.keySet().joinToString { "$it=${args.get(it)}" }}")
        }

        val safeBinding = _binding ?: return
        safeBinding.mainContentContainer.animate()
            .alpha(0f)
            .setDuration(100)
            .withEndAction {
                val currentBinding = _binding ?: return@withEndAction
                currentBinding.mainContentContainer.visibility = View.GONE

                currentBinding.fullscreenFragmentContainer.visibility = View.VISIBLE
                currentBinding.fullscreenFragmentContainer.alpha = 0f

                val fragmentFactory = fullScreenFragments[destinationId]
                if (fragmentFactory != null) {
                    if (isFinishing || isDestroyed || supportFragmentManager.isStateSaved) {
                        return@withEndAction
                    }
                    val fragment = fragmentFactory.invoke()
                    
                    // Pass arguments to the fragment
                    if (arguments != null) {
                        fragment.arguments = arguments
                        android.util.Log.d("HomeActivity", "✅ Arguments passed to fragment: ${arguments.keySet().joinToString { "$it=${arguments.get(it)}" }}")
                    }

                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.animation,
                            R.anim.animation2,
                            R.anim.animation3,
                            R.anim.animation4
                        )
                        .replace(R.id.fullscreen_fragment_container, fragment)
                        .commit()
                    currentBinding.fullscreenFragmentContainer.animate()
                        .alpha(1f)
                        .setDuration(200)
                        .start()
                }
            }
            .start()
    }

    private fun hideFullScreenFragment() {
        if (isAutoSwipeEnabled) {
            startAutoSwipe()
        }

        val safeBinding = _binding ?: return
        if (safeBinding.fullscreenFragmentContainer.visibility == View.VISIBLE) {
            safeBinding.fullscreenFragmentContainer.animate()
                .alpha(0f)
                .setDuration(100) // Faster
                .withEndAction {
                    val currentBinding = _binding ?: return@withEndAction
                    currentBinding.fullscreenFragmentContainer.visibility = View.GONE

                    supportFragmentManager.findFragmentById(R.id.fullscreen_fragment_container)?.let { fragment ->
                        if (!isFinishing && !isDestroyed && !supportFragmentManager.isStateSaved) {
                            supportFragmentManager.beginTransaction()
                                .setCustomAnimations(
                                    R.anim.animation,
                                    R.anim.animation2,
                                    R.anim.animation3,
                                    R.anim.animation4
                                )
                                .remove(fragment)
                                .commit()
                        }
                    }

                    currentBinding.mainContentContainer.visibility = View.VISIBLE
                    currentBinding.mainContentContainer.alpha = 0f
                    currentBinding.mainContentContainer.animate()
                        .alpha(1f)
                        .setDuration(200)
                        .start()
                }
                .start()
        }
    }

    override fun onDestroy() {
        _binding?.let { b ->
            // Cancel pending view animations to avoid late callbacks after Activity recreation.
            b.dailyAyahCard.animate().setListener(null).cancel()
            b.dotsContainer.animate().setListener(null).cancel()
            b.mainContentContainer.animate().setListener(null).cancel()
            b.fullscreenFragmentContainer.animate().setListener(null).cancel()
        }
        super.onDestroy()
        stopAutoSwipe()
        _binding = null
    }
}
