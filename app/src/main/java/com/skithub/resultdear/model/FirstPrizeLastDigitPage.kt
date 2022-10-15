package com.skithub.resultdear.model

data class FirstPrizeLastDigitPage(
    val error: Boolean? = false,
    val license: Boolean? = false,
    val subscribeData: SubscribeData? = null,
    val unSubscribeData: UnSubscribeData? = null
)