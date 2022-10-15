package com.skithub.resultdear.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import com.skithub.resultdear.utils.admob.MyInterstitialAd

import java.text.SimpleDateFormat
import java.util.*

object SharedPreUtils {

    var sharedPreferences: SharedPreferences?=null
    val sharedPreferenceName: String="MyPreference"

    private fun initSharedPref(context: Context): SharedPreferences {
        if (sharedPreferences==null) {
            sharedPreferences=context.getSharedPreferences(sharedPreferenceName,MODE_PRIVATE)
        }
        return sharedPreferences!!
    }

    suspend fun setStringToStorage(context: Context,key: String, value: String) {
        val editor: SharedPreferences.Editor=initSharedPref(context).edit()
        editor.putString(key,value)
        editor.apply()
    }

    @SuppressLint("SimpleDateFormat")
     fun setLastAdTimeToStorage(context: Context) {
        var adCount = getAdCountWithoutSuspend(context)
        val editor: SharedPreferences.Editor=initSharedPref(context).edit()
        val currentTimesInMill = System.currentTimeMillis()


        editor.putLong("AdmobAdTime",currentTimesInMill)
        editor.apply()
    }

    @SuppressLint("SimpleDateFormat")
     fun setCustomPopupTimeToStorage(context: Context) {
        var adCount = getAdCountWithoutSuspend(context)
        val editor: SharedPreferences.Editor=initSharedPref(context).edit()
        val currentTimesInMill = System.currentTimeMillis()


        editor.putLong("CustomPopup",currentTimesInMill)
        editor.apply()
    }


    @SuppressLint("SimpleDateFormat")
    fun setLastFanAdTimeToStorage(context: Context) {
        var adCount = getFanAdCountWithoutSuspend(context)
        val editor: SharedPreferences.Editor=initSharedPref(context).edit()
        val currentTimesInMill = System.currentTimeMillis()


        //editor.putInt("FanAdCount", 1)
        editor.putLong("FanAdTime",currentTimesInMill)
        editor.apply()
    }



    fun getLastAdTimeWithoutSuspend(context: Context) : Long {
        return initSharedPref(context).getLong("AdmobAdTime",0)
    }

    fun getLastCustomPopupWithoutSuspend(context: Context) : Long {
        return initSharedPref(context).getLong("CustomPopup",0)
    }

    fun getLastFanAdTimeWithoutSuspend(context: Context) : Long {
        return initSharedPref(context).getLong("FanAdTime",0)
    }

     fun getAdCountWithoutSuspend(context: Context) : Int {
         val adCount = initSharedPref(context).getInt("AdCount",0)
         Log.d("Ad Count", adCount.toString())
        return adCount
    }

    fun getFanAdCountWithoutSuspend(context: Context) : Int {
        val adCount = initSharedPref(context).getInt("FanAdCount",0)
        Log.d("Ad Count", adCount.toString())
        return adCount
    }

    suspend fun setBooleanToStorage(context: Context,key: String, value: Boolean) {
        val editor: SharedPreferences.Editor=initSharedPref(context).edit()
        editor.putBoolean(key,value)
        editor.apply()
    }

    suspend fun setIntToStorage(context: Context,key: String, value: Int) {
        val editor: SharedPreferences.Editor=initSharedPref(context).edit()
        editor.putInt(key,value)
        editor.apply()
    }

    suspend fun setLongToStorage(context: Context,key: String, value: Long) {
        val editor: SharedPreferences.Editor=initSharedPref(context).edit()
        editor.putLong(key,value)
        editor.apply()
    }

    suspend fun getLongFromStorage(context: Context,key: String, defaultValue: Long): Long {
        return initSharedPref(context).getLong(key,defaultValue)
    }

    suspend fun getStringFromStorage(context: Context,key: String, defaultValue: String?) : String? {
        return initSharedPref(context).getString(key,defaultValue)
    }

    fun getStringFromStorageWithoutSuspend(context: Context,key: String, defaultValue: String?) : String? {
        return initSharedPref(context).getString(key,defaultValue)
    }

    suspend fun getBooleanFromStorage(context: Context,key: String, defaultValue: Boolean) : Boolean {
        return initSharedPref(context).getBoolean(key,defaultValue)
    }

    suspend fun getIntFromStorage(context: Context,key: String, defaultValue: Int) : Int {
        return initSharedPref(context).getInt(key,defaultValue)
    }



}