package com.summer.itis.cardsproject.ui.base.base_first

import android.annotation.SuppressLint
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.summer.itis.cardsproject.R
import com.summer.itis.cardsproject.model.game.GameData
import com.summer.itis.cardsproject.model.game.Lobby
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.gamesRepository
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.userRepository
import com.summer.itis.cardsproject.repository.database.game.GamesRepository
import com.summer.itis.cardsproject.repository.database.user.UserRepository
import com.summer.itis.cardsproject.utils.AppHelper
import com.summer.itis.cardsproject.utils.Const
import com.summer.itis.cardsproject.ui.base.base_first.BaseView
import com.summer.itis.cardsproject.utils.Const.IN_GAME_STATUS


open class BasePresenter<View: BaseView>: MvpPresenter<View>() {

    var isStopped: Boolean = false

    fun changeUserStatus(status: String) {
        userRepository.changeJustUserStatus(status).subscribe()
    }

    fun checkUserConnection(checkIt: () -> (Unit)) {
        userRepository.checkUserConnection(checkIt)
    }

    @SuppressLint("CheckResult")
    fun waitEnemy() {
        gamesRepository.waitEnemy().subscribe { relation ->
            if (relation.relation.equals(Const.IN_GAME_STATUS)) {
                gamesRepository.findById(relation.id).subscribe { lobby ->
                    if (!isStopped) {
                        setGameData(lobby)
                        viewState.showGameRequestDialog(lobby)

                    }
                }
            }
        }
    }

    private fun setGameData(lobby: Lobby) {
        AppHelper.currentUser.let {
            it.gameLobby = lobby
            val gameData: GameData = GameData()
            gameData.gameMode = Const.ONLINE_GAME
            val invitedId = lobby.invited?.playerId
            val creatorId = lobby.creator?.playerId
            if (invitedId != null && creatorId.equals(UserRepository.currentId)) {
                invitedId.let {
                    gameData.enemyId = it
                    gameData.role = GamesRepository.FIELD_CREATOR
                }
            } else {
                creatorId?.let {
                    gameData.enemyId = it
                    gameData.role = GamesRepository.FIELD_INVITED
                }
            }
            it.gameLobby?.gameData = gameData
        }
    }

    fun agreeGameRequest(lobby: Lobby) {
        lobby.gameData?.let { gameData ->
            userRepository.checkUserStatus(gameData.enemyId).subscribe { isOnline ->
                viewState.hideGameRequestDialog()
                if (isOnline) {
                    userRepository.changeJustUserStatus(IN_GAME_STATUS).subscribe { changed ->
                        gamesRepository.acceptMyGame(lobby).subscribe { e ->
                            viewState.goToGame()
                        }
                    }
                } else {
                    viewState.showSnackBar(R.string.enemy_not_online)
                    waitEnemy()
                }
            }

        }
    }

    fun refuseAndWait(lobby: Lobby) {
        viewState.hideGameRequestDialog()
        gamesRepository.refuseGame(lobby).subscribe{ e -> waitEnemy()}
    }
}