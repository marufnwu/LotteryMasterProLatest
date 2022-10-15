package com.skithub.resultdear.ui.number_not_plays

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skithub.resultdear.R
import com.skithub.resultdear.adapter.DuplicateLotteryNumberRecyclerAdapter
import com.skithub.resultdear.databinding.ActivityNotPlayedNumBinding
import com.skithub.resultdear.model.LotteryNumberModel
import com.skithub.resultdear.ui.MyApplication
import com.skithub.resultdear.utils.*
import com.skithub.resultdear.utils.MyExtensions.shortToast

class NotPlayedNumActivity : AppCompatActivity() {
    lateinit var loadingDialog: LoadingDialog
    lateinit var binding: ActivityNotPlayedNumBinding
    private var list: MutableList<LotteryNumberModel> = arrayListOf()
    private lateinit var adapter: DuplicateLotteryNumberRecyclerAdapter
    private lateinit var layoutManager: LinearLayoutManager

    var pastVisibleItem : Int =0
    var visibleItemCount = 0
    var totalItemCount=0

    private var PAGE = 1
    private var PAGE_SIZE= 30
    private var TOTAL_PAGE = 0
    var isLoading = true
    lateinit var middle : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotPlayedNumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this)
        middle = intent?.getStringExtra("middle")!!
        supportActionBar?.title = "$middle "+getString(R.string.numNotplayed)

        setupRecyclerView()
        loadDuplicateLotteryNumber()

    }



    private fun setupRecyclerView() {
        adapter= DuplicateLotteryNumberRecyclerAdapter(this,list)
        layoutManager= LinearLayoutManager(this)
        binding.recyclerView.layoutManager=layoutManager
        binding.recyclerView.adapter=adapter


        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                                    //getAudios(PAGE)
                                    loadDuplicateLotteryNumber()
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
                if(PAGE<2){
                    loadingDialog.show()
                }
                val response=(application as MyApplication)
                    .myApi
                    .getNotPlayedNumByMiddle(PAGE.toString(), TOTAL_PAGE.toString(), middle)

                isLoading = false

                if (response.isSuccessful && response.code()==200) {
                    loadingDialog.hide()
                    if (response.body()!=null) {
                        if (response.body()?.status.equals("success",true)) {

                            TOTAL_PAGE = response.body()?.totalPages!!.toInt()


                            list.addAll(response.body()?.data!!)
                            adapter.notifyDataSetChanged()
                            /*val Liveuserdb = FirebaseDatabase.getInstance().getReference("ActiveUsers")
                            val prefs = RegisterActivity.PreferenceHelper.customPreference(this, CUSTOM_PREF_NAME)
                            val map: HashMap<String, Any?> = HashMap()
                            map["phone"] = prefs.userPhone
                            map["activity"] = getString(R.string.common_number)
                            Liveuserdb.child(prefs.userToken!!).setValue(map)*/
                        } else {
                            shortToast("message:- ${response.body()?.message}")
                            Log.d(Constants.TAG,"message:- ${response.body()?.message}")
                        }
                    }
                } else {
                    Log.d("ErrorMsg", response.message().toString())
                    loadingDialog.hide()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                loadingDialog.hide()
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        if (newBase!=null) {
            super.attachBaseContext(CommonMethod.updateLanguage(newBase))
        } else {
            super.attachBaseContext(newBase)
        }
    }


}