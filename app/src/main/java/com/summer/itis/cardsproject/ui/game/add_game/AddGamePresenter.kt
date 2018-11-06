package com.summer.itis.cardsproject.ui.game.add_game

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.summer.itis.cardsproject.R
import com.summer.itis.cardsproject.model.game.Lobby
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.cardRepository
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.gamesRepository
import com.summer.itis.cardsproject.repository.database.user.UserRepository
import com.summer.itis.cardsproject.ui.base.base_first.BasePresenter
import com.summer.itis.cardsproject.utils.Const.TAG_LOG

@InjectViewState
class AddGamePresenter : BasePresenter<AddGameView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        Log.d(TAG_LOG, "attach presenter")
    }

    fun createGame(lobby: Lobby) {
        gamesRepository.createLobby(lobby).subscribe { e ->
            viewState.onGameCreated()
        }
    }

    fun checkCanCreateGame(lobby: Lobby) {
        Log.d(TAG_LOG,"lobby type = ${lobby.type}")
        cardRepository.findCardsByType(UserRepository.currentId,lobby.type, false).subscribe { myCards ->
            val mySize = myCards.size
            Log.d(TAG_LOG,"mySize = $mySize and cardNumber = ${lobby.cardNumber}")
            if (mySize >= lobby.cardNumber) {
                viewState.createGame(lobby)
            } else {
                viewState.showSnackBar(R.string.you_dont_have_card_min)
            }
        }
    }
}