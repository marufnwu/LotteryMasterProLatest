package com.skithub.resultdear.model.response

import com.google.gson.annotations.SerializedName

data class GenericApiResponse<T> (
    @SerializedName("error") val error: Boolean = true,
    @SerializedName("msg") val msg: String = "",
    @SerializedName("data") val data:T? = null
)