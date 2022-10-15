package com.skithub.resultdear.model

import com.google.gson.annotations.SerializedName
import com.skithub.resultdear.model.LotteryNumberModel
import com.skithub.resultdear.model.SubscribeData
import com.skithub.resultdear.model.UnSubscribeData

data class MiddlePartPage(
    @SerializedName("error") val error: Boolean? = false,
    @SerializedName("license") val license: Boolean? = false,
    @SerializedName("subscribeData") val subscribeData: MutableList<String>? = null,
    @SerializedName("unSubscribeData") val unSubscribeData: UnSubscribeData? = null
)
