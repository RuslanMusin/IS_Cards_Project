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

    //SharedPreferences
    const val USER_DATA_PREFERENCES = "user_data"
    const val USER_USERNAME = "user_username"
    const val USER_PASSWORD = "user_password"

    //image path
    const val IMAGE_START_PATH = "images/users/"
    const val AVATAR = "avatar"
    const val STUB_PATH = "https://upload.wikimedia.org/wikipedia/commons/b/ba/Leonardo_self.jpg"

    const val SEP = "/"

}
