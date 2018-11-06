package com.summer.itis.cardsproject.ui.base.base_first

import com.summer.itis.cardsproject.model.game.Lobby


interface BaseView: BasicFunctional {

    fun showGameRequestDialog(lobby: Lobby)

    fun hideGameRequestDialog()

    fun goToGame()
}