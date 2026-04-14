package com.hadify.NumberMerge2048

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

private const val ADS_TAG = "AdMob"

object AdMobAdUnits {
    // Real ad units (release)
    private const val BANNER_HOME_RELEASE = "ca-app-pub-9055648436770206/3891431556"
    private const val BANNER_SECONDARY_RELEASE = "ca-app-pub-9055648436770206/4876338029"
    private const val REWARDED_RELEASE = "ca-app-pub-9055648436770206/3371684662"

    // Google test ad units (debug)
    private const val BANNER_TEST = "ca-app-pub-3940256099942544/9214589741"
    private const val REWARDED_TEST = "ca-app-pub-3940256099942544/5224354917"

    private fun useTestAds(context: Context): Boolean = context.packageName.endsWith(".dev")

    fun homeBanner(context: Context): String = if (useTestAds(context)) BANNER_TEST else BANNER_HOME_RELEASE

    fun secondaryBanner(context: Context): String = if (useTestAds(context)) BANNER_TEST else BANNER_SECONDARY_RELEASE

    fun rewarded(context: Context): String = if (useTestAds(context)) REWARDED_TEST else REWARDED_RELEASE
}

fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) {
            return current
        }
        current = current.baseContext
    }
    return null
}

@Composable
fun AdMobBanner(
    adUnitId: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val config = LocalConfiguration.current
    val adWidthDp = config.screenWidthDp.coerceAtLeast(320)
    val adSize = remember(context, adWidthDp) {
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp)
    }

    val adView = remember(adUnitId, adSize) {
        AdView(context).apply {
            setAdSize(adSize)
            this.adUnitId = adUnitId
            loadAd(AdRequest.Builder().build())
        }
    }

    DisposableEffect(adView) {
        onDispose {
            adView.destroy()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { adView },
    )
}

class RewardedAdController(
    private val context: Context,
    private val adUnitId: String,
) {
    private var ad: RewardedAd? = null
    private var loading = false

    var isReady by mutableStateOf(false)
        private set

    fun preload() {
        if (ad != null || loading) {
            return
        }

        loading = true
        RewardedAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    loading = false
                    ad = rewardedAd
                    isReady = true
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    loading = false
                    ad = null
                    isReady = false
                    Log.d(ADS_TAG, "Rewarded load failed: ${loadAdError.message}")
                }
            },
        )
    }

    fun show(
        activity: Activity?,
        onRewardEarned: (RewardItem) -> Unit,
        onUnavailable: () -> Unit,
    ) {
        val rewarded = ad
        if (activity == null || rewarded == null) {
            preload()
            onUnavailable()
            return
        }

        ad = null
        isReady = false

        rewarded.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preload()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d(ADS_TAG, "Rewarded show failed: ${adError.message}")
                preload()
            }
        }

        rewarded.show(activity) { rewardItem ->
            onRewardEarned(rewardItem)
        }
    }
}
