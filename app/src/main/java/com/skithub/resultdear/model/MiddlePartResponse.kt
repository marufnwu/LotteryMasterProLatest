package com.skithub.resultdear.model

import com.google.gson.annotations.SerializedName
import com.skithub.resultdear.model.MiddlePart

data class MiddlePartResponse(
    @SerializedName("error") val error : Boolean = true,
    @SerializedName("msg") val msg : String = "",
    @SerializedName("lastPart") val lastPArt : MutableList<MiddlePart> = mutableListOf(),
)
