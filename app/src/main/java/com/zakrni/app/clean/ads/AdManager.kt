package com.zakrni.app.clean.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.WebView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
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

        // Real AdMob ad unit IDs.
        private const val REAL_BANNER_ID = "ca-app-pub-6596512756174890/8174396964"
        private const val REAL_INTERSTITIAL_ID = "ca-app-pub-6596512756174890/7651652788"
        private const val REAL_REWARDED_ID = "ca-app-pub-6596512756174890/1734716356"
        private const val REAL_REWARDED_INTERSTITIAL_ID = "ca-app-pub-6596512756174890/2544410730"
        private const val REAL_APP_OPEN_ID = "ca-app-pub-6596512756174890/3869827043"

        // Native is created in AdMob but not rendered yet (needs dedicated NativeAdView layout).
        private const val REAL_NATIVE_ID = "ca-app-pub-6596512756174890/6495990386"

        val BANNER_AD_UNIT_ID = REAL_BANNER_ID
        val INTERSTITIAL_AD_UNIT_ID = REAL_INTERSTITIAL_ID
        val REWARDED_AD_UNIT_ID = REAL_REWARDED_ID
        val REWARDED_INTERSTITIAL_AD_UNIT_ID = REAL_REWARDED_INTERSTITIAL_ID
        val APP_OPEN_AD_UNIT_ID = REAL_APP_OPEN_ID
        val NATIVE_AD_UNIT_ID = REAL_NATIVE_ID

        private const val MAX_RETRY_DELAY_MS = 60_000L
        private const val APP_OPEN_MIN_INTERVAL_MS = 120_000L
        private const val APP_OPEN_SUPPRESS_AFTER_FULLSCREEN_MS = 15_000L
    }

    @Volatile private var isInitialized = false
    @Volatile private var isInitializing = false

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var appOpenAd: AppOpenAd? = null

    private var isInterstitialLoading = false
    private var isRewardedLoading = false
    private var isRewardedInterstitialLoading = false
    private var isAppOpenLoading = false

    private var interstitialRetryCount = 0
    private var rewardedRetryCount = 0
    private var rewardedInterstitialRetryCount = 0
    private var appOpenRetryCount = 0

    private var isShowingAppOpenAd = false
    private var isShowingFullScreenAd = false
    private var lastAppOpenShownAt = 0L
    private var lastFullScreenDismissedAt = 0L

    private val mainHandler = Handler(Looper.getMainLooper())
    private val retryHandler = mainHandler

    private val onInitializedCallbacks = mutableListOf<() -> Unit>()

    fun initialize() {
        if (!isMainThread()) {
            runOnMain { initialize() }
            return
        }

        if (isInitialized || isInitializing) return
        isInitializing = true

        val requestConfig = RequestConfiguration.Builder()
            .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED)
            .setTagForUnderAgeOfConsent(RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED)
            .build()

        MobileAds.setRequestConfiguration(requestConfig)

        MobileAds.initialize(context) { initStatus ->
            Log.d(TAG, "AdMob initialized: $initStatus")
            isInitialized = true
            isInitializing = false

            if (shouldShowAds()) {
                preloadAllAdTypes()
            }

            onInitializedCallbacks.forEach { callback -> callback() }
            onInitializedCallbacks.clear()
        }
    }

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

    fun buildAdRequest(): AdRequest = AdRequest.Builder().build()

    // Ads are fully disabled for everyone (kill switch). Every ad load/show path checks this.
    fun shouldShowAds(): Boolean = false

    fun preloadAllAdTypes() {
        loadInterstitialAd()
        loadRewardedAd()
        loadRewardedInterstitialAd()
        loadAppOpenAd()
    }

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
        Log.d(TAG, "Loading interstitial ad with unitId=$INTERSTITIAL_AD_UNIT_ID")

        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            buildAdRequest(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    interstitialRetryCount = 0
                    Log.d(TAG, "Interstitial ad loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    interstitialRetryCount++
                    maybeRecoverJavascriptEngine(error)
                    val delay = minOf(5000L * interstitialRetryCount, MAX_RETRY_DELAY_MS)
                    Log.w(TAG, "Interstitial load failed: ${error.message} (code=${error.code}) — retry in ${delay / 1000}s")
                    retryHandler.postDelayed({ loadInterstitialAd() }, delay)
                }
            }
        )
    }

    fun loadRewardedAd() {
        if (!isMainThread()) {
            runOnMain { loadRewardedAd() }
            return
        }

        if (!isInitialized) {
            whenReady { loadRewardedAd() }
            return
        }

        if (!shouldShowAds() || isRewardedLoading || rewardedAd != null) return

        isRewardedLoading = true
        Log.d(TAG, "Loading rewarded ad with unitId=$REWARDED_AD_UNIT_ID")

        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            buildAdRequest(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    rewardedRetryCount = 0
                    Log.d(TAG, "Rewarded ad loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    rewardedRetryCount++
                    maybeRecoverJavascriptEngine(error)
                    val delay = minOf(5000L * rewardedRetryCount, MAX_RETRY_DELAY_MS)
                    Log.w(TAG, "Rewarded load failed: ${error.message} (code=${error.code}) — retry in ${delay / 1000}s")
                    retryHandler.postDelayed({ loadRewardedAd() }, delay)
                }
            }
        )
    }

    fun loadRewardedInterstitialAd() {
        if (!isMainThread()) {
            runOnMain { loadRewardedInterstitialAd() }
            return
        }

        if (!isInitialized) {
            whenReady { loadRewardedInterstitialAd() }
            return
        }

        if (!shouldShowAds() || isRewardedInterstitialLoading || rewardedInterstitialAd != null) return

        isRewardedInterstitialLoading = true
        Log.d(TAG, "Loading rewarded interstitial ad with unitId=$REWARDED_INTERSTITIAL_AD_UNIT_ID")

        RewardedInterstitialAd.load(
            context,
            REWARDED_INTERSTITIAL_AD_UNIT_ID,
            buildAdRequest(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitialAd = ad
                    isRewardedInterstitialLoading = false
                    rewardedInterstitialRetryCount = 0
                    Log.d(TAG, "Rewarded interstitial ad loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedInterstitialAd = null
                    isRewardedInterstitialLoading = false
                    rewardedInterstitialRetryCount++
                    maybeRecoverJavascriptEngine(error)
                    val delay = minOf(5000L * rewardedInterstitialRetryCount, MAX_RETRY_DELAY_MS)
                    Log.w(TAG, "Rewarded interstitial load failed: ${error.message} (code=${error.code}) — retry in ${delay / 1000}s")
                    retryHandler.postDelayed({ loadRewardedInterstitialAd() }, delay)
                }
            }
        )
    }

    fun loadAppOpenAd() {
        if (!isMainThread()) {
            runOnMain { loadAppOpenAd() }
            return
        }

        if (!isInitialized) {
            whenReady { loadAppOpenAd() }
            return
        }

        if (!shouldShowAds() || isAppOpenLoading || appOpenAd != null) return

        isAppOpenLoading = true
        Log.d(TAG, "Loading app open ad with unitId=$APP_OPEN_AD_UNIT_ID")

        AppOpenAd.load(
            context,
            APP_OPEN_AD_UNIT_ID,
            buildAdRequest(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isAppOpenLoading = false
                    appOpenRetryCount = 0
                    Log.d(TAG, "App open ad loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd = null
                    isAppOpenLoading = false
                    appOpenRetryCount++
                    maybeRecoverJavascriptEngine(error)
                    val delay = minOf(5000L * appOpenRetryCount, MAX_RETRY_DELAY_MS)
                    Log.w(TAG, "App open load failed: ${error.message} (code=${error.code}) — retry in ${delay / 1000}s")
                    retryHandler.postDelayed({ loadAppOpenAd() }, delay)
                }
            }
        )
    }

    private fun maybeRecoverJavascriptEngine(error: LoadAdError) {
        val message = error.message ?: return
        if (!message.contains("JavascriptEngine", ignoreCase = true)) return

        Log.w(TAG, "Detected JavascriptEngine load issue; warming up WebView before retry")
        runCatching {
            WebView(context).apply {
                settings.javaScriptEnabled = true
                destroy()
            }
        }.onFailure { failure ->
            Log.w(TAG, "WebView warm-up failed during ad recovery", failure)
        }
    }

    fun showAppOpenIfAvailable(activity: Activity) {
        if (!isMainThread()) {
            runOnMain { showAppOpenIfAvailable(activity) }
            return
        }

        if (!isInitialized || !shouldShowAds()) return
        if (isShowingAppOpenAd || isShowingFullScreenAd || activity.isFinishing || activity.isDestroyed) return

        val now = System.currentTimeMillis()
        if (now - lastAppOpenShownAt < APP_OPEN_MIN_INTERVAL_MS) return
        if (now - lastFullScreenDismissedAt < APP_OPEN_SUPPRESS_AFTER_FULLSCREEN_MS) return

        val ad = appOpenAd ?: run {
            loadAppOpenAd()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                isShowingAppOpenAd = true
                isShowingFullScreenAd = true
                Log.d(TAG, "App open ad shown")
            }

            override fun onAdDismissedFullScreenContent() {
                isShowingAppOpenAd = false
                isShowingFullScreenAd = false
                appOpenAd = null
                lastAppOpenShownAt = System.currentTimeMillis()
                lastFullScreenDismissedAt = System.currentTimeMillis()
                loadAppOpenAd()
                Log.d(TAG, "App open ad dismissed")
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                isShowingAppOpenAd = false
                isShowingFullScreenAd = false
                appOpenAd = null
                lastFullScreenDismissedAt = System.currentTimeMillis()
                loadAppOpenAd()
                Log.w(TAG, "App open failed to show: ${error.message}")
            }
        }

        ad.show(activity)
    }

    fun showNavigationAd(activity: Activity, onDismissed: () -> Unit = {}) {
        if (!isMainThread()) {
            activity.runOnUiThread { showNavigationAd(activity, onDismissed) }
            return
        }

        if (!isInitialized) {
            whenReady { showNavigationAd(activity, onDismissed) }
            return
        }

        if (!shouldShowAds()) {
            onDismissed()
            return
        }

        val rewardedInterstitial = rewardedInterstitialAd
        if (rewardedInterstitial != null) {
            rewardedInterstitial.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    isShowingFullScreenAd = true
                    Log.d(TAG, "Rewarded interstitial shown")
                }

                override fun onAdDismissedFullScreenContent() {
                    isShowingFullScreenAd = false
                    lastFullScreenDismissedAt = System.currentTimeMillis()
                    rewardedInterstitialAd = null
                    loadRewardedInterstitialAd()
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.w(TAG, "Rewarded interstitial failed to show: ${error.message}")
                    isShowingFullScreenAd = false
                    lastFullScreenDismissedAt = System.currentTimeMillis()
                    rewardedInterstitialAd = null
                    loadRewardedInterstitialAd()
                    showInterstitialAd(activity, onDismissed)
                }
            }

            rewardedInterstitial.show(activity) { rewardItem ->
                Log.d(TAG, "Rewarded interstitial reward: ${rewardItem.amount} ${rewardItem.type}")
            }
            return
        }

        showInterstitialAd(activity, onDismissed)
    }

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

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    isShowingFullScreenAd = false
                    lastFullScreenDismissedAt = System.currentTimeMillis()
                    interstitialAd = null
                    loadInterstitialAd()
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.w(TAG, "Interstitial failed to show: code=${error.code}, message=${error.message}")
                    isShowingFullScreenAd = false
                    lastFullScreenDismissedAt = System.currentTimeMillis()
                    interstitialAd = null
                    loadInterstitialAd()
                    onDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    isShowingFullScreenAd = true
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

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onDismissed: () -> Unit = {}
    ) {
        if (!isMainThread()) {
            activity.runOnUiThread { showRewardedAd(activity, onRewardEarned, onDismissed) }
            return
        }

        if (!isInitialized) {
            whenReady { showRewardedAd(activity, onRewardEarned, onDismissed) }
            return
        }

        if (!shouldShowAds()) {
            onDismissed()
            return
        }

        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    isShowingFullScreenAd = true
                    Log.d(TAG, "Rewarded ad shown")
                }

                override fun onAdDismissedFullScreenContent() {
                    isShowingFullScreenAd = false
                    lastFullScreenDismissedAt = System.currentTimeMillis()
                    rewardedAd = null
                    loadRewardedAd()
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.w(TAG, "Rewarded failed to show: ${error.message}")
                    isShowingFullScreenAd = false
                    lastFullScreenDismissedAt = System.currentTimeMillis()
                    rewardedAd = null
                    loadRewardedAd()
                    onDismissed()
                }
            }

            ad.show(activity) { rewardItem ->
                Log.d(TAG, "Reward earned: ${rewardItem.amount} ${rewardItem.type}")
                onRewardEarned()
            }
        } else {
            loadRewardedAd()
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
