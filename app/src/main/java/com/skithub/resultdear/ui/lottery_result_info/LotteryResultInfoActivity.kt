package com.skithub.resultdear.ui.lottery_result_info

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.skithub.resultdear.R
import com.skithub.resultdear.adapter.LmpClassVideoAdapter
import com.skithub.resultdear.adapter.LotteryResultRecyclerAdapter
import com.skithub.resultdear.adapter.MediaPlayerTutorialAdapter
import com.skithub.resultdear.adapter.VideoTutorialAdapter
import com.skithub.resultdear.database.network.ApiInterface
import com.skithub.resultdear.database.network.MyApi
import com.skithub.resultdear.database.network.RetrofitClient
import com.skithub.resultdear.database.network.api.SecondServerApi
import com.skithub.resultdear.databinding.ActivityLotteryResultInfoBinding
import com.skithub.resultdear.databinding.ConnectionCheckDialogBinding
import com.skithub.resultdear.model.*
import com.skithub.resultdear.model.response.VideoResponse
import com.skithub.resultdear.model.response.VideoTypeResposne
import com.skithub.resultdear.model.response.lmpVideoResponse
import com.skithub.resultdear.ui.MyApplication
import com.skithub.resultdear.ui.PlayerActivity
import com.skithub.resultdear.ui.lmpclass_videos.LmpClassVideoActivity
import com.skithub.resultdear.utils.*
import com.skithub.resultdear.utils.MyExtensions.shortToast
import com.skithub.resultdear.utils.admob.MyInterstitialAd
import com.skithub.resultdear.utils.fan.FANInterstitialAd
import org.json.JSONArray
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class LotteryResultInfoActivity : AppCompatActivity() {
    lateinit var lmpClassVideoAdapter: LmpClassVideoAdapter
    private var lmpVideo : MutableList<Any> = mutableListOf<Any>()
    var audioStatusList: MutableList<AudioStatus> = ArrayList<AudioStatus>()
    var pastVisibleItem : Int =0
    var visibleItemCount = 0
    var totalItemCount=0
    var previousTotal = 0

    private var PAGE = 1
    private var PAGE_SIZE= 30
    private var TOTAL_PAGE = 0
    var isLoading = true


    lateinit var myInterstitialAd: MyInterstitialAd
    lateinit var fanInterstitialAd: FANInterstitialAd


    private lateinit var secondServerApi: SecondServerApi
    private lateinit var videoAdapter: VideoTutorialAdapter
    private lateinit var videoLayoutManager: LinearLayoutManager
    private var videoList: MutableList<Video> = arrayListOf()
    private var ytList: MutableList<VideoTutorModel> = arrayListOf()

    private lateinit var myApi: MyApi
    private var resultSlotId: Int = 0
    private lateinit var binding: ActivityLotteryResultInfoBinding
    private lateinit var viewModelLottery: LotteryResultInfoViewModel
    private var resultDate: String=CommonMethod.increaseDecreaseDaysUsingValue(0, Locale.ENGLISH)
    private var resultTime: String=Constants.eveningTime
    private var resultDateTwo: String=CommonMethod.increaseDecreaseDaysUsingValue(-2, Locale.ENGLISH)
    private var list: MutableList<LotteryNumberModel> = arrayListOf()
    private var finalList: MutableList<LotteryResultRecyclerModel> = arrayListOf()
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: LotteryResultRecyclerAdapter
    private var adsImageList: MutableList<AdsImageModel> = arrayListOf()
    private lateinit var connectivityManager: ConnectivityManager
    //val CUSTOM_PREF_NAME = "User_data_extra"
    private var connectionAlertDialog: AlertDialog?=null
    private lateinit var connectionDialogBinding: ConnectionCheckDialogBinding

    private var apiInterface: ApiInterface? = null
    val CUSTOM_PREF_NAME = "User_data_extra"


    private lateinit var ytAdapter: VideoTutorialAdapter
    private lateinit var mediaPlayerAdapter: MediaPlayerTutorialAdapter
    var videoCallingType: LmpClassVideoActivity.VideoCallingType? = null

    lateinit var adSource: AdSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLotteryResultInfoBinding.inflate(layoutInflater)
        val factoryLottery: LotteryResultInfoViewModelFactory= LotteryResultInfoViewModelFactory((application as MyApplication).myApi)
        viewModelLottery=ViewModelProvider(this,factoryLottery).get(LotteryResultInfoViewModel::class.java)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        //myInterstitialAd = MyInterstitialAd(this)
//        fanInterstitialAd = FANInterstitialAd(this)
//        fanInterstitialAd.loadAd()

        adSource= AdSource(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        apiInterface = RetrofitClient.getApiClient().create(ApiInterface::class.java)

        videoCallingType = LmpClassVideoActivity.VideoCallingType.FACEBOOK_VIDEO

        myApi = (application as MyApplication).myApi
        secondServerApi = (application as MyApplication).secondServerApi

        val bundle=intent.extras
        if (bundle!=null) {
            resultDate=bundle.getString(Constants.resultDateKey,CommonMethod.increaseDecreaseDaysUsingValue(0, Locale.ENGLISH))
            resultTime=bundle.getString(Constants.resultTimeKey, Constants.noonTime)
            resultSlotId=bundle.getInt(Constants.resultSlotIdKey,0)
        }

        initAll()

        layoutManager = LinearLayoutManager(this)
        binding.recyLotteryClass.layoutManager = layoutManager
        binding.recyLotteryClass.setHasFixedSize(true)
        initTutorialRecyScrollListener()


        lmpClassVideoAdapter = LmpClassVideoAdapter(this, lmpVideo)
        binding.recyLotteryClass.adapter = lmpClassVideoAdapter



        if (CommonMethod.haveInternet(connectivityManager)) {
            setUpRecyclerView()
            loadLotteryNumberInfoUsingDateAndTime()
            //loadLotteryNumberInfoUsingDateAndTimeSecondServer()
            loadAdsImageInfo()
            checkBannerAd()
        }else{
            noInternetDialog(getString(R.string.no_internet),getString(R.string.no_internet_message))
        }



    }

    private fun checkBannerAd(){
        Coroutines.main {

            try {
                val res =  myApi.getBanner("ResultPageBannerAd")
                if(res.isSuccessful && res.body()!=null){
                    res.body()!!.let { banner ->
                        if(!banner!!.error){
                            if (banner.imageUrl != null) {
                                binding.rlAdRoot.visibility = View.VISIBLE

                                binding.adUpArrowBtn.setImageResource(R.drawable.ic_arrow_down_icon)
                                binding.adUpArrowBtn.visibility = View.VISIBLE

                                binding.adUpArrowBtn.setOnClickListener {
                                    binding.adUpArrowBtn.visibility = View.GONE
                                    binding.adDownArrowBtn.visibility = View.VISIBLE
                                    binding.imageBanner.visibility = View.GONE
                                    /*val hide: Animation =
                                        AnimationUtils.loadAnimation(this@MainActivity, R.anim.top_bottom)
                                    binding.adLayout.startAnimation(hide)*/
                                }
                                binding.adDownArrowBtn.setOnClickListener {
                                    binding.adUpArrowBtn.visibility = View.VISIBLE
                                    binding.adDownArrowBtn.visibility = View.GONE
                                    binding.imageBanner.visibility = View.VISIBLE
                                    /*val hide: Animation =
                                        AnimationUtils.loadAnimation(this@MainActivity, R.anim.bottom_top)
                                    binding.adLayout.startAnimation(hide)*/
                                }

                                Glide.with(applicationContext)
                                    .load(banner.imageUrl)
                                    .thumbnail(Glide.with(applicationContext).load(R.drawable.placeholder))
                                    .into(binding.imageBanner)
                                binding.imageBanner.setOnClickListener(View.OnClickListener {
                                    if (banner.actionType === 1) {
                                        //open url
                                        if (banner.actionUrl != null) {
                                            val url: String = banner.actionUrl!!
                                            val linkHost = Uri.parse(url).host
                                            val uri = Uri.parse(url)
                                            if (linkHost == null) {
                                                return@OnClickListener
                                            }
                                            if (linkHost == "play.google.com") {
                                                val appId = uri.getQueryParameter("id")
                                                val intent = Intent(Intent.ACTION_VIEW)
                                                intent.data = Uri.parse("market://details?id=$appId")
                                                startActivity(intent)
                                            } else if (linkHost == "www.youtube.com") {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    intent.setPackage("com.google.android.youtube")
                                                    startActivity(intent)
                                                }catch (e : Exception){
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    startActivity(intent)
                                                }
                                            } else if(url.endsWith(".mp4") || url.endsWith(".mpeg") || url.endsWith(".mpd") ||
                                                url.startsWith("https://lmpclass.sikderithub.com/embed") || url.startsWith("http://lmpclass.sikderithub.com/embed")){
                                                val intent = Intent(this, PlayerActivity::class.java)
                                                intent.putExtra("url", url)
                                                startActivity(intent)
                                            }else if (url != null && (url.startsWith("http://") || url.startsWith(
                                                    "https://"
                                                ))
                                            ) {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                startActivity(intent)
                                            }
                                        }
                                    } else if (banner.actionType === 2) {
                                        //open activity
                                    }
                                })

                            }else{
                                binding.rlAdRoot.visibility = View.GONE
                            }
                        }else{
                            binding.rlAdRoot.visibility = View.GONE
                            Log.d("Banner", banner.msg!!)
                        }
                    }
                }

            }catch (e : Exception){

            }


        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun noInternetDialog(til: String, msg: String) {
        connectionDialogBinding= ConnectionCheckDialogBinding.inflate(layoutInflater)
        connectionDialogBinding.connectionTitle.text = til
        connectionDialogBinding.connectionMessage.text = msg
        connectionDialogBinding.tryAgainBtn.setOnClickListener {
            if (CommonMethod.haveInternet(connectivityManager)) {
                setUpRecyclerView()
                loadLotteryNumberInfoUsingDateAndTime()
                //loadLotteryNumberInfoUsingDateAndTimeSecondServer()
                loadAdsImageInfo()
                connectionAlertDialog?.dismiss()
            }
        }
        val builder=AlertDialog.Builder(this@LotteryResultInfoActivity)
            .setCancelable(false)
            .setView(connectionDialogBinding.root)
        connectionAlertDialog=builder.create()
        if (connectionAlertDialog?.window!=null) {
            connectionAlertDialog?.window!!.attributes.windowAnimations=R.style.DialogTheme
        }
        if (!isFinishing) {
            connectionAlertDialog?.show()
        }

    }

    private fun initAll() {
        connectivityManager=getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        binding.leftDateTextView.text=resultDate
        binding.leftTimeTextView.text=resultTime
        binding.rightDateTextView.text=resultDate
        binding.rightTimeTextView.text=resultTime
        binding.stateNameTextView.text=resources.getString(R.string.nagaland_state)



    }

    private fun setUpRecyclerView() {
        layoutManager= LinearLayoutManager(this)
        adapter= LotteryResultRecyclerAdapter(this,finalList,adsImageList)
        binding.resultRecyclerView.layoutManager=layoutManager
        binding.resultRecyclerView.adapter=adapter

        ViewCompat.setNestedScrollingEnabled(binding.resultRecyclerView, false);
    }

    private fun loadAdsImageInfo() {
        Coroutines.main {
            try {
                adsImageList.clear()
                val response=viewModelLottery.getAdsImageInfo()
                if (response.isSuccessful && response.code()==200) {
                    if (response.body()!=null) {
                        if (response.body()?.status.equals("success")) {
                            try {
                                adsImageList.addAll(response.body()?.data!!)
                            } finally {
                                adapter.notifyDataSetChanged()
                            }

                        }
                    }
                }
            } catch (e: Exception) {

            }
        }
    }

    private fun loadLotteryNumberInfoUsingDateAndTime() {
        Coroutines.main {
            try {
                binding.spinKit.visibility=View.VISIBLE
                binding.resultRootLayout.visibility=View.GONE
                binding.waitingRootLayout.visibility=View.GONE
                val response=viewModelLottery.getLotteryNumberListByDateSlot(resultDate,resultSlotId,
                    SharedPreUtils.getStringFromStorageWithoutSuspend(this,Constants.userIdKey,Constants.defaultUserId).toString())
                binding.spinKit.visibility=View.GONE
                if (response.isSuccessful && response.code()==200) {
                    if (response.body()!=null) {
                        if (response.body()?.status.equals("success")) {
                            list.clear()
                            list.addAll(response.body()?.data!!)
                            if (list.size>0) {
                                 //getVideoType()
                                 getFacebookVideos(PAGE)


                                filteringLotteryNumber(list)
                                binding.resultRootLayout.visibility=View.VISIBLE
                                binding.waitingRootLayout.visibility=View.GONE
                                supportActionBar?.title = getString(R.string.view_details)

                                /*val rootArray = JSONArray(response.body().toString())
                                for (i in 0 until rootArray.length()) {
                                    val bannerimage = rootArray.getJSONObject(i).getString("viewCount")
                                    Toast.makeText(this,bannerimage,Toast.LENGTH_LONG).show()
                                }*/
                            } else {
                                binding.resultRootLayout.visibility=View.GONE
                                binding.waitingRootLayout.visibility=View.VISIBLE
                                supportActionBar?.title = getString(R.string.result_not_publish_title)
                                /*val Liveuserdb = FirebaseDatabase.getInstance().getReference("ActiveUsers")
                                val prefs = RegisterActivity.PreferenceHelper.customPreference(this, CUSTOM_PREF_NAME)
                                val map: HashMap<String, Any?> = HashMap()
                                map["phone"] = prefs.userPhone
                                map["activity"] = getString(R.string.result_not_publish_title)
                                Liveuserdb.child(prefs.userToken!!).setValue(map)*/
                            }
                        } else {
                            binding.spinKit.visibility=View.GONE
                            binding.resultRootLayout.visibility=View.GONE
                            binding.waitingRootLayout.visibility=View.VISIBLE
                            supportActionBar?.title = getString(R.string.result_not_publish_title)
                            //shortToast("${response.body()?.message}")
                            /*val Liveuserdb = FirebaseDatabase.getInstance().getReference("ActiveUsers")
                            val prefs = RegisterActivity.PreferenceHelper.customPreference(this, CUSTOM_PREF_NAME)
                            val map: HashMap<String, Any?> = HashMap()
                            map["phone"] = prefs.userPhone
                            map["activity"] = getString(R.string.result_not_publish_title)
                            Liveuserdb.child(prefs.userToken!!).setValue(map)*/
                        }
                    } else {
                        binding.spinKit.visibility=View.GONE
                        binding.resultRootLayout.visibility=View.GONE
                        binding.waitingRootLayout.visibility=View.VISIBLE
                        supportActionBar?.title = getString(R.string.result_not_publish_title)
                        shortToast("Sorry, Unknown error occurred.")
                        /*val Liveuserdb = FirebaseDatabase.getInstance().getReference("ActiveUsers")
                        val prefs = RegisterActivity.PreferenceHelper.customPreference(this, CUSTOM_PREF_NAME)
                        val map: HashMap<String, Any?> = HashMap()
                        map["phone"] = prefs.userPhone
                        map["activity"] = getString(R.string.result_not_publish_title)
                        Liveuserdb.child(prefs.userToken!!).setValue(map)*/
                    }
                } else {
                    binding.spinKit.visibility=View.GONE
                    binding.resultRootLayout.visibility=View.GONE
                    binding.waitingRootLayout.visibility=View.VISIBLE
                    supportActionBar?.title = getString(R.string.result_not_publish_title)
                    //shortToast("failed for:- ${response.errorBody()?.string()}")
                    /*val Liveuserdb = FirebaseDatabase.getInstance().getReference("ActiveUsers")
                    val prefs = RegisterActivity.PreferenceHelper.customPreference(this, CUSTOM_PREF_NAME)
                    val map: HashMap<String, Any?> = HashMap()
                    map["phone"] = prefs.userPhone
                    map["activity"] = getString(R.string.result_not_publish_title)
                    Liveuserdb.child(prefs.userToken!!).setValue(map)*/
                }
            } catch (e: Exception) {
                binding.resultRootLayout.visibility=View.GONE
                binding.waitingRootLayout.visibility=View.VISIBLE
                supportActionBar?.title = getString(R.string.result_not_publish_title)
                /*val Liveuserdb = FirebaseDatabase.getInstance().getReference("ActiveUsers")
                val prefs = RegisterActivity.PreferenceHelper.customPreference(this, CUSTOM_PREF_NAME)
                val map: HashMap<String, Any?> = HashMap()
                map["phone"] = prefs.userPhone
                map["activity"] = getString(R.string.result_not_publish_title)
                Liveuserdb.child(prefs.userToken!!).setValue(map)*/
            }
        }
    }
    private fun loadLotteryNumberInfoUsingDateAndTimeSecondServer() {
        Coroutines.main {
            try {
                binding.spinKit.visibility=View.VISIBLE
                binding.resultRootLayout.visibility=View.GONE
                binding.waitingRootLayout.visibility=View.GONE
                val response=secondServerApi.getLotteryNumberListByDateSlot(resultDate,resultSlotId,
                    SharedPreUtils.getStringFromStorageWithoutSuspend(this,Constants.userIdKey,Constants.defaultUserId).toString())
                binding.spinKit.visibility=View.GONE
                if (response.isSuccessful && response.code()==200) {
                    if (response.body()!=null) {
                        if (response.body()?.status.equals("success")) {
                            list.clear()
                            list.addAll(response.body()?.data!!)
                            if (list.size>0) {
                                 //getLotteryClassVideo()
                                filteringLotteryNumber(list)
                                binding.resultRootLayout.visibility=View.VISIBLE
                                binding.waitingRootLayout.visibility=View.GONE
                                supportActionBar?.title = getString(R.string.view_details)

                                /*val rootArray = JSONArray(response.body().toString())
                                for (i in 0 until rootArray.length()) {
                                    val bannerimage = rootArray.getJSONObject(i).getString("viewCount")
                                    Toast.makeText(this,bannerimage,Toast.LENGTH_LONG).show()
                                }*/
                            } else {
                                binding.resultRootLayout.visibility=View.GONE
                                binding.waitingRootLayout.visibility=View.VISIBLE
                                supportActionBar?.title = getString(R.string.result_not_publish_title)
                                /*val Liveuserdb = FirebaseDatabase.getInstance().getReference("ActiveUsers")
                                val prefs = RegisterActivity.PreferenceHelper.customPreference(this, CUSTOM_PREF_NAME)
                                val map: HashMap<String, Any?> = HashMap()
                                map["phone"] = prefs.userPhone
                                map["activity"] = getString(R.string.result_not_publish_title)
                                Liveuserdb.child(prefs.userToken!!).setValue(map)*/
                            }
                        } else {
                            binding.spinKit.visibility=View.GONE
                            binding.resultRootLayout.visibility=View.GONE
                            binding.waitingRootLayout.visibility=View.VISIBLE
                            supportActionBar?.title = getString(R.string.result_not_publish_title)
                            //shortToast("${response.body()?.message}")
                            /*val Liveuserdb = FirebaseDatabase.getInstance().getReference("ActiveUsers")
                            val prefs = RegisterActivity.PreferenceHelper.customPreference(this, CUSTOM_PREF_NAME)
                            val map: HashMap<String, Any?> = HashMap()
                            map["phone"] = prefs.userPhone
                            map["activity"] = getString(R.string.result_not_publish_title)
                            Liveuserdb.child(prefs.userToken!!).setValue(map)*/
                        }
                    } else {
                        binding.spinKit.visibility=View.GONE
                        binding.resultRootLayout.visibility=View.GONE
                        binding.waitingRootLayout.visibility=View.VISIBLE
                        supportActionBar?.title = getString(R.string.result_not_publish_title)
                        shortToast("Sorry, Unknown error occurred.")
                        /*val Liveuserdb = FirebaseDatabase.getInstance().getReference("ActiveUsers")
                        val prefs = RegisterActivity.PreferenceHelper.customPreference(this, CUSTOM_PREF_NAME)
                        val map: HashMap<String, Any?> = HashMap()
                        map["phone"] = prefs.userPhone
                        map["activity"] = getString(R.string.result_not_publish_title)
                        Liveuserdb.child(prefs.userToken!!).setValue(map)*/
                    }
                } else {
                    binding.spinKit.visibility=View.GONE
                    binding.resultRootLayout.visibility=View.GONE
                    binding.waitingRootLayout.visibility=View.VISIBLE
                    supportActionBar?.title = getString(R.string.result_not_publish_title)
                    //shortToast("failed for:- ${response.errorBody()?.string()}")
                    /*val Liveuserdb = FirebaseDatabase.getInstance().getReference("ActiveUsers")
                    val prefs = RegisterActivity.PreferenceHelper.customPreference(this, CUSTOM_PREF_NAME)
                    val map: HashMap<String, Any?> = HashMap()
                    map["phone"] = prefs.userPhone
                    map["activity"] = getString(R.string.result_not_publish_title)
                    Liveuserdb.child(prefs.userToken!!).setValue(map)*/
                }
            } catch (e: Exception) {
                binding.resultRootLayout.visibility=View.GONE
                binding.waitingRootLayout.visibility=View.VISIBLE
                supportActionBar?.title = getString(R.string.result_not_publish_title)
                /*val Liveuserdb = FirebaseDatabase.getInstance().getReference("ActiveUsers")
                val prefs = RegisterActivity.PreferenceHelper.customPreference(this, CUSTOM_PREF_NAME)
                val map: HashMap<String, Any?> = HashMap()
                map["phone"] = prefs.userPhone
                map["activity"] = getString(R.string.result_not_publish_title)
                Liveuserdb.child(prefs.userToken!!).setValue(map)*/
            }
        }
    }



    private fun filteringLotteryNumber(list: MutableList<LotteryNumberModel>) {
//        val firstList: MutableList<LotteryNumberModel> = arrayListOf()
        val secondList: MutableList<LotteryNumberModel> = arrayListOf()
        val thirdList: MutableList<LotteryNumberModel> = arrayListOf()
        val fourthList: MutableList<LotteryNumberModel> = arrayListOf()
        val fifthList: MutableList<LotteryNumberModel> = arrayListOf()

        binding.leftTimeTextView.text = resultTime
        binding.rightTimeTextView.text = resultTime

        ResultViewer()
        for ( item in list) {
            if (item.winType.equals(Constants.winTypeFirst)) {
//                firstList.add(item)
                binding.serialFirstTextView.text=item.lotterySerialNumber
                binding.firstPrizeLotteryNumberTextView.text=item.lotteryNumber
                binding.remainingAllSerialTextView.text="\u20B9 1000/- ${item.lotteryNumber} (REMAINING ALL SERIALS)"
                if(!item.name!!.isNullOrEmpty()){
                    binding.lotteryName.visibility = View.VISIBLE
                    binding.lotteryName.text = item.name
                }
            } else if (item.winType.equals(Constants.winTypeSecond)) {
                secondList.add(item)
            } else if (item.winType.equals(Constants.winTypeThird)) {
                thirdList.add(item)
            } else if (item.winType.equals(Constants.winTypeFourth)) {
                fourthList.add(item)
            } else if (item.winType.equals(Constants.winTypeFifth)) {
                fifthList.add(item)
            }
        }
        binding.showCount.visibility = View.VISIBLE
        finalList.clear()
//        finalList.add(LotteryResultRecyclerModel(Constants.winTypeFirst,firstList))
        finalList.add(LotteryResultRecyclerModel(Constants.winTypeSecond,secondList))
        finalList.add(LotteryResultRecyclerModel(Constants.winTypeThird,thirdList))
        finalList.add(LotteryResultRecyclerModel(Constants.winTypeFourth,fourthList))
        finalList.add(LotteryResultRecyclerModel(Constants.winTypeFifth,fifthList))

        adapter.notifyDataSetChanged()
    }

    override fun attachBaseContext(newBase: Context?) {
        if (newBase!=null) {
            super.attachBaseContext(CommonMethod.updateLanguage(newBase))
        } else {
            super.attachBaseContext(newBase)
        }
    }


    private fun ResultViewer(){
        val call: Call<JsonElement> = apiInterface!!.getResultViewer(resultDate,resultTime,SharedPreUtils.getStringFromStorageWithoutSuspend(this,Constants.userIdKey,Constants.defaultUserId).toString())
        call.enqueue(object : Callback<JsonElement> {
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                if (response.isSuccessful && response.code() == 200) {
                    try {
                        val rootArray = JSONArray(response.body().toString())
                        for (i in 0 until rootArray.length()) {


                            val viewerCount = rootArray.getJSONObject(i).getString("viewer_count")
                            binding.resultCountView.text = viewerCount

                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@LotteryResultInfoActivity,
                            "error found:- " + e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@LotteryResultInfoActivity,
                        "Unknown error occurred.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                Toast.makeText(
                    this@LotteryResultInfoActivity,
                    "error:- " + t.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun initTutorialRecyScrollListener() {
        binding.recyLotteryClass.addOnScrollListener(
            object: RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    visibleItemCount = layoutManager.getChildCount()
                    totalItemCount = layoutManager.getItemCount()
                    pastVisibleItem = layoutManager.findFirstVisibleItemPosition()

                    if (dy > 0) {
                        Log.d("Pagination", "Scrolled")
                        if (!isLoading) {
                            if (PAGE <= TOTAL_PAGE) {
                                Log.d("Pagination", "Total Page $TOTAL_PAGE")
                                Log.d("Pagination", "Page $PAGE")
                                if (visibleItemCount + pastVisibleItem >= totalItemCount) {
                                    //postListProgress.setVisibility(View.VISIBLE)
                                    isLoading = true
                                    Log.v("...", "Last Item Wow !")
                                    //Do pagination.. i.e. fetch new data
                                    PAGE++
                                    if (PAGE <= TOTAL_PAGE) {
                                        choseCallingFunction(page = PAGE)
                                    } else {
                                        isLoading = false
                                        //postListProgress.setVisibility(View.GONE)
                                        if(videoCallingType == LmpClassVideoActivity.VideoCallingType.FACEBOOK_VIDEO){
                                            videoCallingType = LmpClassVideoActivity.VideoCallingType.LMPCLASS_VIDEO
                                            PAGE = 1
                                            TOTAL_PAGE = 0
                                            choseCallingFunction(PAGE)
                                        }
                                    }
                                }
                            } else {
                                if(videoCallingType == LmpClassVideoActivity.VideoCallingType.FACEBOOK_VIDEO){
                                    videoCallingType = LmpClassVideoActivity.VideoCallingType.LMPCLASS_VIDEO
                                    PAGE = 1
                                    TOTAL_PAGE = 0
                                    choseCallingFunction(PAGE)
                                }else{
                                    Log.d("Pagination", "End of page")
                                }
                                //postListProgress.setVisibility(View.GONE);

                            }
                        } else {
                            Log.d("Pagination", "Loading")
                        }
                    }else{

                    }
                }
            })
    }


    fun choseCallingFunction(page: Int){
        if(videoCallingType == LmpClassVideoActivity.VideoCallingType.FACEBOOK_VIDEO){
            getFacebookVideos(page)
        }else if(videoCallingType == LmpClassVideoActivity.VideoCallingType.LMPCLASS_VIDEO){
            getVideos(page)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getFacebookVideos(page: Int) {
        Coroutines.main {
            try {
                //val response=viewModel.getVideo("")
                val response=(application as MyApplication).myApi.getFacebookVideoListWithCountPaging(page)
                if (response.isSuccessful && response.code()==200) {
                    if (response.body()!=null) {
                        isLoading = false

                        if (response.body()?.status.equals("success",true)) {
                            val list = response.body()?.data!!
                            lmpVideo.addAll(list)
                            lmpClassVideoAdapter.notifyDataSetChanged()

                            if(list.size<10){
                                isLoading = false

                                videoCallingType = LmpClassVideoActivity.VideoCallingType.LMPCLASS_VIDEO
                                PAGE = 1
                                TOTAL_PAGE = 0
                                choseCallingFunction(PAGE)
                            }

                        } else {
                            isLoading = false

                            videoCallingType = LmpClassVideoActivity.VideoCallingType.LMPCLASS_VIDEO
                            PAGE = 1
                            TOTAL_PAGE = 0
                            choseCallingFunction(PAGE)

                        }
                    }else{
                        isLoading = false
                    }
                } else {
                    isLoading = false
                }
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }


    private fun getVideos(page: Int) {
        isLoading = true
        (application as MyApplication).myApi
            .getLmpClassVideo(page)
            .enqueue( object : Callback<lmpVideoResponse> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<lmpVideoResponse>, response: Response<lmpVideoResponse>) {
                    isLoading = false
                    if(response.isSuccessful && response.body()!=null){
                        val lmpVideoResponse = response.body()!!
                        if(!lmpVideoResponse.error){
                            TOTAL_PAGE = lmpVideoResponse.totalPages
                            lmpVideoResponse.lmpVideos?.let {
                                Log.d("Dataaa", Gson().toJson(it))


                                lmpVideo.addAll(it)
                                lmpClassVideoAdapter.notifyDataSetChanged()

                            }
                        }
                    }
                }

                override fun onFailure(call: Call<lmpVideoResponse>, t: Throwable) {
                    isLoading = false
                }

            }
            )
    }

    private fun getYtVideos() {
        Coroutines.main {
            try {
                binding.spinKit.visibility= View.VISIBLE
                val response=myApi.getVideoListInResultInfo("")
                if (response.isSuccessful && response.code()==200) {
                    binding.spinKit.visibility= View.GONE
                    if (response.body()!=null) {
                        if (response.body()?.status.equals("success",true)) {
                            ytList.addAll(response.body()?.data!!)
                            videoLayoutManager= LinearLayoutManager(this)
                            videoAdapter= VideoTutorialAdapter(this,ytList)

                            binding.recyLotteryClass.layoutManager= videoLayoutManager
                            binding.recyLotteryClass.adapter=videoAdapter

                            binding.recyLotteryClass.isNestedScrollingEnabled = false
                        }
                    }
                } else {
                    binding.spinKit.visibility= View.GONE

                }
            } catch (e: Exception) {
                binding.spinKit.visibility= View.GONE
            }
        }
    }

    private fun getVideoType(){
        (application as MyApplication).myApi
            .getVideoType()
            .enqueue(object: Callback<VideoTypeResposne> {
                override fun onResponse(call: Call<VideoTypeResposne>, response: Response<VideoTypeResposne>) {

                    if(response.isSuccessful && response.body()!=null){
                        Log.d("Response", response.body()!!.error.toString())
                        if(!response.body()!!.error!!){
                            layoutManager= LinearLayoutManager(this@LotteryResultInfoActivity)
                            binding.recyLotteryClass.layoutManager=layoutManager

                            Log.d("Response", response.body()!!.msg!!)
                            if(response.body()!!.type == 1){
                                Log.d("Response", response.body()!!.type.toString())
                                //media player
//                                mediaPlayerAdapter = MediaPlayerTutorialAdapter(this@LotteryResultInfoActivity, videoList)
//                                binding.recyLotteryClass.adapter = mediaPlayerAdapter
//                                binding.spinKit.visibility= View.VISIBLE
                                getVideos(1)
                            }else{
                                //yt video
                                ytAdapter= VideoTutorialAdapter(this@LotteryResultInfoActivity,ytList)
                                binding.recyLotteryClass.adapter=ytAdapter
                                getYtVideos()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<VideoTypeResposne>, t: Throwable) {
                    t.printStackTrace()
                }

            })
    }

    override fun onBackPressed() {
        //myInterstitialAd.onBackPress()
        //fanInterstitialAd.onBackPress()

        adSource.onBackPress()
    }

}