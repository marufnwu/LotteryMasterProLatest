package com.skithub.resultdear.utils.customview

import android.content.Context
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.skyfishjy.library.RippleBackground

class BlinkImageView(context: Context?, attrSet: AttributeSet) : LinearLayout(context, attrSet) {

    var imageView : ImageView? = null
    var ripBg : RippleBackground? = null

    init {

         imageView = ImageView(context, attrSet)
         ripBg = RippleBackground(context, attrSet)

        addView(ripBg)
        addView(imageView)
    }

    override fun addView(child: View?) {
        super.addView(child)

    }

    public fun setSrcAAA(drawable: Drawable){
        imageView?.setImageDrawable(drawable)
    }

}