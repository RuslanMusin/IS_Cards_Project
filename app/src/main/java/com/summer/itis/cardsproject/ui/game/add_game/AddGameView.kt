package com.summer.itis.cardsproject.ui.game.add_game

import com.summer.itis.cardsproject.model.game.Lobby
import com.summer.itis.cardsproject.ui.base.base_first.BaseView

interface AddGameView : BaseView {

    fun onGameCreated()

    fun createGame(lobby: Lobby)
}