package com.hadify.NumberMerge2048

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val CONSENT_TAG = "Consent"

/**
 * Google User Messaging Platform (UMP) wrapper. Shows whichever privacy message the player's
 * region needs (the GDPR consent form in the EEA/UK, the US state privacy-law opt-out message in
 * regulated US states) before any ad is requested, and exposes whether Settings must offer a
 * "Privacy options" entry.
 *
 * The messages themselves are configured and published in AdMob -> Privacy & messaging.
 */
class ConsentManager(context: Context) {
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context.applicationContext)

    /** True when Settings must show a "Privacy options" entry (GDPR or US state regulations). */
    var privacyOptionsRequired by mutableStateOf(isPrivacyOptionsRequired())
        private set

    /** True once the SDK says ads may be requested (consent obtained or not required). */
    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    /**
     * Refreshes the consent status (every app launch, as Google requires) and shows the consent
     * form if one is required and not answered yet. [onDone] always runs on the main thread, also
     * after an error (usually offline); callers then check [canRequestAds].
     */
    fun gatherConsent(activity: Activity, onDone: () -> Unit) {
        val params = ConsentRequestParameters.Builder().apply {
            // Debug builds only: pretend to be in a regulated region so the forms can be tested on
            // emulators / registered test devices. Never active in release builds.
            val debugGeography = when (BuildConfig.UMP_DEBUG_GEOGRAPHY) {
                "EEA" -> ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA
                "US_STATE" -> ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_REGULATED_US_STATE
                else -> null
            }
            if (BuildConfig.DEBUG && debugGeography != null) {
                setConsentDebugSettings(
                    ConsentDebugSettings.Builder(activity)
                        .setDebugGeography(debugGeography)
                        .build()
                )
            }
        }.build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(CONSENT_TAG, "Consent form: ${formError.errorCode} ${formError.message}")
                    }
                    refreshPrivacyOptions()
                    onDone()
                }
            },
            { requestError ->
                // Previously stored consent (if any) still applies.
                Log.w(CONSENT_TAG, "Consent info update: ${requestError.errorCode} ${requestError.message}")
                refreshPrivacyOptions()
                onDone()
            },
        )
    }

    /** Opens the privacy options form so the player can review/withdraw consent or opt out. */
    fun showPrivacyOptionsForm(activity: Activity, onDismissed: (FormError?) -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) {
                Log.w(CONSENT_TAG, "Privacy options form: ${formError.errorCode} ${formError.message}")
            }
            refreshPrivacyOptions()
            onDismissed(formError)
        }
    }

    private fun refreshPrivacyOptions() {
        privacyOptionsRequired = isPrivacyOptionsRequired()
    }

    private fun isPrivacyOptionsRequired(): Boolean =
        consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
}

/**
 * Process-wide ads switch. Nothing talks to AdMob (no SDK init, no ad request) until [start] is
 * called, and [start] is only called once the UMP consent flow allows ad requests.
 */
object AdsRuntime {
    private val initStarted = AtomicBoolean(false)

    /** Banners and rewarded ads only load while this is true. */
    var adsEnabled by mutableStateOf(false)
        private set

    fun start(context: Context) {
        if (!initStarted.compareAndSet(false, true)) {
            return
        }
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            // Google recommends initializing the Mobile Ads SDK off the main thread.
            MobileAds.initialize(appContext) {}
            withContext(Dispatchers.Main) {
                adsEnabled = true
            }
        }
    }
}
