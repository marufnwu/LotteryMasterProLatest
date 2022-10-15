package com.skithub.resultdear.ui.middle_part

import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.skithub.resultdear.R
import com.skithub.resultdear.adapter.CustomerCareNumAdapter
import com.skithub.resultdear.adapter.DuplicateLotteryNumberRecyclerAdapter
import com.skithub.resultdear.database.network.MyApi
import com.skithub.resultdear.databinding.ActivityLastDigitFirstPrizeBinding
import com.skithub.resultdear.databinding.ActivityMiddleSerialBinding
import com.skithub.resultdear.databinding.ConnectionCheckDialogBinding
import com.skithub.resultdear.model.LotteryNumberModel
import com.skithub.resultdear.model.UnSubscribeData
import com.skithub.resultdear.ui.MyApplication
import com.skithub.resultdear.ui.last_digit_first_prize.LastDigitFirstPrizeActivity
import com.skithub.resultdear.utils.CommonMethod
import com.skithub.resultdear.utils.Constants
import com.skithub.resultdear.utils.Coroutines
import com.skithub.resultdear.utils.MyExtensions.shortToast
import com.skithub.resultdear.utils.SharedPreUtils

class MiddleSerialActivity : AppCompatActivity() {

    private enum class PLAN{
        SUBSCRIBE,
        UNSUBSCRIBE
    }

    private lateinit var myApi: MyApi
    private lateinit var binding: ActivityMiddleSerialBinding
    private lateinit var connectionDialogBinding: ConnectionCheckDialogBinding
    private lateinit var connectivityManager: ConnectivityManager
    private var connectionAlertDialog: AlertDialog?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMiddleSerialBinding.inflate(layoutInflater)

        setContentView(binding.root)
        myApi = (application as MyApplication).myApi
        supportActionBar?.title = getString(R.string.middle_part_plays_more)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        connectivityManager=getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        if (CommonMethod.haveInternet(connectivityManager)) {

            loadPage()

        }else{
            noInternetDialog(getString(R.string.no_internet),getString(R.string.no_internet_message))
        }

    }
    private fun loadPage() {

        Coroutines.main {
            var response = (application as MyApplication).myApi.middlePartPage(
                SharedPreUtils.getStringFromStorageWithoutSuspend(
                    this,
                    Constants.userIdKey,
                    Constants.defaultUserId
                )!!
            )

            if(response.isSuccessful && response.body()!=null){

                var middlePartPage = response.body()!!

                if(middlePartPage.license!!){
                    //already subscribe
                    setPage(PLAN.SUBSCRIBE)

                    if(middlePartPage.subscribeData!=null){
                        var nums = middlePartPage.subscribeData

                        setMiddleSerial(nums)
                    }
                }else{
                    //not subscribe
                    setPage(PLAN.UNSUBSCRIBE)

                    buildUnSubscribePage(middlePartPage.unSubscribeData)
                }
            }else{
                shortToast(response.message())
            }
        }


    }

    private fun setMiddleSerial(nums: MutableList<String>?) {

        val list: MutableList<LotteryNumberModel> = mutableListOf()

        nums?.let {
            for (num in it){
                list.add(LotteryNumberModel(null, num, null, null, null, null, null, null, null))
            }
        }

        val adapter= DuplicateLotteryNumberRecyclerAdapter(this,list)
        val layoutManager= LinearLayoutManager(this)

        binding.recyclerView.layoutManager=layoutManager
        binding.recyclerView.adapter=adapter

    }

    private fun buildUnSubscribePage(unSubscribeData: UnSubscribeData?) {
        unSubscribeData?.let {

            if(!unSubscribeData.banner?.error!!){
                CommonMethod.loadBannerWithClick(this, unSubscribeData.banner!!, binding.ivBeforeSubscription)
                binding.imgContainer.visibility =View.VISIBLE
                binding.rippleBg.startRippleAnimation()
            }

            it.contact?.let { contact->
                if (!contact.error!!){
                    binding.recyContact.visibility = View.VISIBLE
                    if(contact.numberList?.size!!>0){
                        binding.recyContact.layoutManager = LinearLayoutManager(this)
                        binding.recyContact.setHasFixedSize(true)
                        val adapter =  CustomerCareNumAdapter(this, contact.numberList!!.toMutableList())
                        binding.recyContact.adapter = adapter
                    }
                }
            }


        }
    }
    private fun setPage(plan : PLAN){
        if(plan== PLAN.SUBSCRIBE){
            binding.laySubscribe.visibility = View.VISIBLE
            binding.layUnSubscribe.visibility = View.GONE
        }else{
            binding.laySubscribe.visibility = View.GONE
            binding.layUnSubscribe.visibility = View.VISIBLE
        }
    }


    private fun noInternetDialog(til: String, msg: String) {
        connectionDialogBinding= ConnectionCheckDialogBinding.inflate(layoutInflater)
        connectionDialogBinding.connectionTitle.text = til
        connectionDialogBinding.connectionMessage.text = msg
        connectionDialogBinding.tryAgainBtn.setOnClickListener {
            if (CommonMethod.haveInternet(connectivityManager)) {

                connectionAlertDialog.let {
                    it?.dismiss()
                }
            }
        }
        val builder= AlertDialog.Builder(this)
            .setCancelable(false)
            .setView(connectionDialogBinding.root)
        connectionAlertDialog=builder.create()
        if (connectionAlertDialog?.window!=null) {
            connectionAlertDialog?.window!!.attributes.windowAnimations= R.style.DialogTheme
        }
        if (!isFinishing) {
            connectionAlertDialog?.show()
        }

    }

}