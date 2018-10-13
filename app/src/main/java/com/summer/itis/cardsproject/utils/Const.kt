package com.summer.itis.cardsproject.utils

import com.google.gson.Gson

//обычный класс констант и прочего общего кода
object Const {

    const val TAG_LOG = "TAG"

    const val MAX_LENGTH = 80
    const val MORE_TEXT = "..."

    //Gamer status
    const val ONLINE_STATUS = "online_status"
    const val OFFLINE_STATUS = "offline_status"

    @JvmField
    val gsonConverter = Gson()

}
