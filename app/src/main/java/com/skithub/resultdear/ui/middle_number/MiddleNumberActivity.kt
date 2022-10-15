package com.skithub.resultdear.ui.middle_number

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.skithub.resultdear.R
import com.skithub.resultdear.adapter.DuplicateLotteryNumberRecyclerAdapter
import com.skithub.resultdear.databinding.ActivityMiddleNumberBinding
import com.skithub.resultdear.model.LotteryNumberModel
import com.skithub.resultdear.ui.MyApplication
import com.skithub.resultdear.utils.MyExtensions.shortToast

import android.content.pm.PackageManager
import com.google.gson.JsonElement
import com.skithub.resultdear.database.network.ApiInterface
import com.skithub.resultdear.database.network.RetrofitClient
import com.skithub.resultdear.databinding.ConnectionCheckDialogBinding
import com.skithub.resultdear.utils.*
import com.skithub.resultdear.utils.admob.MyInterstitialAd
import com.skyfishjy.library.RippleBackground
import org.json.JSONArray
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MiddleNumberActivity : AppCompatActivity() {
    lateinit var myInterstitialAd: MyInterstitialAd
    private lateinit var binding: ActivityMiddleNumberBinding
    private lateinit var viewModel: MiddleNumberViewModel
    private var list: MutableList<LotteryNumberModel> = arrayListOf()
    private lateinit var adapter: DuplicateLotteryNumberRecyclerAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private var page_number: Int=1
    private var item_count: Int=30
    private lateinit var connectivityManager: ConnectivityManager
    //val CUSTOM_PREF_NAME = "User_data_extra"
    private var connectionAlertDialog: AlertDialog?=null
    private lateinit var connectionDialogBinding: ConnectionCheckDialogBinding
    private var license_check: String? = null
    //Toast.makeText(this@MiddleNumberActivity, android_id, Toast.LENGTH_SHORT).show()
    private var apiInterface: ApiInterface? = null
    val CUSTOM_PREF_NAME = "User_data_extra"

    lateinit var adSource: AdSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMiddleNumberBinding.inflate(layoutInflater)
        val factory= MiddleNumberViewModelFactory((application as MyApplication).myApi)
        viewModel= ViewModelProvider(this,factory).get(MiddleNumberViewModel::class.java)
        setContentView(binding.root)
        CommonMethod.keepScreenOn(this)

        val days = intent?.getIntExtra("days", 0)!!
        if(days<=0){
            supportActionBar?.title = getString(R.string.middle_number)
        }else{
            supportActionBar?.title = getString(R.string.middle_number_plays_more_days)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //myInterstitialAd = MyInterstitialAd(this)
        adSource = AdSource(this)

        apiInterface = RetrofitClient.getApiClient().create(ApiInterface::class.java)

        license_check = intent.getStringExtra("license_position").toString()

        initAll()

        if (CommonMethod.haveInternet(connectivityManager)) {

                binding.recyclerView.visibility = View.VISIBLE
                binding.standerdLayout.visibility = View.GONE
                setupRecyclerView()
            if(days<=0){
                loadDuplicateLotteryNumber()
            }else{
                loadDuplicateLotteryNumber(10)
            }

        }else{
            noInternetDialog(getString(R.string.no_internet),getString(R.string.no_internet_message))
        }

    }

    private fun getPremiumStatus() {
        Coroutines.main {
            try {
                val response=viewModel.getPaidForContact("2",
                    SharedPreUtils.getStringFromStorageWithoutSuspend(this,Constants.userIdKey,Constants.defaultUserId).toString())
                if (response.isSuccessful && response.code()==200) {
                    if (response.body()!=null) {
                        binding.spinKit.visibility= View.GONE
                        binding.standerdLayout.visibility = View.VISIBLE

                        binding.pnOne.text = response.body()?.phone_one
                        binding.pnTwo.text = response.body()?.phone_two
                        binding.pnThree.text = response.body()?.phone_three
                        val rippleBackground = findViewById<View>(R.id.content) as RippleBackground
                        rippleBackground.startRippleAnimation()
                        Glide.with(this@MiddleNumberActivity).load(response.body()?.video_thumbail).placeholder(R.drawable.loading_placeholder).fitCenter().into(binding.ytthumbail)
                        binding.ytthumbail.setOnClickListener {
                            val webIntent: Intent= Intent(Intent.ACTION_VIEW,Uri.parse(response.body()?.video_link))
                            startActivity(Intent.createChooser(webIntent,"Choose one:"))
                        }
                        binding.PhoneOne.setOnClickListener {
                            val dialIntent = Intent(Intent.ACTION_DIAL)
                            dialIntent.data = Uri.parse("tel:" + response.body()?.phone_one)
                            startActivity(dialIntent)
                        }
                        binding.PhoneTwo.setOnClickListener {
                            val dialIntent = Intent(Intent.ACTION_DIAL)
                            dialIntent.data = Uri.parse("tel:" + response.body()?.phone_two)
                            startActivity(dialIntent)
                        }
                        binding.PhoneThree.setOnClickListener {
                            val dialIntent = Intent(Intent.ACTION_DIAL)
                            dialIntent.data = Uri.parse("tel:" + response.body()?.phone_three)
                            startActivity(dialIntent)
                        }

                        binding.whatsAppBtn.setOnClickListener {
                            try {
                                val mobile = response.body()?.whats_app
                                val msg = ""
                                startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://api.whatsapp.com/send?phone=$mobile&text=$msg")
                                    )
                                )
                            } catch (e: java.lang.Exception) {
                                Toast.makeText(this@MiddleNumberActivity, "WhatsApp not Installed", Toast.LENGTH_SHORT).show()
                            }

                        }



                        if (response.body()?.banner_image!!.length > 6){
                            binding.adUpArrowBtn.setImageResource(R.drawable.ic_arrow_down_icon)
                            Glide.with(this@MiddleNumberActivity).load(response.body()?.banner_image).placeholder(R.drawable.loading_placeholder).into(binding.imageBanner)
                            binding.imageBanner.setOnClickListener {
                                val webIntent: Intent= Intent(Intent.ACTION_VIEW,Uri.parse(response.body()?.target_link))
                                startActivity(Intent.createChooser(webIntent,"Choose one:"))
                            }
                            /*val hide: Animation =
                                AnimationUtils.loadAnimation(this@MainActivity, R.anim.bottom_top)
                            binding.adLayout.startAnimation(hide)*/
                            binding.adLayout.visibility = View.VISIBLE
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



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)


        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.option_share -> {
                CommonMethod.shareAppLink(this)
            }
            R.id.option_moreApps -> {
                CommonMethod.openConsoleLink(this,Constants.consoleId)
            }
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
                if (!license_check.equals("1")){
                    getPremiumStatus()
                    binding.recyclerView.visibility = View.GONE
                    binding.standerdLayout.visibility = View.VISIBLE
                }else{
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.standerdLayout.visibility = View.GONE
                    setupRecyclerView()
                    loadDuplicateLotteryNumber()
                }
                connectionAlertDialog?.dismiss()
            }
        }
        val builder=AlertDialog.Builder(this@MiddleNumberActivity)
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
        binding.spinKit.visibility= View.GONE
    }

    private fun setupRecyclerView() {
        adapter= DuplicateLotteryNumberRecyclerAdapter(this,list)
        layoutManager= LinearLayoutManager(this)
        binding.recyclerView.layoutManager=layoutManager
        binding.recyclerView.adapter=adapter
    }

    private fun loadDuplicateLotteryNumber() {

        val tryAgain = TryAgainAlert(this)
            .create()
            .setTryAgainButtonText(null, object: TryAgainAlert.OnTryAgainClick {
                override fun onClick() {
                    loadDuplicateLotteryNumber()
                }

            })
        Coroutines.main {
            try {
                binding.spinKit.visibility= View.VISIBLE
                val response=viewModel.getMiddlePlaysMoreNumberList(page_number.toString(),item_count.toString())
                if (response.isSuccessful && response.code()==200) {
                    binding.spinKit.visibility= View.GONE
                    if (response.body()!=null) {
                        if (response.body()?.status.equals("success",true)) {
                            val temporaryList=response.body()?.data!!
                            list.addAll(temporaryList)
                            adapter.notifyDataSetChanged()
                            generateFinalList(temporaryList)
                            /*val Liveuserdb = FirebaseDatabase.getInstance().getReference("ActiveUsers")
                            val prefs = RegisterActivity.PreferenceHelper.customPreference(this, CUSTOM_PREF_NAME)
                            val map: HashMap<String, Any?> = HashMap()
                            map["phone"] = prefs.userPhone
                            map["activity"] = getString(R.string.middle_number)
                            Liveuserdb.child(prefs.userToken!!).setValue(map)*/
                        } else {
                            shortToast("message:- ${response.body()?.message}")
                        }
                    }
                } else {
                    binding.spinKit.visibility= View.GONE
                    tryAgain.show()
                }
            } catch (e: Exception) {
                binding.spinKit.visibility= View.GONE
                tryAgain.show()
            }
        }
    }
    private fun loadDuplicateLotteryNumber(days:Int) {

        val tryAgain = TryAgainAlert(this)
            .create()
            .setTryAgainButtonText(null, object: TryAgainAlert.OnTryAgainClick {
                override fun onClick() {
                    loadDuplicateLotteryNumber()
                }

            })
        Coroutines.main {
            try {
                binding.spinKit.visibility= View.VISIBLE
                val response=(application as MyApplication).myApi.getMiddlePlaysMoreNumberListInDays(page_number.toString(),item_count.toString(), days)
                if (response.isSuccessful && response.code()==200) {
                    binding.spinKit.visibility= View.GONE
                    if (response.body()!=null) {
                        if (response.body()?.status.equals("success",true)) {
                            val temporaryList=response.body()?.data!!
                            list.addAll(temporaryList)
                            adapter.notifyDataSetChanged()
                            generateFinalList(temporaryList)
                        } else {
                            shortToast("message:- ${response.body()?.message}")
                        }
                    }
                } else {
                    binding.spinKit.visibility= View.GONE
                    tryAgain.show()
                }
            } catch (e: Exception) {
                binding.spinKit.visibility= View.GONE
                tryAgain.show()
            }
        }
    }

    private fun generateFinalList(temporaryList: MutableList<LotteryNumberModel>) {
        list.clear()
//        for (i in 0 until temporaryList.size) {
//            if (list.isNullOrEmpty()) {
//                val lotteryNumberModel=LotteryNumberModel(temporaryList[i].id,temporaryList[i].lotteryNumber!!.substring(0,temporaryList[i].lotteryNumber!!.length-2),temporaryList[i].lotterySerialNumber,temporaryList[i].winType,temporaryList[i].winDate,temporaryList[i].winTime,temporaryList[i].winDayName)
//                list.add(lotteryNumberModel)
//                Log.d(Constants.TAG,"check:- ${CommonMethod.subStringFromString(temporaryList[i].lotterySerialNumber!!,2)}")
//            }
//            try {
//                for (j in 0 until list.size) {
//                    if (!CommonMethod.subStringFromString(temporaryList[i].lotteryNumber!!,2).equals(list[j].lotteryNumber)) {
//                        val lotteryNumberModel=LotteryNumberModel(temporaryList[i].id,temporaryList[i].lotteryNumber!!.substring(0,temporaryList[i].lotteryNumber!!.length-2),temporaryList[i].lotterySerialNumber,temporaryList[i].winType,temporaryList[i].winDate,temporaryList[i].winTime,temporaryList[i].winDayName)
//                        list.add(lotteryNumberModel)
//                        Log.d(Constants.TAG,"condition true")
//                        break
//                    } else {
//                        Log.d(Constants.TAG,"condition false")
//                    }
//                }
//            } catch (e: Exception) {
//                Log.d(Constants.TAG,"generating error:- ${e.message}")
//            }
//        }


        temporaryList.forEach parentLoop@{


//CommonMethod.subStringFromString(it.lotteryNumber!!,2)
                if (!it.lotteryNumber.equals("74065") && !it.lotteryNumber.equals("53586") && !it.lotteryNumber.equals("44511")
                    && !it.lotteryNumber.equals("97531") && !it.lotteryNumber.equals("50675") && !it.lotteryNumber.equals("86937")
                    && !it.lotteryNumber.equals("48587") && !it.lotteryNumber.equals("80864")) {

                    if (it.lotteryNumber!!.length > 4) {
                        val lotteryNumberModel = LotteryNumberModel(
                            it.id,
                            it.lotteryNumber!!.substring(1, it.lotteryNumber!!.length - 2),
                            it.lotterySerialNumber,
                            it.winType,
                            it.winDate,
                            it.winTime,
                            it.winDayName,
                            it.SlotId,
                            it.name
                        )
                        list.add(lotteryNumberModel)
                    } else {
                        val lotteryNumberModel = LotteryNumberModel(
                            it.id,
                            it.lotteryNumber!!.substring(0, it.lotteryNumber!!.length - 2),
                            it.lotterySerialNumber,
                            it.winType,
                            it.winDate,
                            it.winTime,
                            it.winDayName,
                            it.SlotId,
                            it.name
                        )
                        list.add(lotteryNumberModel)
                    }


                }

                Log.d(Constants.TAG,"check:- ${CommonMethod.subStringFromString(it.lotteryNumber!!,2)}")

        }
        adapter.notifyDataSetChanged()
        Log.d(Constants.TAG,"temporary list size:- ${temporaryList.size} ")
        Log.d(Constants.TAG,"final list size:- ${list.size} ")
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun attachBaseContext(newBase: Context?) {
        if (newBase!=null) {
            super.attachBaseContext(CommonMethod.updateLanguage(newBase))
        } else {
            super.attachBaseContext(newBase)
        }
    }

    override fun onBackPressed() {
        //myInterstitialAd.onBackPress()
        adSource.onBackPress()
    }



}