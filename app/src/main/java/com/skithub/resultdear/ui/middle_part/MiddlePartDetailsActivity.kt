package com.skithub.resultdear.ui.middle_part

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.skithub.resultdear.R
import com.skithub.resultdear.adapter.DuplicateLotteryNumberRecyclerAdapter
import com.skithub.resultdear.adapter.LotteryNumberRecyclerAdapter
import com.skithub.resultdear.databinding.ActivityMiddleDetailsBinding
import com.skithub.resultdear.databinding.ActivityMiddlePartDetailsBinding
import com.skithub.resultdear.model.LotteryNumberModel
import com.skithub.resultdear.ui.MyApplication
import com.skithub.resultdear.utils.Constants
import com.skithub.resultdear.utils.Coroutines
import com.skithub.resultdear.utils.LoadingDialog
import com.skithub.resultdear.utils.MyExtensions.shortToast
import com.skithub.resultdear.utils.SharedPreUtils

class MiddlePartDetailsActivity : AppCompatActivity() {
    lateinit var binding : ActivityMiddlePartDetailsBinding
    lateinit var loadingDialog: LoadingDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMiddlePartDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog = LoadingDialog(this)
        val middle = intent?.getStringExtra("middle")
        val range = intent?.getStringExtra("range")



        if(middle!=null && range!=null){
            supportActionBar?.title = getString(R.string.middle_part_plays_more)+" "+middle
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            getNumbers(middle, range)
        }
    }

    private fun getNumbers(middle: String, range: String) {
        loadingDialog.show()
        Coroutines.main {
            try {
                binding.spinKit.visibility= View.VISIBLE
                val response=(application as MyApplication).myApi.getMiddlePartDeatils(
                    SharedPreUtils.getStringFromStorageWithoutSuspend(
                        this,
                        Constants.userIdKey,
                        Constants.defaultUserId
                    )!!,
                    middle,
                    range
                )
                loadingDialog.hide()
                if (response.isSuccessful && response.code()==200) {
                    binding.spinKit.visibility= View.GONE
                    if (response.body()!=null) {
                        if (response.body()?.status.equals("success",true)) {
                            setupRecycler(response.body()?.data!!)
                        } else {
                            shortToast("message:- ${response.body()?.message}")
                            Log.d(Constants.TAG,"message:- ${response.body()?.message}")
                        }
                    }else{
                        shortToast("null")
                    }
                } else {
                    shortToast(response.message())
                    binding.spinKit.visibility= View.GONE
                }
            } catch (e: Exception) {
                shortToast(e.message!!)
                binding.spinKit.visibility= View.GONE
            }
        }
    }

    private fun setupRecycler(data: MutableList<LotteryNumberModel>) {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        val adapter = LotteryNumberRecyclerAdapter(this, data)
        binding.recyclerView.adapter =adapter
        adapter.notifyDataSetChanged()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}