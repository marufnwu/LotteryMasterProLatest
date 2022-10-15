package com.skithub.resultdear.ui.notification

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.FrameLayout
import com.skithub.resultdear.databinding.ActivityNotificationWebBinding
import com.skithub.resultdear.databinding.ActivityWebViewBinding
import com.skithub.resultdear.utils.LoadingDialog

class NotificationWebActivity : AppCompatActivity() {
    lateinit var loadingDialog: LoadingDialog
    lateinit var binding : ActivityNotificationWebBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationWebBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this)

        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = MyWebViewClient()
        binding.webView.webChromeClient = MyChromeClient()

        val url = intent?.getStringExtra("url")

        if(url!=null){
            loadingDialog.show()
            binding.webView.loadUrl(url)
        }
    }
    inner class MyChromeClient: WebChromeClient(){
        private var mCustomView: View? = null
        private var mCustomViewCallback: CustomViewCallback? = null
        protected var mFullscreenContainer: FrameLayout? = null
        private var mOriginalOrientation = 0
        private var mOriginalSystemUiVisibility = 0

//
//        override fun onReceivedTouchIconUrl(view: WebView?, url: String?, precomposed: Boolean) {
//
//            Log.d("onReceivedTouchIconUrl", url!!)
//
//            super.onReceivedTouchIconUrl(view, url, precomposed)
//
//        }
//
//
//        override fun getDefaultVideoPoster(): Bitmap? {
//            return if (mCustomView == null) {
//                null
//            } else BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573)
//        }
//
//        override fun onHideCustomView() {
//            (window.getDecorView() as FrameLayout).removeView(mCustomView)
//            mCustomView = null
//            getWindow().getDecorView().setSystemUiVisibility(mOriginalSystemUiVisibility)
//            setRequestedOrientation(mOriginalOrientation)
//            mCustomViewCallback!!.onCustomViewHidden()
//            mCustomViewCallback = null
//        }
//
//        override fun onShowCustomView(
//            paramView: View?,
//            paramCustomViewCallback: CustomViewCallback?
//        ) {
//            if (mCustomView != null) {
//                onHideCustomView()
//                return
//            }
//            mCustomView = paramView
//            mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility()
//            mOriginalOrientation = getRequestedOrientation()
//            mCustomViewCallback = paramCustomViewCallback
//            (getWindow().getDecorView() as FrameLayout).addView(
//                mCustomView,
//                FrameLayout.LayoutParams(-1, -1)
//            )
//            getWindow().getDecorView()
//                .setSystemUiVisibility(3846 or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
//        }
    }

    inner class MyWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            Log.d("url", request?.url?.host.toString())

            if(request?.url?.host=="m.facebook.com" || request?.url?.host=="facebook.com"){

            }

            return super.shouldOverrideUrlLoading(view, request)
        }
        override fun onLoadResource(view: WebView?, url: String?) {
            super.onLoadResource(view, url)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            loadingDialog.show()
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            binding.webView.visibility = View.VISIBLE
            loadingDialog.hide()

            //addFbVideoView()

        }

        override fun onReceivedError( view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
        }
    }

}