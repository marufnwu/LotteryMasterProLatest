package com.skithub.resultdear.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task
import com.google.gson.Gson
import com.skithub.resultdear.utils.MyExtensions.shortToast


class AppReview {
    companion object{
        fun request(context : Context){
            val reviewManager = ReviewManagerFactory.create(context);


            val request: Task<ReviewInfo> = reviewManager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful()) {
                    // Getting the ReviewInfo object
                    val reviewInfo: ReviewInfo = task.getResult()
                    val flow: Task<Void> = reviewManager.launchReviewFlow(context as Activity, reviewInfo)
                    flow.addOnCompleteListener { task1 ->

                    }
                }else{
                    context.shortToast(task.exception?.message!!)
                }
            }
        }




        }
    }
