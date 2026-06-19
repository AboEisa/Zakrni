package com.zakrni.app.clean.ads

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.zakrni.app.R

/**
 * Displays a single Native Ad and retries loading when inventory is unavailable.
 */
class NativeAdContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "NativeAdContainer"
        private const val BASE_RETRY_DELAY_MS = 8_000L
        private const val MAX_RETRY_DELAY_MS = 60_000L
    }

    private var currentAdManager: AdManager? = null
    private var nativeAd: NativeAd? = null
    private var isLoading = false
    private var retryStep = 0

    private val handler = Handler(Looper.getMainLooper())
    private val retryRunnable = Runnable {
        currentAdManager?.let { loadNativeAdInternal(it) }
    }

    fun loadAd(adManager: AdManager) {
        currentAdManager = adManager
        retryStep = 0
        isLoading = false
        handler.removeCallbacks(retryRunnable)

        if (!adManager.shouldShowAds()) {
            visibility = View.GONE
            return
        }

        adManager.whenReady {
            loadNativeAdInternal(adManager)
        }
    }

    private fun loadNativeAdInternal(adManager: AdManager) {
        if (!adManager.shouldShowAds() || isLoading) {
            visibility = View.GONE
            return
        }

        isLoading = true

        val adLoader = AdLoader.Builder(context, AdManager.NATIVE_AD_UNIT_ID)
            .forNativeAd { loadedAd ->
                if (!adManager.shouldShowAds()) {
                    loadedAd.destroy()
                    isLoading = false
                    visibility = View.GONE
                    return@forNativeAd
                }

                nativeAd?.destroy()
                nativeAd = loadedAd

                val adView = LayoutInflater.from(context)
                    .inflate(R.layout.view_native_ad, this, false) as NativeAdView
                populateNativeAdView(loadedAd, adView)

                removeAllViews()
                addView(adView)

                handler.removeCallbacks(retryRunnable)
                retryStep = 0
                isLoading = false
                visibility = View.VISIBLE
                Log.d(TAG, "Native ad loaded")
            }
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                    visibility = View.GONE

                    retryStep = (retryStep + 1).coerceAtMost(10)
                    val delay = minOf(BASE_RETRY_DELAY_MS * retryStep, MAX_RETRY_DELAY_MS)
                    Log.w(TAG, "Native failed: code=${error.code}, message=${error.message}. Retry in ${delay / 1000}s")

                    handler.removeCallbacks(retryRunnable)
                    handler.postDelayed(retryRunnable, delay)
                }
            })
            .build()

        adLoader.loadAd(adManager.buildAdRequest())
    }

    private fun populateNativeAdView(ad: NativeAd, adView: NativeAdView) {
        val mediaView = adView.findViewById<MediaView>(R.id.native_ad_media)
        val headlineView = adView.findViewById<TextView>(R.id.native_ad_headline)
        val bodyView = adView.findViewById<TextView>(R.id.native_ad_body)
        val ctaView = adView.findViewById<Button>(R.id.native_ad_cta)

        adView.mediaView = mediaView
        adView.headlineView = headlineView
        adView.bodyView = bodyView
        adView.callToActionView = ctaView

        mediaView.mediaContent = ad.mediaContent
        headlineView.text = ad.headline

        if (ad.body.isNullOrBlank()) {
            bodyView.visibility = View.GONE
        } else {
            bodyView.text = ad.body
            bodyView.visibility = View.VISIBLE
        }

        ctaView.text = ad.callToAction ?: context.getString(R.string.native_ad_cta_fallback)
        ctaView.visibility = if (ad.callToAction.isNullOrBlank()) View.GONE else View.VISIBLE

        adView.setNativeAd(ad)
    }

    fun destroyAd() {
        handler.removeCallbacks(retryRunnable)
        isLoading = false
        nativeAd?.destroy()
        nativeAd = null
        removeAllViews()
    }
}

