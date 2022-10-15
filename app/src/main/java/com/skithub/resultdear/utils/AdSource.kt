package com.skithub.resultdear.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.skithub.resultdear.utils.MyExtensions.shortToast
import com.skithub.resultdear.utils.admob.MyInterstitialAd
import com.skithub.resultdear.utils.fan.FANInterstitialAd
import com.skithub.resultdear.utils.ironsource.IsInterstitialAd

class AdSource(val context:Context) : AdSourceListener {
    private lateinit var fanInterstitialAd: FANInterstitialAd
    private lateinit var myInterstitialAd: MyInterstitialAd
    private lateinit var isInterstitialAd: IsInterstitialAd
    private lateinit var customAdPopup: CustomAdPopup



    enum class Type{
        FAN,
        ADMOB,
        IRON_SOURCE
    }


    companion object{
        var adSource : String? = null
    }

    init {
        customAdPopup = CustomAdPopup(context)
        if(adSource!=null){
            if(adSource.equals("fan", true)){
                fanInterstitialAd = FANInterstitialAd(context, this)
            }else if(adSource.equals("admob", true)){
                myInterstitialAd = MyInterstitialAd(context)
            }else if(adSource.equals("ironsource", true)){
                isInterstitialAd = IsInterstitialAd(context as Activity)
            }

        }else{
            Log.d("Admob", "Interstitial ad is off")
        }


    }

    public fun onBackPress(){
        try {
            if(adSource!=null){

                if(adSource.equals("fan")){
                    fanInterstitialAd.onBackPress()
                }else if(adSource.equals("admob", true)){
                    myInterstitialAd.onBackPress()
                }else if(adSource.equals("ironsource", true)){
                    isInterstitialAd.onBackPress()
                }else {
                    (context as Activity).finish()
                }

            }else{
                Log.d("Admob", "Interstitial ad is off")
            }
        }catch (e:Exception){
            (context as Activity).finish()
        }
    }

    private fun finishActivity(){
        (context as Activity).finish()
    }

    override fun adShown(type: Type) {

    }

    override fun adDismissed() {
        finishActivity()
    }

    override fun onError(message: String) {
        customAdPopup.onBackPress()
    }
}

interface AdSourceListener{
    fun adShown(type: AdSource.Type)
    fun adDismissed()
    fun onError(message: String)
}