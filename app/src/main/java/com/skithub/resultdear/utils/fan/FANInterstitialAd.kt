package com.skithub.resultdear.utils.fan

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.icu.util.TimeUnit
import android.util.Log
import android.util.TimeUtils
import android.widget.Toast
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener

import com.google.android.gms.ads.LoadAdError
import com.skithub.resultdear.ui.lottery_number_check.LotteryNumberCheckActivity
import com.skithub.resultdear.ui.lottery_serial_check.LotterySerialCheckActivity
import com.skithub.resultdear.utils.*
import com.skithub.resultdear.utils.admob.MyInterstitialAd


class FANInterstitialAd(val context: Context, val adSourceListener: AdSourceListener) {
    private lateinit var interstitialAdListener: InterstitialAdListener
    private lateinit var fanInterstitialAdListener: FanInterstitialAdListener
    private var customAdPopup: CustomAdPopup = CustomAdPopup(context)

    interface FanInterstitialAdListener{
        fun onAdDismissedFullScreenContent()
        fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError?)
        fun onAdShowedFullScreenContent()
        fun onAdFailedToLoad(adError: LoadAdError?)
        fun onAdLoaded(interstitialAd: com.google.android.gms.ads.interstitial.InterstitialAd)
    }

    companion object{
        public var interstitialAd : InterstitialAd? = null
        const val placementId = "1000235817291984_1000238193958413"
        //const val placementId = "414956540690437_414962077356550" //test
        const val TAG = "FAN_INTERSTITIAL_AD"
        const val  AD_LOADING_MIN = 1
        private var mAdIsLoading: Boolean = false

        val AD_SHOW_HOUR = 24
        val AD_SIZE = 4

        fun isAdAvailable(): Boolean {
            if(interstitialAd!=null && interstitialAd!!.isAdLoaded && !interstitialAd!!.isAdInvalidated){
                return true
            }

            return false
        }
    }

    init {
        if(interstitialAd==null){
            Log.d("FacebookAd", "Add is null")

            interstitialAd = InterstitialAd(context, placementId)
        }else{
            Log.d("FacebookAd", "Add not null")

        }

        interstitialAdListener = object : InterstitialAdListener {
            override fun onError(p0: Ad?, p1: AdError?) {
                Log.d("FacebookAd", p1!!.errorCode.toString())
                // Don't forget to set the ad reference to null so you
                // don't show the ad a second time.
                //loadAd()
                //checkNumber()
                //interstitialAd = null
                //load()
                //interstitialAdListener?.onAdDismissedFullScreenContent()




                mAdIsLoading = false


                //finishActivity()
            }

            override fun onAdLoaded(p0: Ad?) {
                Log.d("FacebookAd", "loaded")
                mAdIsLoading =false

            }

            override fun onAdClicked(p0: Ad?) {
            }

            override fun onLoggingImpression(p0: Ad?) {
            }

            override fun onInterstitialDisplayed(p0: Ad?) {
                customAdPopup.activityMap[context] = true
            }

            override fun onInterstitialDismissed(p0: Ad?) {
                Log.d("FacebookAd", "Ad was dismissed.")
                // Don't forget to set the ad reference to null so you
                // don't show the ad a second time.
                //loadAd()
                //checkNumber()
                //load()
                //interstitialAdListener?.onAdDismissedFullScreenContent()

                finishActivity()
                //adSourceListener.adDismissed()
            }

        }

        load()

    }

    fun loadAd(){
        if(!interstitialAd!!.isAdLoaded && !mAdIsLoading){
            load()
        }
    }

    private fun forceAdLoad(){
        if(!interstitialAd!!.isAdLoaded && !mAdIsLoading){
            SharedPreUtils.setLastFanAdTimeToStorage(context)

            Log.d("FacebookAd", "Ad loading...")
            mAdIsLoading = true
            interstitialAd?.loadAd(
                interstitialAd?.buildLoadAdConfig()
                    ?.withAdListener(interstitialAdListener)
                    ?.build())
        }
    }

    private fun load(){
        if(!interstitialAd!!.isAdLoaded && !mAdIsLoading && isAdShownAllowed()){
            SharedPreUtils.setLastFanAdTimeToStorage(context)

            Log.d("FacebookAd", "Ad loading...")
            mAdIsLoading = true
            interstitialAd?.loadAd(
                interstitialAd?.buildLoadAdConfig()
                    ?.withAdListener(interstitialAdListener)
                    ?.build())
        }
    }

    fun show(){
        if(interstitialAd!!.isAdLoaded){
            interstitialAd!!.show()
        }
    }

    fun onBackPress(){
        if(isAdAvailable()){
            show()
        }else{
            //loadingDialog.show()

            //Toast.makeText(context, "isAdAvailable not", Toast.LENGTH_SHORT).show()
            //finishActivity()
            //adSourceListener.onError("Ad loading error")

            if(context !is LotteryNumberCheckActivity){
                if(CommonMethod.isCustomPopupShownAllowed(context)){
                    load()
                    customAdPopup.onBackPress()
                    return
                }

            }

            finishActivity()
        }
    }

    @SuppressLint("LongLogTag")
    private fun finishActivity(){
        Log.d("LotterySerialCheckActivity", "finishCalled")
        //loadingDialog.hide()
        (context as Activity).finish()
        //goToMainactivity()
    }

    @SuppressLint("SimpleDateFormat")
    fun isAdShownAllowed(): Boolean{
        if (CommonMethod.accountAge!=null){
            if(CommonMethod.accountAge!!.toInt()<=4){
                return  false
            }
        }else{
            return false
        }





        val currentTime =  System.currentTimeMillis()

        val lastAdTime = SharedPreUtils.getLastFanAdTimeWithoutSuspend(context)

        val adCount = SharedPreUtils.getFanAdCountWithoutSuspend(context)

        val hour = CommonMethod.getHoursDifBetweenToTime(currentTime, lastAdTime)

        val v = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(currentTime)


        Log.d("FacebookAd", "last  $lastAdTime")
        Log.d("FacebookAd", "current  $currentTime")
        Log.d("FacebookAd", "hour diff "+CommonMethod.getHoursDifBetweenToTime(currentTime, lastAdTime))
        Log.d("FacebookAd", "Min diff "+CommonMethod.getMinDifBetweenToTime(currentTime, lastAdTime))

        return AD_LOADING_MIN <= CommonMethod.getMinDifBetweenToTime(currentTime, lastAdTime)


        return if(hour >= AD_SHOW_HOUR){
            true
        }else{
            adCount< AD_SIZE
        }
    }
}