package com.summer.itis.cardsproject.utils

import com.google.gson.Gson

//обычный класс констант и прочего общего кода
object Const {

    const val TAG_LOG = "TAG"

    const val MAX_LENGTH = 80
    const val MORE_TEXT = "..."

    const val ONLINE_GAME = "online_game"
    const val BOT_GAME = "bot_game"

    const val BOT_ID = "6n5OesjRMGN0jFAhP5jG9hxtaRg2"
    const val USER_ID = "user_id"

    const val ADD_CARD: Int = 1
    const val ITEM_JSON = "item_json"


    //game modes/stadies
    const val MODE_CHANGE_CARDS = "change_cards"
    const val MODE_PLAY_GAME = "play_game"
    const val MODE_CARD_VIEW = "card_view"
    const val MODE_END_GAME = "end_game"

    //Test list types
    const val OFFICIAL_LIST = "OFFICIAL"
    const val USER_LIST = "USER"
    const val MY_LIST = "MY"

    //Gamer status
    const val ONLINE_STATUS = "online_status"
    const val OFFLINE_STATUS = "offline_status"
    const val STOP_STATUS = "stop_status"
    const val IN_GAME_STATUS = "in_game_status"
    const val NOT_ACCEPTED = "not_accepted"
    const val EDIT_STATUS = "edit_status"

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

    //API
    //query
    const val FORMAT = "xml"
    const val ACTION_QUERY = "query"
    const val PROP = "extracts|pageimages|desc"
    const val EXINTRO = "1"
    const val EXPLAINTEXT = "1"
    const val PIPROP = "original"
    const val PILICENSE = "any"
    const val TITLES = "Толстой, Лев Николаевич"
    //opensearch
    const val ACTION_SEARCH = "opensearch"
    const val UTF_8 = "1"
    const val NAMESPACE = "0"
    const val SEARCH = "Лев Толстой"

    //TestRelation
    const val WIN_GAME = "win_game"
    const val WIN_AFTER_WIN = "after_win_test"
    const val LOSE_AFTER_WIN = "ore_after_test"
    const val TEST_AFTER_WIN = ""

    const val AFTER_TEST = "after_test"
    const val WIN_AFTER_TEST = "after_win_test"
    const val TEST_AFTER_TEST = "ore_after_test"
    const val LOSE_AFTER_TEST = ""

    const val LOSE_GAME = "lose_game"
    const val WIN_AFTER_LOSE = "after_win_test"
    const val LOSE_AFTER_LOSE = "ore_after_test"
    const val TEST_AFTER_LOSE = ""

    const val BEFORE_TEST = "before_test"

    //Firebase constants
    const val SEP = "/"
    const val QUERY_END = "\uf8ff"

    //GameType
    const val OFFICIAL_TYPE = "official_type"
    const val USER_TYPE = "user_type"

    //UserType
    const val ADMIN_ROLE = "admin_role"
    const val USER_ROLE = "user_role"

    //User type
    const val WATCHER_TYPE = "watcher"
    const val OWNER_TYPE = "owner"
    const val RESTRICT_OWNER_TYPE = "restrict_owner"
    const val FOLLOWER_TYPE = "follower"

    const val USER_KEY = "user"

}
