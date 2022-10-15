package com.skithub.resultdear.ui.middle_part

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.skithub.resultdear.R
import com.skithub.resultdear.adapter.DuplicateLotteryNumberRecyclerAdapter
import com.skithub.resultdear.databinding.ActivityMiddlePartBinding
import com.skithub.resultdear.model.LotteryNumberModel
import com.skithub.resultdear.model.MiddlePart
import com.skithub.resultdear.ui.MyApplication
import com.skithub.resultdear.utils.Constants
import com.skithub.resultdear.utils.Coroutines
import com.skithub.resultdear.utils.LoadingDialog
import com.skithub.resultdear.utils.MyExtensions.shortToast
import com.skithub.resultdear.utils.SharedPreUtils

class MiddlePartActivity : AppCompatActivity() {
    var middle: String? = null
    lateinit var adapter: DuplicateLotteryNumberRecyclerAdapter
    lateinit var binding : ActivityMiddlePartBinding
    lateinit var loadingDialog: LoadingDialog
    var allList : MutableList<LotteryNumberModel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMiddlePartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = DuplicateLotteryNumberRecyclerAdapter(this, allList)
        val layoutManager = LinearLayoutManager(this)

        binding.recyclerView.layoutManager=layoutManager
        binding.recyclerView.adapter= adapter

        loadingDialog = LoadingDialog(this)
        middle = intent?.getStringExtra(Constants.lotteryNumberKey)


        supportActionBar?.title = getString(R.string.middle_part_plays_more)+" "+middle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)





        if(middle!=null){
            adapter.setMiddle(middle!!)
            loadPage(middle!!)
        }

    }


    private fun loadPage(middle : String) {
        loadingDialog.show()
        Coroutines.main {
            val response = (application as MyApplication).myApi.getPatByMiddle(
                SharedPreUtils.getStringFromStorageWithoutSuspend(
                    this,
                    Constants.userIdKey,
                    Constants.defaultUserId
                )!!,
                middle
            )

            loadingDialog.hide()

            if(response.isSuccessful && response.body()!=null){

                val middlePartResponse = response.body()!!

                if(!middlePartResponse.error){
                    setData(middlePartResponse.lastPArt)
                }else{
                    shortToast(middlePartResponse.msg)
                }
            }else{
                shortToast(response.message())
            }
        }


    }

    private fun setData(lastPArts: MutableList<MiddlePart>) {
        val list: MutableList<LotteryNumberModel> = mutableListOf()

        lastPArts.let {
            for (num in it){

                var partNo = ""
                when(num.range!!.trim()){
                    "0-19"->partNo = "1"
                    "20-39"->partNo = "2"
                    "40-59"->partNo = "3"
                    "60-79"->partNo = "4"
                    "80-99"->partNo = "5"
                }

                val rangeWithCount = "${partNo},${num.range},${num.count}"
                list.add(LotteryNumberModel(null, rangeWithCount, null, null, null, null, null, null, null))
            }
        }

        allList.addAll(list)
        adapter.notifyDataSetChanged()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putString(Constants.lotteryNumberKey, middle)

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        middle = savedInstanceState.getString(Constants.lotteryNumberKey)
    }

}