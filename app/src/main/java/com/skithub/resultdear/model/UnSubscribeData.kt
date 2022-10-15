package com.skithub.resultdear.model

import com.skithub.resultdear.model.response.Banner


data class UnSubscribeData(
    val banner: Banner? = null,
    val contact: Contacts? = null,

)
