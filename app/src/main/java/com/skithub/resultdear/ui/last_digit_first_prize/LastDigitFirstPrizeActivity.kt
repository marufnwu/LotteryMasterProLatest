package com.skithub.resultdear.ui.last_digit_first_prize

import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.skithub.resultdear.R
import com.skithub.resultdear.adapter.DuplicateLotteryNumberRecyclerAdapter
import com.skithub.resultdear.database.network.MyApi
import com.skithub.resultdear.databinding.ActivityLastDigitFirstPrizeBinding
import com.skithub.resultdear.databinding.ConnectionCheckDialogBinding
import com.skithub.resultdear.model.LotteryNumberModel
import com.skithub.resultdear.model.UnSubscribeData
import com.skithub.resultdear.ui.MyApplication
import com.skithub.resultdear.utils.CommonMethod
import com.skithub.resultdear.utils.Constants
import com.skithub.resultdear.utils.Coroutines
import com.skithub.resultdear.utils.MyExtensions.shortToast
import com.skithub.resultdear.utils.SharedPreUtils

class LastDigitFirstPrizeActivity : AppCompatActivity() {

    private enum class PLAN{
        SUBSCRIBE,
        UNSUBSCRIBE
    }

    private lateinit var myApi: MyApi
    private lateinit var binding: ActivityLastDigitFirstPrizeBinding
    private lateinit var connectionDialogBinding: ConnectionCheckDialogBinding
    private lateinit var connectivityManager: ConnectivityManager
    private var connectionAlertDialog: AlertDialog?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLastDigitFirstPrizeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myApi = (application as MyApplication).myApi
        supportActionBar?.title = getString(R.string.firstDigitFirstPrize)
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
            var response = (application as MyApplication).myApi.firstPrizeLastDigitPage(
                SharedPreUtils.getStringFromStorageWithoutSuspend(
                    this,
                    Constants.userIdKey,
                    Constants.defaultUserId
                )!!
            )

            if(response.isSuccessful && response.body()!=null){

                var firstPrizeLastDigitPage = response.body()!!

                if(firstPrizeLastDigitPage.license!!){
                    //already subscribe
                    setPage(PLAN.SUBSCRIBE)

                    if(firstPrizeLastDigitPage.subscribeData!=null){
                        var lotteryNumber = firstPrizeLastDigitPage.subscribeData!!.firstPrizeLastDigit

                        setLastDigit(lotteryNumber)
                    }
                }else{
                    //not subscribe
                    setPage(PLAN.UNSUBSCRIBE)

                    buildUnSubscribePage(firstPrizeLastDigitPage.unSubscribeData)
                }
            }else{
                shortToast(response.message())
            }
        }


    }

    private fun buildUnSubscribePage(unSubscribeData: UnSubscribeData?) {

        if (unSubscribeData==null){
            shortToast("null")
        }

        unSubscribeData?.let {
            CommonMethod.loadBannerWithClick(this, unSubscribeData.banner!!, binding.ivBeforeSubscription)
            binding.rippleBg.startRippleAnimation()
        }
    }

    private fun setLastDigit(lotteryNumber: List<LotteryNumberModel>) {
        val adapter= DuplicateLotteryNumberRecyclerAdapter(this,lotteryNumber.toMutableList())
        val layoutManager= LinearLayoutManager(this)

        binding.recyclerView.layoutManager=layoutManager
        binding.recyclerView.adapter=adapter

    }


    private fun setPage(plan : PLAN){
        if(plan==PLAN.SUBSCRIBE){
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
            connectionAlertDialog?.window!!.attributes.windowAnimations=R.style.DialogTheme
        }
        if (!isFinishing) {
            connectionAlertDialog?.show()
        }

    }
}