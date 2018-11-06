package com.summer.itis.cardsproject.ui.game.game_list.game

import android.os.CountDownTimer
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.summer.itis.cardsproject.ui.base.base_first.BasePresenter
import com.summer.itis.cardsproject.ui.game.game_list.game.GameListView
import com.summer.itis.cardsproject.utils.AppHelper
import com.summer.itis.cardsproject.R
import com.summer.itis.cardsproject.model.common.Relation
import com.summer.itis.cardsproject.model.game.GameData
import com.summer.itis.cardsproject.model.game.Lobby
import com.summer.itis.cardsproject.model.game.LobbyPlayerData
import com.summer.itis.cardsproject.repository.RepositoryProvider
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.cardRepository
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.gamesRepository
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.userRepository
import com.summer.itis.cardsproject.repository.database.game.GamesRepository.Companion.FIELD_CREATOR
import com.summer.itis.cardsproject.repository.database.game.GamesRepository.Companion.FIELD_INVITED
import com.summer.itis.cardsproject.repository.database.user.UserRepository

import com.summer.itis.cardsproject.utils.Const
import com.summer.itis.cardsproject.utils.Const.BOT_GAME
import com.summer.itis.cardsproject.utils.Const.BOT_ID
import com.summer.itis.cardsproject.utils.Const.IN_GAME_STATUS
import com.summer.itis.cardsproject.utils.Const.ONLINE_GAME
import com.summer.itis.cardsproject.utils.Const.TAG_LOG
import com.summer.itis.cardsproject.utils.Const.USER_TYPE
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer

@InjectViewState
class GameListPresenter : BasePresenter<GameListView>() {

    lateinit var timer: CountDownTimer

    fun loadOfficialGamesByQuery(query: String) {
        AppHelper.currentUser.id.let {
            RepositoryProvider.gamesRepository
                    .findOfficialTestsByQuery(query, it)
                    .doOnSubscribe(Consumer<Disposable> { viewState.showLoading(it) })
                    .doAfterTerminate(Action { viewState.hideLoading() })
                    .subscribe({ viewState.changeDataSet(it) }, { viewState.handleError(it) })
        }
    }

    fun loadUserGamesByQuery(query: String, userId: String) {
        RepositoryProvider.gamesRepository
                .findUserTestsByQuery(query, userId)
                .doOnSubscribe(Consumer<Disposable> { viewState.showLoading(it) })
                .doAfterTerminate(Action { viewState.hideLoading() })
                .subscribe({ viewState.changeDataSet(it) }, { viewState.handleError(it) })
    }

    fun loadUserGames(userId: String) {
        RepositoryProvider.gamesRepository
                .findUserTests(userId)
                .doOnSubscribe(Consumer<Disposable> { viewState.showLoading(it) })
                .doAfterTerminate(Action { viewState.hideLoading() })
                .subscribe({ viewState.changeDataSet(it) }, { viewState.handleError(it) })
    }


    fun loadOfficialGames() {
        Log.d(Const.TAG_LOG, "load books")
        AppHelper.currentUser.id.let {
            RepositoryProvider.gamesRepository
                    .findOfficialTests(it)
                    .doOnSubscribe({ viewState.showLoading(it) })
                    .doAfterTerminate({ viewState.hideLoading() })
                    .doAfterTerminate({ viewState.setNotLoading() })
                    .subscribe({ viewState.changeDataSet(it) }, { viewState.handleError(it) })
        }
    }

    fun onItemClick(lobby: Lobby) {
        if(!lobby.id.equals(AppHelper.currentUser.lobbyId)) {
            viewState.showDetails(lobby)
        } else {
            viewState.showSnackBar(R.string.you_cant_play_with_youself)
        }
    }

    fun findGame(lobby: Lobby) {
        Log.d(TAG_LOG,"find game online")
        val gameData: GameData = GameData()
        lobby.creator?.playerId?.let{ gameData.enemyId = it}
        cardRepository.findCardsByType(gameData.enemyId,lobby.type, false).subscribe{ enemyCards ->
            val cardsSize = enemyCards.size
            if(cardsSize >= lobby.cardNumber) {
                gameData.role = FIELD_INVITED
                gameData.gameMode = ONLINE_GAME
                lobby.gameData = gameData
                AppHelper.currentUser.gameLobby = lobby
                viewState.showProgressDialog(R.string.progress_message)
                setGameRequestTimer(lobby)
                gamesRepository.goToLobby(lobby, gameFinded(), gameNotAccepted(lobby))
            } else {
                viewState.showSnackBar(R.string.enemy_doesnt_have_card_min)
            }
        }

    }

    private fun setGameRequestTimer(lobby: Lobby) {
        timer = object : CountDownTimer(25000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                viewState.hideProgressDialog()
                gamesRepository.notAccepted(lobby)
            }
        }
        timer.start()
    }

    fun gameFinded(): () -> Unit {
        return {
            timer.cancel()
            viewState.onGameFinded()
        }
    }

    fun gameNotAccepted(lobby: Lobby): () -> Unit {
        return  {
            timer.cancel()
            viewState.hideProgressDialog()
            gamesRepository.notAccepted(lobby)
        }
    }

    fun findBotGame() {
       playWithBot(prepareBotData())
    }

    private fun prepareBotData(): Lobby {
        val lobby: Lobby = Lobby()

        val playerData = LobbyPlayerData()
        playerData.playerId = UserRepository.currentId
        playerData.online = true

        lobby.creator = playerData
        lobby.status = IN_GAME_STATUS
        lobby.isFastGame = true
        lobby.type = USER_TYPE

        val enemyData = LobbyPlayerData()
        enemyData.playerId = BOT_ID
        enemyData.online = true

        val gameData: GameData = GameData()
        gameData.enemyId = BOT_ID
        gameData.gameMode = BOT_GAME
        gameData.role = FIELD_CREATOR
        lobby.gameData = gameData

        return lobby
    }

    private fun playWithBot(lobby: Lobby) {
        AppHelper.currentUser.let {
            it.gameLobby = lobby
            Log.d(TAG_LOG,"enemyId = ${lobby.gameData?.enemyId}")
            Log.d(TAG_LOG,"enemyId 2= ${it.gameLobby?.gameData?.enemyId}")
            gamesRepository.createBotLobby(lobby).subscribe { created ->
                val relation: Relation = Relation()
                relation.relation = IN_GAME_STATUS
                relation.id = lobby.id
                userRepository.changeJustUserStatus(IN_GAME_STATUS).subscribe()
                viewState.onBotGameFinded()
            }
        }
    }
}
