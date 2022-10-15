package com.skithub.resultdear.model

import com.google.gson.annotations.SerializedName

data class MiddlePart(
    @SerializedName("range") val range : String?,
    @SerializedName("count") val count : String?,
)