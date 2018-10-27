package com.summer.itis.cardsproject.model.game

import com.summer.itis.cardsproject.ui.game.play.bot_play.BotGameActivity.Companion.MODE_CHANGE_CARDS

class LobbyPlayerData() {

    lateinit var playerId: String
    var online: Boolean = false
    var randomSendOnLoseCard: String? = null
    var choosedCards: Map<String, CardChoose>? = null
    var answers: Map<String, Boolean>? = null
    var mode: String = MODE_CHANGE_CARDS

    companion object {
        val randomSendOnLoseCard = "randomSendOnLoseCard"
        val choosedCards = "choosedCards"
        val answers = "answers"
    }
}
