package us.goldprice.hargaemas.ads

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import us.goldprice.hargaemas.R
import us.goldprice.hargaemas.data.AdConfig

object AdManager {
    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var clickCount = 0

    // For testing if adConfig doesn't have real IDs or hasn't loaded
    private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"

    fun loadInterstitial(context: Context, config: AdConfig?) {
        if (interstitialAd != null || isInterstitialLoading) return
        val adUnitId = config?.interstitial_id ?: TEST_INTERSTITIAL_ID

        isInterstitialLoading = true
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                isInterstitialLoading = false
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                interstitialAd = null
                isInterstitialLoading = false
            }
        })
    }

    fun showInterstitialIfReady(activity: Activity, config: AdConfig?) {
        val interval = config?.interstitial_click_interval ?: 3
        clickCount++
        if (clickCount >= interval) {
            interstitialAd?.let {
                it.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        interstitialAd = null
                        clickCount = 0
                        loadInterstitial(activity, config)
                    }

                    override fun onAdFailedToShowFullScreenContent(error: AdError) {
                        interstitialAd = null
                    }
                }
                it.show(activity)
            } ?: run {
                // If it wasn't ready, try loading it
                loadInterstitial(activity, config)
            }
        }
    }
    
    fun getNativeAdUnitId(config: AdConfig?): String {
        return config?.native_id ?: TEST_NATIVE_ID
    }
}

@Composable
fun NativeAdViewComposable(context: Context, config: AdConfig?) {
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    if (nativeAd == null) {
        val adUnitId = AdManager.getNativeAdUnitId(config)
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad ->
                nativeAd = ad
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    nativeAd = null
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    nativeAd?.let { ad ->
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                val adView = LayoutInflater.from(ctx).inflate(R.layout.ad_native_template, null) as NativeAdView
                populateNativeAdView(ad, adView)
                adView
            }
        )
    }
}

private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
    adView.headlineView = adView.findViewById(R.id.ad_headline)
    adView.bodyView = adView.findViewById(R.id.ad_body)
    adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
    adView.iconView = adView.findViewById(R.id.ad_app_icon)
    adView.starRatingView = adView.findViewById(R.id.ad_stars)
    adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
    adView.mediaView = adView.findViewById(R.id.ad_media)

    (adView.headlineView as TextView).text = nativeAd.headline
    nativeAd.mediaContent?.let { adView.mediaView?.mediaContent = it }

    if (nativeAd.body == null) {
        adView.bodyView?.visibility = android.view.View.INVISIBLE
    } else {
        adView.bodyView?.visibility = android.view.View.VISIBLE
        (adView.bodyView as TextView).text = nativeAd.body
    }

    if (nativeAd.callToAction == null) {
        adView.callToActionView?.visibility = android.view.View.INVISIBLE
    } else {
        adView.callToActionView?.visibility = android.view.View.VISIBLE
        (adView.callToActionView as Button).text = nativeAd.callToAction
    }

    if (nativeAd.icon == null) {
        adView.iconView?.visibility = android.view.View.GONE
    } else {
        (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
        adView.iconView?.visibility = android.view.View.VISIBLE
    }

    if (nativeAd.starRating == null) {
        adView.starRatingView?.visibility = android.view.View.INVISIBLE
    } else {
        (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
        adView.starRatingView?.visibility = android.view.View.VISIBLE
    }

    if (nativeAd.advertiser == null) {
        adView.advertiserView?.visibility = android.view.View.INVISIBLE
    } else {
        (adView.advertiserView as TextView).text = nativeAd.advertiser
        adView.advertiserView?.visibility = android.view.View.VISIBLE
    }

    adView.setNativeAd(nativeAd)
}
