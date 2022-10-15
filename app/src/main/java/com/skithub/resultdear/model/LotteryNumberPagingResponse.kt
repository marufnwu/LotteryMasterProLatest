package com.skithub.resultdear.model

data class LotteryNumberPagingResponse(
    val status: String?=null,
    val message: String?=null,
    val currentPage: String? = null,
    val totalPages:String? =null,
    val data: MutableList<LotteryNumberModel>?=null
)
