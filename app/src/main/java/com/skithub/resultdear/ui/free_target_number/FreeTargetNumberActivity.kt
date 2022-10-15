package com.skithub.resultdear.ui.free_target_number

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.skithub.resultdear.R
import com.skithub.resultdear.databinding.ActivityFreeTragetNumberBinding
import com.skithub.resultdear.model.response.GenericResponse
import com.skithub.resultdear.ui.MyApplication
import com.skithub.resultdear.utils.*
import com.skithub.resultdear.utils.MyExtensions.shortToast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FreeTargetNumberActivity : AppCompatActivity() {
    lateinit var binding: ActivityFreeTragetNumberBinding
    lateinit var loadingDialog : LoadingDialog
    var selectedCity : String? = null
    var selectedVillage : String? = null
    var selectedDistrict : String? = null
    lateinit var genericDialog: GenericDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =  ActivityFreeTragetNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)
        genericDialog = GenericDialog.make(this)

        loadingDialog = LoadingDialog(this)

        //checkLocation()

        binding.layoutTutorial.visibility = View.VISIBLE
        binding.layoutInfo.visibility = View.GONE

        binding.btnContinue.setOnClickListener {
            setDistrict()
        }

        binding.btnSubmit.setOnClickListener {
            onSubmit()
        }

        getBannerVideo()


    }

    private fun getBannerVideo() {

        Coroutines.main {
            CommonMethod.getBanner("FreeTargetNumber", binding.imgBanner, (application as MyApplication).myApi, this)
            binding.rippleBg.startRippleAnimation()
        }

    }

    private fun onSubmit() {
        if(selectedDistrict==null){
            shortToast("Select district first")
            return
        }

        if(selectedCity==null){
            shortToast("Select city first")
            return
        }

        loadingDialog.show()
        (application as MyApplication)
            .myApi
            .addLocation(
                SharedPreUtils.getStringFromStorageWithoutSuspend(this, Constants.userIdKey, Constants.defaultUserId)!!,
                CommonMethod.deviceId(this),
                selectedDistrict!!,
                selectedCity!!
            )
            .enqueue(object : Callback<GenericResponse> {
                override fun onResponse(
                    call: Call<GenericResponse>,
                    response: Response<GenericResponse>) {

                    loadingDialog.hide()




                    response.body().let {
                        shortToast(it?.msg!!)
                        if(!it.error){
                            showInReviewPopup()
                        }
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    loadingDialog.hide()

                }

            })




    }

    private fun showInReviewPopup() {
        genericDialog.setBodyText("Your request in review")
            .setImage(null)
            .setCancelable(false)
            .setShowNegativeButton(false)
            .setPositiveButtonText("Back")
            .setOnGenericDialogListener(object : GenericDialog.OnGenericDialogListener {
                override fun onPositiveButtonClick(dialog: GenericDialog?) {
                    finish()
                }

                override fun onNegativeButtonClick(dialog: GenericDialog?) {

                }

                override fun onToast(message: String?) {

                }

            }).init().showDialog()
    }

    private fun setDistrict() {
        binding.layoutTutorial.visibility = View.GONE
        binding.layoutInfo.visibility = View.VISIBLE


        ArrayAdapter.createFromResource(
            this,
            R.array.district_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            binding.spinnerDistrict.adapter = adapter
        }

        binding.spinnerDistrict.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                if(pos>0){
                    //item selected
                    selectedDistrict = p0?.selectedItem.toString().trim()
                    if(pos==1){
                        setCity()
                    }else{
                        selectedCity = "NA"
                        binding.spinnerCity.visibility = View.GONE
                        binding.spinnerVillage.visibility = View.GONE
                    }
                }else{
                    //nothing selected
                    selectedDistrict = null
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                selectedDistrict = null
            }

        }

        binding.spinnerDistrict.performClick()

    }

    private fun setCity() {
        binding.spinnerCity.visibility = View.VISIBLE

        ArrayAdapter.createFromResource(
            this,
            R.array.city_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            binding.spinnerCity.adapter = adapter
        }


        binding.spinnerCity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                if(pos>0){
                    //item selected
                    selectedCity = p0?.selectedItem.toString()

                    if(selectedCity == "Barrackpur - ব্যারাকপুর"){
                        //setVillage()
                    }else{
                        binding.spinnerVillage.visibility = View.GONE
                    }
                }else{
                    //nothing selected
                    selectedCity = null
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                selectedCity = null
            }

        }
        binding.spinnerCity.performClick()






    }

    private fun setVillage() {
        binding.spinnerVillage.visibility = View.VISIBLE


        ArrayAdapter.createFromResource(
            this,
            R.array.village_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            binding.spinnerVillage.adapter = adapter
        }


        binding.spinnerVillage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                if(pos>0){
                    //item selected
                    selectedVillage = p0?.selectedItem.toString()
                   shortToast(selectedVillage!!)
                }else{
                    //nothing selected
                    selectedVillage = null
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                selectedVillage = null
            }

        }
        binding.spinnerVillage.performClick()

    }


    fun checkLocation(){
        loadingDialog.show()
        val loc = Location(this, )
        loc.areaBlockListener = {
            loadingDialog.hide()
            loadingDialog.hide()
            if(!it){
                loc.showAreaBlockMessage()
            }
        }

        loc.isBlockArea()
    }
}