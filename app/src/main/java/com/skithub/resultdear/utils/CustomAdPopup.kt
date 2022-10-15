package com.skithub.resultdear.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.*
import com.skithub.resultdear.R
import com.skithub.resultdear.databinding.TutorialModelBinding
import com.skithub.resultdear.model.FacebookVideoModel
import com.skithub.resultdear.ui.webview.WebViewActivity
import com.skithub.resultdear.utils.MyExtensions.lifecycleOwner
import java.util.regex.Matcher
import java.util.regex.Pattern

class CustomAdPopup(private val context: Context) {

    val activityMap : MutableMap<Context, Boolean> = mutableMapOf()

    interface AdListener{
        fun onShow(dialogInterface: DialogInterface)
        fun onError(message: String)
        fun onDismiss()
    }


    lateinit var dialogBinding: TutorialModelBinding
    lateinit var dialog: Dialog
    lateinit var myAdListener: AdListener

    companion object{
        const val SHOW_INTERVAL_IN_SECOND = 30;
        var isDialogShowing: Boolean  = false
        var videoList : MutableList<FacebookVideoModel> = mutableListOf()
    }

    init {
        initBackPopup()
        addListener()
        getActivityLifecycle()
    }

    private fun getActivityLifecycle() {
        context.lifecycleOwner()?.lifecycle?.addObserver(LifecycleEventObserver { source, event ->
                if (Lifecycle.Event.ON_DESTROY == event) {
                    if(isDialogShowing){
                        dialog.dismiss()
                    }
                }
        })
    }

    private fun addListener() {
        this.addVoiceListener(object : AdListener {
            override fun onShow(dialogInterface: DialogInterface) {
                activityMap[context] = true
            }

            override fun onError(message: String) {
                finishActivity()
            }

            override fun onDismiss() {
                finishActivity()
            }

        })
    }

    private fun addVoiceListener(myAdListener: AdListener){
        this.myAdListener = myAdListener
    }



    private fun setDataToDialog(video: FacebookVideoModel){
        Glide.with(context)
            .load(video.thumbail)
            .into(dialogBinding.Thumbail)

        dialogBinding.videoTitle.text = video.videoTitle

        dialogBinding.Thumbail.setOnClickListener {
            openLink(context, video.videoLink!!)
        }

    }

    private fun openLink(context: Context, videoLink: String) {
        val facebookReg = "((https://|https://www.)?(fb.watch|m.facebook.com/watch|facebook.com/watch))(.*)"
        val pattern: Pattern = Pattern.compile(
            facebookReg ,
            Pattern.CASE_INSENSITIVE
        )
        val matcher: Matcher = pattern.matcher(videoLink)
        if (matcher.matches()) {
            context.startActivity(
                Intent(context, WebViewActivity::class.java)
                .putExtra("url", "https://lmpclass.sikderithub.com/watch.php?link=$videoLink"))
            return
        }

        CommonMethod.openLink(context, videoLink)
    }

    private fun initBackPopup() {
        dialog = Dialog(context, R.style.Theme_Lutt_NoActionBar)
        dialogBinding =TutorialModelBinding.inflate(LayoutInflater.from(context))

        dialogBinding.root.background = getDrawable(context, R.drawable.blur_bg)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(dialogBinding.root)
        dialogBinding.root.gravity = Gravity.CENTER

        dialog.window?.setBackgroundDrawable(getDrawable(context, R.drawable.blur_bg))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        val window: Window? = dialog.window
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        dialogBinding.imgCancel.visibility = View.VISIBLE
        dialogBinding.imgCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.imgBack.setOnClickListener {
            dialog.dismiss()
           finishActivity()
        }


        dialog.setOnShowListener {
            isDialogShowing = true
            myAdListener.onShow(it)
            dialogBinding.imgBack.visibility = View.VISIBLE
            dialogBinding.imgBack.alpha = 0.0f
            dialogBinding.imgBack.animate()
                .alpha(1.0f).duration = 6000

            SharedPreUtils.setCustomPopupTimeToStorage(context)
        }

        dialog.setOnDismissListener {
            myAdListener.onDismiss()
            isDialogShowing = false
        }

    }
    private fun show(){


        if(isDialogShowing){
            dialog.dismiss()
        }

        if(activityMap.containsKey(context)){
           myAdListener.onError("Popup already shown")
           return
        }

        if(videoList.isNotEmpty()){
            val audio = videoList.random()
            setDataToDialog(audio)
            dialog.show()
        }else{
            Log.d("VoiceMessage", "Empty List")
            //myVoiceListener.onError("No voice found")
           myAdListener.onError("No Videos Found")
        }
    }

    fun onBackPress(){
        show()
    }

    private fun finishActivity(){
        (context as Activity).finish()
    }


}