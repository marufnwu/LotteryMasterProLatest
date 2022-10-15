package com.skithub.resultdear.utils.ironsource

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.integration.IntegrationHelper
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.sdk.InterstitialListener
import com.skithub.resultdear.utils.CommonMethod
import com.skithub.resultdear.utils.GenericDialog
import com.skithub.resultdear.utils.MyExtensions.lifecycleOwner
import com.skithub.resultdear.utils.MyExtensions.shortToast
import com.skithub.resultdear.utils.SharedPreUtils
import com.skithub.resultdear.utils.admob.MyInterstitialAd
import com.skithub.resultdear.utils.fan.FANInterstitialAd

class IsInterstitialAd(public val activity: Activity) {
    companion object{
        var isAdLoaded = false
        var isAdLoading = false
    }
    init {
        IronSource.init(activity, "161495f55")
        IntegrationHelper.validateIntegration(activity);
        initLifeCycle()
        setListener()
        load()
    }


    private fun initLifeCycle() {
        val lifeCycle: LifecycleOwner? = (activity as Context).lifecycleOwner()
        if (lifeCycle != null) {
            lifeCycle.lifecycle
                .addObserver(LifecycleEventObserver { _, event ->
                    when(event){
                        Lifecycle.Event.ON_DESTROY-> onDestroy()
                        Lifecycle.Event.ON_RESUME-> onResume()
                        Lifecycle.Event.ON_PAUSE-> onPause()
                        else->{

                        }
                    }
                })
        } else {
        }


    }

    private fun onDestroy() {

    }
    private fun onResume() {
        IronSource.onResume(activity)
    }
    private fun onPause() {
        IronSource.onPause(activity)
    }

    fun load(){
        if (!isAdLoading && !isAdLoaded && isAdShownAllowed()) {
            isAdLoading = true
            Log.d("IronSource", "Ad loading..")
            IronSource.loadInterstitial()
        }else{
            Log.d("IronSource", "Ad Loading not allowed")

        }
    }

    private fun setListener() {
        IronSource.setInterstitialListener(object : InterstitialListener {
            override fun onInterstitialAdReady() {
                isAdLoaded = true
                isAdLoading = false
                SharedPreUtils.setLastAdTimeToStorage(activity)
            }

            override fun onInterstitialAdLoadFailed(p0: IronSourceError?) {
                isAdLoaded = false
                isAdLoading = false
                Log.d("IronSource", "onInterstitialAdLoadFailed "+p0.toString())
            }

            override fun onInterstitialAdOpened() {
                Log.d("IronSource", "onInterstitialAdOpened")
            }

            override fun onInterstitialAdClosed() {
                isAdLoaded = false
                finishActivity()
                Log.d("IronSource", "onInterstitialAdClosed")
            }

            override fun onInterstitialAdShowSucceeded() {
                isAdLoaded = false
                Log.d("IronSource", "onInterstitialAdShowSucceeded")
            }

            override fun onInterstitialAdShowFailed(p0: IronSourceError?) {
                isAdLoaded = false
                finishActivity()
                Log.d("IronSource", "onInterstitialAdShowSucceeded")
            }

            override fun onInterstitialAdClicked() {
                Log.d("IronSource", "onInterstitialAdClicked")
            }

        })
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

        val lastAdTime = SharedPreUtils.getLastAdTimeWithoutSuspend(activity)

        val adCount = SharedPreUtils.getAdCountWithoutSuspend(activity)

        val hour = CommonMethod.getHoursDifBetweenToTime(currentTime, lastAdTime)

        Log.d("IronSource", "hour diff "+ CommonMethod.getHoursDifBetweenToTime(currentTime, lastAdTime))
        Log.d("IronSource", "Min diff "+ CommonMethod.getMinDifBetweenToTime(currentTime, lastAdTime))

        return FANInterstitialAd.AD_LOADING_MIN <= CommonMethod.getMinDifBetweenToTime(currentTime, lastAdTime)

    }

    @SuppressLint("LongLogTag")
    private fun finishActivity(){
        Log.d("LotterySerialCheckActivity", "finishCalled")
        activity.finish()
    }

    fun onBackPress(){
        Log.d("IronSource", "onBackPress, isAdLoaded $isAdLoaded")

        if(isAdLoaded){
            IronSource.showInterstitial()
        }else{
            finishActivity()
        }
    }



}