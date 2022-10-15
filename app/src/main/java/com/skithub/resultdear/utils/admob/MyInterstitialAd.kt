package com.skithub.resultdear.utils.admob

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.skithub.resultdear.utils.CommonMethod
import com.skithub.resultdear.utils.LoadingDialog
import com.skithub.resultdear.utils.MyExtensions.shortToast
import com.skithub.resultdear.utils.SharedPreUtils
import com.skithub.resultdear.utils.fan.FANInterstitialAd

class MyInterstitialAd(val context: Context) {

    var isClickedBackButton = false
    lateinit var loadingDialog : LoadingDialog
    private var interstitialAdListener : MyInterstitialAdListener? = null

    init {
        load()
    }

    interface MyInterstitialAdListener{
        fun onAdDismissedFullScreenContent()
        fun onAdFailedToShowFullScreenContent(adError: AdError?)
        fun onAdShowedFullScreenContent()
        fun onAdFailedToLoad(adError: LoadAdError?)
        fun onAdLoaded(interstitialAd: InterstitialAd)
    }

    companion object{
        val AD_LIMIT = false
        val AD_SHOW_HOUR = 24
        val AD_SIZE = 1000000


        private val AD_UNIT_ID: String = "ca-app-pub-7170895924418716/6104788784" //real ad //mother.admob
        //private val AD_UNIT_ID: String = "ca-app-pub-4815123169795375/2041010606" //real ad //sikderItHub
        //private val AD_UNIT_ID: String = "ca-app-pub-8326396827024206/9759913678" //real ad //daddy
        //private val AD_UNIT_ID: String = "ca-app-pub-3940256099942544/1033173712" //test  ad

        private var mInterstitialAd: InterstitialAd? = null
        private final var TAG = "LotteryNumberCheckActivity"
        private var mAdIsLoading: Boolean = false

        fun isAdAvailable(): Boolean {
            if(!mAdIsLoading && mInterstitialAd!=null){
                return true
            }

            return false
        }

    }


    init {
        loadingDialog = LoadingDialog(context as Activity)
    }

    fun onBackPress(){
        isClickedBackButton = true
        if(isAdAvailable()){
            showInterstitial()
        }else{
            //loadingDialog.show()
            //load()
            //Toast.makeText(context, "isAdAvailable not", Toast.LENGTH_SHORT).show()
            finishActivity()
        }
    }

    fun showInterstitial() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad was dismissed.")
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    mInterstitialAd = null
                    //loadAd()
                    //checkNumber()
                    //load()
                    //interstitialAdListener?.onAdDismissedFullScreenContent()

                    finishActivity()

                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    Log.d(TAG, "Ad failed to show. "+p0.message)
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    mInterstitialAd = null
                    //checkNumber()
                    //interstitialAdListener?.onAdFailedToShowFullScreenContent(adError)

                    finishActivity()

                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed fullscreen content.")
                    // Called when ad is dismissed.
                    //interstitialAdListener?.onAdShowedFullScreenContent()
                    //SharedPreUtils.setLastAdTimeToStorage(context)
                }
            }
            mInterstitialAd?.show(context as Activity)
        }else{
            //checkNumber()
            interstitialAdListener?.onAdDismissedFullScreenContent()
            if (!mAdIsLoading && mInterstitialAd == null) {
                mAdIsLoading = true
                //loadAd()
                //load()
            }
        }
    }


    private fun loadAd() {
        var adRequest = AdRequest.Builder().build()


        SharedPreUtils.setLastAdTimeToStorage(context)

        InterstitialAd.load(
            context, AD_UNIT_ID, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.message)
                    mInterstitialAd = null
                    mAdIsLoading = false

                    //interstitialAdListener?.onAdFailedToLoad(adError)

                    val error = "domain: ${adError.domain}, code: ${adError.code}, " +
                            "message: ${adError.message}"

                    Log.d("AdmobAd", error)

                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d("AdmobAd", "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                    mAdIsLoading = false
                    //interstitialAdListener?.onAdLoaded(interstitialAd)
                    //showInterstitial()


                }
            }
        )
    }

    fun load(){
        if (!mAdIsLoading && mInterstitialAd == null && isAdShownAllowed()) {
            mAdIsLoading = true
            Log.d("AdmobAd", "Ad loading..")
            loadAd()
        }else{
            Log.d("AdmobAd", "Ad Loading not allowed")


            //loadingDialog.hide()
            //interstitialAdListener?.onAdFailedToLoad(null)

            //finishActivity()
        }
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

        val lastAdTime = SharedPreUtils.getLastAdTimeWithoutSuspend(context)

        val adCount = SharedPreUtils.getAdCountWithoutSuspend(context)

        val hour = CommonMethod.getHoursDifBetweenToTime(currentTime, lastAdTime)

        Log.d("AdmobAd", "hour diff "+CommonMethod.getHoursDifBetweenToTime(currentTime, lastAdTime))
        Log.d("AdmobAd", "Min diff "+CommonMethod.getMinDifBetweenToTime(currentTime, lastAdTime))

        return FANInterstitialAd.AD_LOADING_MIN <= CommonMethod.getMinDifBetweenToTime(currentTime, lastAdTime)



        return if(hour >= AD_SHOW_HOUR){
            true
        }else{
            adCount< AD_SIZE
        }
    }

    @SuppressLint("LongLogTag")
    private fun finishActivity(){
        Log.d("LotterySerialCheckActivity", "finishCalled")
        loadingDialog.hide()
        (context as Activity).finish()
        //goToMainactivity()
    }



}