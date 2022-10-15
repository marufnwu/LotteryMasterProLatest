package com.skithub.resultdear.model.page

import com.google.gson.annotations.SerializedName
import com.skithub.resultdear.model.LotteryNumberResponse
import com.skithub.resultdear.model.UnSubscribeData

data class MiddlePage (
    @SerializedName("error") val error: Boolean? = false,
    @SerializedName("license") val license: Boolean? = false,
    @SerializedName("subscribeData") val subscribeData: LotteryNumberResponse? = null,
    @SerializedName("unSubscribeData") val unSubscribeData: UnSubscribeData? = null
)