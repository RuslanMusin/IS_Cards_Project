package com.summer.itis.cardsproject.model.user

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import com.summer.itis.cardsproject.model.card.Card
import com.summer.itis.cardsproject.model.common.Identified
import com.summer.itis.cardsproject.model.game.Lobby
import com.summer.itis.cardsproject.model.test.Test
import com.summer.itis.cardsproject.utils.Const.OFFLINE_STATUS

@IgnoreExtraProperties
class User: Identified {

    override lateinit var id: String

    lateinit var email: String
    lateinit var username: String
    lateinit var lowerUsername: String
    lateinit var photoUrl: String
    var description: String = "STANDART_DESC"
    lateinit var role: String
    var lobbyId: String? = null

    var isStandartPhoto: Boolean = true
    var status: String = OFFLINE_STATUS

    @Exclude
    var gameLobby: Lobby? = null

    @Exclude
    private val cards: List<Card>? = null

    @Exclude
    private val tests: List<Test>? = null

    constructor() {}

    constructor(email: String, username: String) {
        this.email = email
        this.username = username
    }
}
