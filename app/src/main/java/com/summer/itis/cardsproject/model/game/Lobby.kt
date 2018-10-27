package com.summer.itis.cardsproject.model.game

import com.google.firebase.database.Exclude
import com.summer.itis.cardsproject.model.common.Identified
import com.summer.itis.cardsproject.model.game.LobbyPlayerData
import com.summer.itis.cardsproject.utils.Const.ONLINE_STATUS
import com.summer.itis.cardsproject.utils.Const.USER_TYPE

class Lobby: Identified {

    override lateinit var id: String
    var title: String? = null
    var lowerTitle: String? = null
    var photoUrl: String? = null
    var cardNumber: Int = 5
    var status: String = ONLINE_STATUS
    var type: String = USER_TYPE
    var isFastGame: Boolean = false

    @Exclude
    var isMyCreation: Boolean = false

    var creator: LobbyPlayerData? = null
    var invited: LobbyPlayerData? = null

    @Exclude
    var gameData: GameData? = null
}
