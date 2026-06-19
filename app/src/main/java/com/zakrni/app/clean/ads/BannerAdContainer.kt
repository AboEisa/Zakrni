package com.zakrni.app.clean.ads

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * A wrapper that shows a banner ad or hides itself if user is subscribed.
 * Usage: Add <com.zakrni.app.clean.ads.BannerAdContainer> in XML layout.
 */
class BannerAdContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "BannerAdContainer"
        private const val BASE_RETRY_DELAY_MS = 5_000L
        private const val MAX_RETRY_DELAY_MS = 60_000L
        private const val NO_FILL_CODE = 3
    }

    private var adView: AdView? = null
    private var retryStep = 0
    private var isLoading = false
    private var currentAdManager: AdManager? = null
    private val handler = Handler(Looper.getMainLooper())
    private val retryRunnable = Runnable {
        currentAdManager?.let { loadAdInternal(it) }
    }

    /**
     * Load and show a banner ad. Call this from Activity/Fragment.
     * If user is subscribed, the view hides itself.
     * Waits for AdMob initialization before loading.
     * Keeps retrying with capped backoff when inventory is temporarily unavailable.
     */
    fun loadAd(adManager: AdManager) {
        currentAdManager = adManager
        retryStep = 0
        isLoading = false
        handler.removeCallbacks(retryRunnable)

        if (!adManager.shouldShowAds()) {
            visibility = View.GONE
            return
        }

        // Wait for AdMob to be fully initialized before loading
        adManager.whenReady {
            loadAdInternal(adManager)
        }
    }

    private fun loadAdInternal(adManager: AdManager) {
        if (!adManager.shouldShowAds() || isLoading) {
            visibility = View.GONE
            return
        }

        // Clean up old ad view if any
        adView?.destroy()
        removeAllViews()

        isLoading = true
        visibility = View.GONE

        val adWidthPixels = if (width > 0) width else resources.displayMetrics.widthPixels
        val adWidthDp = (adWidthPixels / resources.displayMetrics.density).toInt().coerceAtLeast(1)
        val adaptiveSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp)

        adView = AdView(context).apply {
            setAdSize(adaptiveSize)
            adUnitId = AdManager.BANNER_AD_UNIT_ID

            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.d(TAG, "✅ Banner ad loaded")
                    this@BannerAdContainer.isLoading = false
                    retryStep = 0
                    this@BannerAdContainer.visibility = View.VISIBLE
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    this@BannerAdContainer.isLoading = false
                    this@BannerAdContainer.visibility = View.GONE

                    val isNoFill = error.code == NO_FILL_CODE
                    retryStep = (retryStep + 1).coerceAtMost(12)
                    val delay = minOf(BASE_RETRY_DELAY_MS * retryStep, MAX_RETRY_DELAY_MS)

                    Log.w(
                        TAG,
                        "❌ Banner failed (code=${error.code}, message=${error.message}). Retrying in ${delay / 1000}s"
                    )

                    handler.removeCallbacks(retryRunnable)
                    handler.postDelayed(retryRunnable, delay)

                    if (!isNoFill) {
                        Log.d(TAG, "Banner failure is not no-fill; keeping retry strategy active.")
                    }
                }
            }

            loadAd(adManager.buildAdRequest())
        }

        addView(adView)
    }

    /**
     * Hide the ad (e.g., when user subscribes)
     */
    fun hideAd() {
        handler.removeCallbacks(retryRunnable)
        isLoading = false
        visibility = View.GONE
        adView?.destroy()
        adView = null
        removeAllViews()
    }

    fun resumeAd() { adView?.resume() }
    fun pauseAd() { adView?.pause() }
    fun destroyAd() {
        handler.removeCallbacks(retryRunnable)
        isLoading = false
        adView?.destroy()
        adView = null
    }
}
