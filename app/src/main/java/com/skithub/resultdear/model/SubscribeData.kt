package com.skithub.resultdear.model

import com.skithub.resultdear.model.LotteryNumberModel

data class SubscribeData(
    val error: Boolean = false,
    val firstPrizeLastDigit: List<LotteryNumberModel> = listOf(),
    val msg: String = ""
)

