package com.zakrni.app.clean.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val subscriptionManager: SubscriptionManager
) {
    companion object {
        private const val TAG = "AdManager"

        // Real Ad IDs from AdMob (used in release builds)
        private const val REAL_BANNER_ID = "ca-app-pub-6596512756174890/8174396964"
        private const val REAL_INTERSTITIAL_ID = "ca-app-pub-6596512756174890/7651652788"

        // Google's official test Ad IDs (used in debug builds — always show test ads)
        private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
        private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"

        // Automatically use test IDs in debug, real IDs in release
        val BANNER_AD_UNIT_ID = if (com.zakrni.app.BuildConfig.DEBUG) TEST_BANNER_ID else REAL_BANNER_ID
        val INTERSTITIAL_AD_UNIT_ID = if (com.zakrni.app.BuildConfig.DEBUG) TEST_INTERSTITIAL_ID else REAL_INTERSTITIAL_ID

        // Minimum interval between interstitial ads (1.5 minutes)
        private const val INTERSTITIAL_INTERVAL_MS = 90 * 1000L
    }

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var lastInterstitialShowTime = 0L
    @Volatile private var isInitialized = false
    @Volatile private var isInitializing = false
    private val mainHandler = Handler(Looper.getMainLooper())

    // Callbacks waiting for initialization to complete
    private val onInitializedCallbacks = mutableListOf<() -> Unit>()

    /**
     * Initialize AdMob SDK with STRICT content filtering.
     * max_ad_content_rating = "G" → General audiences only (no adult, gambling, etc.)
     */
    fun initialize() {
        if (!isMainThread()) {
            runOnMain { initialize() }
            return
        }

        if (isInitialized || isInitializing) return
        isInitializing = true

        val requestConfig = RequestConfiguration.Builder()
            // "G" = General audiences — strictest filter, blocks ALL adult/mature content
            .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
            // Keep audience tags unspecified unless app is explicitly child-directed.
            .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED)
            .setTagForUnderAgeOfConsent(RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED)
            .apply {
                // Only register test devices in debug builds
                if (com.zakrni.app.BuildConfig.DEBUG) {
                    setTestDeviceIds(listOf("D91559BB0E33D3488B1A08B753EDA313"))
                }
            }
            .build()

        MobileAds.setRequestConfiguration(requestConfig)

        MobileAds.initialize(context) { initStatus ->
            Log.d(TAG, "AdMob initialized: $initStatus")
            isInitialized = true
            isInitializing = false
            // Pre-load first interstitial
            if (shouldShowAds()) {
                loadInterstitialAd()
            }
            // Run any waiting callbacks
            onInitializedCallbacks.forEach { callback -> callback() }
            onInitializedCallbacks.clear()
        }
    }

    /**
     * Execute a callback when AdMob is initialized.
     * If already initialized, runs immediately.
     */
    fun whenReady(callback: () -> Unit) {
        if (!isMainThread()) {
            runOnMain { whenReady(callback) }
            return
        }

        if (isInitialized) {
            callback()
        } else {
            onInitializedCallbacks.add(callback)
            if (!isInitializing) {
                initialize()
            }
        }
    }

    /**
     * Build an AdRequest with additional content filtering
     */
    fun buildAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    /**
     * Should we show ads? Returns false if user is subscribed (premium)
     */
    fun shouldShowAds(): Boolean {
        return !subscriptionManager.isSubscribed()
    }

    // ======================== INTERSTITIAL ADS ========================

    private var interstitialRetryCount = 0
    private val maxInterstitialRetries = 3
    private val retryHandler = mainHandler

    /**
     * Pre-load an interstitial ad with retry on failure
     */
    fun loadInterstitialAd() {
        if (!isMainThread()) {
            runOnMain { loadInterstitialAd() }
            return
        }

        if (!isInitialized) {
            whenReady { loadInterstitialAd() }
            return
        }

        if (!shouldShowAds() || isInterstitialLoading || interstitialAd != null) return

        isInterstitialLoading = true
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            buildAdRequest(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded")
                    interstitialAd = ad
                    isInterstitialLoading = false
                    interstitialRetryCount = 0
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(
                        TAG,
                        "Interstitial load failed (attempt ${interstitialRetryCount + 1}): " +
                            "code=${error.code}, domain=${error.domain}, message=${error.message}"
                    )
                    interstitialAd = null
                    isInterstitialLoading = false
                    // Retry with exponential backoff
                    if (interstitialRetryCount < maxInterstitialRetries) {
                        interstitialRetryCount++
                        val delay = 5000L * interstitialRetryCount
                        Log.d(TAG, "Retrying interstitial in ${delay / 1000}s")
                        retryHandler.postDelayed({ loadInterstitialAd() }, delay)
                    }
                }
            }
        )
    }

    /**
     * Show interstitial ad if conditions are met:
     * - User is NOT subscribed
     * - An ad is loaded
     * - Enough time passed since last interstitial (prevents spam)
     */
    fun showInterstitialAd(activity: Activity, onDismissed: () -> Unit = {}) {
        if (!isMainThread()) {
            activity.runOnUiThread { showInterstitialAd(activity, onDismissed) }
            return
        }

        if (!isInitialized) {
            whenReady { showInterstitialAd(activity, onDismissed) }
            return
        }

        if (!shouldShowAds()) {
            onDismissed()
            return
        }

        val now = System.currentTimeMillis()
        if (now - lastInterstitialShowTime < INTERSTITIAL_INTERVAL_MS) {
            Log.d(TAG, "Too soon to show another interstitial")
            onDismissed()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial dismissed")
                    interstitialAd = null
                    lastInterstitialShowTime = System.currentTimeMillis()
                    loadInterstitialAd() // Pre-load next one
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.w(
                        TAG,
                        "Interstitial failed to show: code=${error.code}, domain=${error.domain}, message=${error.message}"
                    )
                    interstitialAd = null
                    loadInterstitialAd()
                    onDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial shown")
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "No interstitial ready")
            loadInterstitialAd()
            onDismissed()
        }
    }

    private fun isMainThread(): Boolean = Looper.myLooper() == Looper.getMainLooper()

    private fun runOnMain(action: () -> Unit) {
        if (isMainThread()) {
            action()
        } else {
            mainHandler.post(action)
        }
    }
}
