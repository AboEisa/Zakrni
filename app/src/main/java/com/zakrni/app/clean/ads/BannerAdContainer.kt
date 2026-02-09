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
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 5000L
    }

    private var adView: AdView? = null
    private var retryCount = 0
    private var currentAdManager: AdManager? = null
    private val handler = Handler(Looper.getMainLooper())

    /**
     * Load and show a banner ad. Call this from Activity/Fragment.
     * If user is subscribed, the view hides itself.
     * Waits for AdMob initialization before loading.
     * Retries up to 3 times on failure with 5-second delay.
     */
    fun loadAd(adManager: AdManager) {
        currentAdManager = adManager
        retryCount = 0

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
        if (!adManager.shouldShowAds()) {
            visibility = View.GONE
            return
        }

        visibility = View.VISIBLE

        // Clean up old ad view if any
        adView?.destroy()
        removeAllViews()

        adView = AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = AdManager.BANNER_AD_UNIT_ID

            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.d(TAG, "✅ Banner ad loaded")
                    retryCount = 0
                    this@BannerAdContainer.visibility = View.VISIBLE
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "❌ Banner failed (attempt ${retryCount + 1}/$MAX_RETRIES): ${error.message}")
                    if (retryCount < MAX_RETRIES) {
                        retryCount++
                        handler.postDelayed({
                            Log.d(TAG, "🔄 Retrying banner ad (attempt $retryCount)...")
                            loadAdInternal(adManager)
                        }, RETRY_DELAY_MS * retryCount)
                    } else {
                        this@BannerAdContainer.visibility = View.GONE
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
        visibility = View.GONE
        adView?.destroy()
        adView = null
        removeAllViews()
    }

    fun resumeAd() { adView?.resume() }
    fun pauseAd() { adView?.pause() }
    fun destroyAd() {
        adView?.destroy()
        adView = null
    }
}
