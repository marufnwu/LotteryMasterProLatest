package com.skithub.resultdear.model

data class NumberResponse(
    val error: Boolean =false,
    val msg: String = "",
    val totalPages: Int = 0,
    val numbers: MutableList<LotteryNumberModel>?=null
)
