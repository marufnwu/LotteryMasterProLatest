package com.skithub.resultdear.ui

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.facebook.ads.AudienceNetworkAds
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.Gson
import com.skithub.resultdear.R
import com.skithub.resultdear.database.network.MyApi
import com.skithub.resultdear.database.network.api.RetrofitClient
import com.skithub.resultdear.database.network.api.SecondServerApi
import com.skithub.resultdear.model.FacebookVideoModel
import com.skithub.resultdear.model.response.AudioTutorialResponse
import com.skithub.resultdear.model.response.GenericApiResponse
import com.skithub.resultdear.utils.CustomAdPopup
import com.skithub.resultdear.utils.CommonMethod
import com.skithub.resultdear.utils.Coroutines
import com.skithub.resultdear.utils.MyExtensions.shortToast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyApplication : Application() {

    lateinit var firebaseAnalytics: FirebaseAnalytics
    val MY_NOTIFICATION_CHANNEL_ID: String="MY_NOTIFICATION_CHANNEL_ID"
    private lateinit var notificationManager: NotificationManager
    val myApi by lazy {
        MyApi.invoke()
    }

    val secondServerApi by lazy {
        SecondServerApi.invoke()
    }

    public val iRetrofitApiCall by lazy {
        RetrofitClient.invoke()
    }


    override fun onCreate() {
        super.onCreate()
        AudienceNetworkAds.initialize(this);
        initFirebaseServices()

        createNotificationChannel()

        //checkDeviceBlock()

        //getVoiceMessageList()
    }

    private fun getVoiceMessageList() {

        Coroutines.main {
            val response = myApi.getRandomVideo()

            if(response.isSuccessful && response.body()!=null){
                if(!response.body()?.error!!){
                    if(response.body()!!.data!=null){
                        CustomAdPopup.videoList.addAll(response.body()!!.data!!)
                    }


                }
            }
        }

    }

    private fun checkDeviceBlock() {

        myApi.checkDeviceBlock(CommonMethod.deviceId(this))
            .enqueue(object: Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                    if(response.body()==1){
                        System.exit(0);
                    }
                }

                override fun onFailure(call: Call<Int>, t: Throwable) {
                    shortToast(t.message.toString())
                }

            })
    }


    private fun initFirebaseServices() {
        FirebaseApp.initializeApp(this)
        firebaseAnalytics = Firebase.analytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        Firebase.messaging.isAutoInitEnabled = true
    }

    private fun createNotificationChannel() {
        notificationManager=getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            val notificationChannel: NotificationChannel= NotificationChannel(MY_NOTIFICATION_CHANNEL_ID,resources.getString(R.string.app_name),NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun attachBaseContext(base: Context?) {
        if (base!=null) {
            super.attachBaseContext(CommonMethod.updateLanguage(base))
        } else {
            super.attachBaseContext(base)
        }
    }



}