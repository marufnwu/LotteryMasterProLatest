package com.skithub.resultdear.ui.last_digit_first_prize

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skithub.resultdear.R
import com.skithub.resultdear.adapter.LotteryNumberRecyclerAdapter
import com.skithub.resultdear.databinding.ActivityLastDigitFirstPrizeDetailsBinding
import com.skithub.resultdear.model.LotteryNumberModel
import com.skithub.resultdear.ui.MyApplication
import com.skithub.resultdear.utils.Constants
import com.skithub.resultdear.utils.Coroutines
import com.skithub.resultdear.utils.MyExtensions.shortToast
import com.skithub.resultdear.utils.SharedPreUtils

class LastDigitFirstPrizeDetailsActivity : AppCompatActivity() {

    lateinit var layoutManager: LinearLayoutManager
    var pastVisibleItem : Int =0
    var visibleItemCount = 0
    var totalItemCount=0
    var previousTotal = 0

    private var PAGE = 1
    private var PAGE_SIZE= 30
    private var TOTAL_PAGE = 0
    var isLoading = true
    var digit : String? = null

    lateinit var adapter: LotteryNumberRecyclerAdapter
    lateinit var binding : ActivityLastDigitFirstPrizeDetailsBinding
    var  list : MutableList<LotteryNumberModel> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLastDigitFirstPrizeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        digit = intent.getStringExtra(Constants.lotteryNumberKey)!!.takeLast(1)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "$digit in First Prize Last Digit"


        setupRecyclerview()
        getNumbers()

    }


    private fun setupRecyclerview() {
        layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)
        adapter = LotteryNumberRecyclerAdapter(this, list)
        binding.recyclerView.adapter = adapter

        binding.recyclerView.addOnScrollListener(
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
                                    PAGE++
                                    if (PAGE <= TOTAL_PAGE) {
                                        getNumbers()
                                    } else {
                                        isLoading = false
                                        //postListProgress.setVisibility(View.GONE)
                                    }
                                }
                            } else {

                                //postListProgress.setVisibility(View.GONE);
                                Log.d("Pagination", "End of page")
                            }
                        } else {
                            Log.d("Pagination", "Loading")
                        }
                    }
                }
            })

    }

    private fun getNumbers() {


        if(PAGE ==1){
            binding.spinKit.visibility = View.VISIBLE
        }

        if(digit==null){
            shortToast("Something went wrong!!")
            return
        }
        isLoading = true
        Coroutines.main {
            val response = (application as MyApplication).myApi.firstPrizeLastDigitDetails(
                SharedPreUtils.getStringFromStorageWithoutSuspend(this, Constants.userIdKey, Constants.defaultUserId)!!,
                digit!!,
                TOTAL_PAGE,
                PAGE
            )

            binding.spinKit.visibility = View.GONE
            isLoading = false
            if(response.isSuccessful && response.body()!=null){
                val lottRes = response.body()!!
                if(!lottRes.error){
                    TOTAL_PAGE = lottRes.totalPages
                    list.addAll(lottRes.numbers!!)
                    adapter.notifyDataSetChanged()
                }else{
                    shortToast(lottRes.msg)
                }
            }else{
                shortToast(response.message())
            }
        }
    }
}