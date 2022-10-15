package com.skithub.resultdear.ui.number_not_plays

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.skithub.resultdear.R
import com.skithub.resultdear.adapter.DuplicateLotteryNumberRecyclerAdapter
import com.skithub.resultdear.databinding.ActivityMiddleSerialBinding
import com.skithub.resultdear.model.LotteryNumberModel
import com.skithub.resultdear.ui.MyApplication
import com.skithub.resultdear.utils.AdSource
import com.skithub.resultdear.utils.CommonMethod
import com.skithub.resultdear.utils.Coroutines
import com.skithub.resultdear.utils.LoadingDialog
import com.skithub.resultdear.utils.MyExtensions.shortToast

class MiddleSerialActivity : AppCompatActivity() {
    lateinit var loadingDialog: LoadingDialog
    lateinit var binding : ActivityMiddleSerialBinding
    lateinit var adSource : AdSource
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMiddleSerialBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = getString(R.string.numNotplayed)

        adSource = AdSource(this)



        loadingDialog = LoadingDialog(this)
        binding.laySubscribe.visibility = View.VISIBLE
        binding.layUnSubscribe.visibility = View.VISIBLE
        loadBanner()
        setMiddleSerial()
    }


    private fun loadBanner() {
        Coroutines.main {
            CommonMethod.getBanner("NumNotPlayedActivity", binding.ivAfterSubscription, (application as MyApplication).myApi, this)

        }
    }

    private fun setMiddleSerial() {
        binding.recyclerView.visibility = View.VISIBLE

        val list: MutableList<LotteryNumberModel> = mutableListOf()

        for (n in 0..99){
            if(n<10){
                list.add(LotteryNumberModel(null, "0$n", null, null, null, null, null, null, null))
            }else{
                list.add(LotteryNumberModel(null, "$n", null, null, null, null, null, null, null))
            }
        }


        val adapter= DuplicateLotteryNumberRecyclerAdapter(this,list)
        val layoutManager= LinearLayoutManager(this)

        binding.recyclerView.layoutManager=layoutManager
        binding.recyclerView.adapter=adapter
        adapter.notifyDataSetChanged()

    }

    override fun onBackPressed() {
        //myInterstitialAd.onBackPress()
        //fanInterstitialAd.onBackPress()

        adSource.onBackPress()
    }

    override fun attachBaseContext(newBase: Context?) {
        if (newBase!=null) {
            super.attachBaseContext(CommonMethod.updateLanguage(newBase))
        } else {
            super.attachBaseContext(newBase)
        }
    }

}