package com.skithub.resultdear.ui.lmpclass_videos

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.skithub.resultdear.R
import com.skithub.resultdear.adapter.LmpClassVideoAdapter
import com.skithub.resultdear.databinding.ActivitySpecialVideoBinding
import com.skithub.resultdear.model.LmpVideo
import com.skithub.resultdear.model.response.SpecialVideoResponse
import com.skithub.resultdear.model.response.lmpVideoResponse
import com.skithub.resultdear.ui.MyApplication
import com.skithub.resultdear.utils.AudioStatus
import com.skithub.resultdear.utils.CommonMethod
import com.skithub.resultdear.utils.LoadingDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class SpecialVideoActivity : AppCompatActivity() {
    lateinit var lmpClassVideoAdapter: LmpClassVideoAdapter
    lateinit var layoutManager: LinearLayoutManager
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
    lateinit var loadingDialog: LoadingDialog
    lateinit var binding : ActivitySpecialVideoBinding

    var facebook: String? = null
    var youtube: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpecialVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()


        setRecyclerViewAdapter()
        getVideos(PAGE)
    }

    private fun initViews() {
        loadingDialog = LoadingDialog(this)
        layoutManager = LinearLayoutManager(this)
        binding.recyVideo.layoutManager = layoutManager
        binding.recyVideo.setHasFixedSize(true)



        binding.facebook.setOnClickListener {
            if(CommonMethod.fbPage!=null){
                CommonMethod.openLink(this, CommonMethod.fbPage!!)
            }
        }

        binding.youtube.setOnClickListener {
            if(CommonMethod.ytChannel!=null){
                CommonMethod.openLink(this, CommonMethod.ytChannel!!)
            }
        }

        initRecyScrollListener()
    }

    private fun setRecyclerViewAdapter() {
        lmpClassVideoAdapter = LmpClassVideoAdapter(this, lmpVideo)
        binding.recyVideo.adapter = lmpClassVideoAdapter
    }


    private fun initRecyScrollListener() {
        binding.recyVideo.addOnScrollListener(
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
                                        getVideos(PAGE)
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

    private fun getVideos(page: Int) {
        if(page==1){
            loadingDialog.show()
        }
        isLoading = true
        (application as MyApplication).myApi
            .getSpecialVideo(page)
            .enqueue( object : Callback<SpecialVideoResponse> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<SpecialVideoResponse>, response: Response<SpecialVideoResponse>) {
                    isLoading = false
                    if(response.isSuccessful && response.body()!=null){
                        loadingDialog.hide()
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

                override fun onFailure(call: Call<SpecialVideoResponse>, t: Throwable) {
                    loadingDialog.hide()
                    isLoading = false
                }

            }
            )
    }
}