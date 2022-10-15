package com.skithub.resultdear.ui.lottery_serial_check

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.skithub.resultdear.databinding.ActivityNewVideoBinding
import com.skithub.resultdear.ui.MyApplication
import com.skithub.resultdear.utils.CommonMethod
import com.skithub.resultdear.utils.Coroutines

class NewVideoActivity : AppCompatActivity() {
    lateinit var binding : ActivityNewVideoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        getBanner()
    }

    private fun getBanner() {
        Coroutines.main {
            CommonMethod.getBanner("NewVideo", binding.ivBanner, (application as MyApplication).myApi, this)
        }
    }
}