package com.summer.itis.cardsproject.ui.game.play.user_play

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.summer.itis.cardsproject.model.card.Card
import com.summer.itis.cardsproject.model.game.CardChoose
import com.summer.itis.cardsproject.model.game.Lobby
import com.summer.itis.cardsproject.model.user.User
import com.summer.itis.cardsproject.repository.RepositoryProvider
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.cardRepository
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.gamesRepository
import com.summer.itis.cardsproject.repository.database.game.GamesRepository
import com.summer.itis.cardsproject.ui.base.base_first.BasePresenter
import com.summer.itis.cardsproject.utils.AppHelper
import com.summer.itis.cardsproject.utils.Const.MODE_PLAY_GAME
import com.summer.itis.cardsproject.utils.Const.TAG_LOG
import com.summer.itis.cardsproject.utils.getRandom

import io.reactivex.Single
import java.util.*

@InjectViewState
class PlayGamePresenter() : BasePresenter<PlayGameView>(), GamesRepository.InGameCallbacks {

    lateinit var lobby: Lobby

    var youCardChosed = false
    var enemyCardChosed = false
    var lastEnemyChoose: CardChoose? = null

    fun setInitState(initlobby: Lobby) {
        lobby = initlobby
        gamesRepository.setLobbyRefs(lobby.id)
        gamesRepository.watchMyStatus()
        setStartCards()
    }

    private fun setStartCards() {
        val single: Single<List<Card>> = cardRepository.findCardsByType(AppHelper.currentUser.id, lobby.type, true)
        single.subscribe { cards: List<Card>? ->
            cards?.let {
                val mutCards = cards.toMutableList()
                val myCards: MutableList<Card> = ArrayList()

                for (i in 1..lobby.cardNumber) {
                    mutCards.getRandom()?.let {
                        Log.d(TAG_LOG,"random card num = $i and name = ${it.abstractCard.name}")
                        myCards.add(it)
                        mutCards.remove(it)
                    }
                }
                if (cards.size > lobby.cardNumber) {
                    viewState.changeCards(myCards,mutCards)
                } else {
                    changeGameMode(MODE_PLAY_GAME)
                    setCardList(myCards, 20000)
                }
            }
        }
    }

    fun waitEnemyGameMode(mode: String): Single<Boolean> {
        Log.d(TAG_LOG,"wait mode  = $mode")
        return gamesRepository.waitGameMode(mode)
    }

    fun changeGameMode(mode: String) {
        Log.d(TAG_LOG,"change mode = $mode")
        gamesRepository.changeGameMode(mode).subscribe()
    }

    fun setCardList(myCards: List<Card>, time: Long) {
        Log.d(TAG_LOG,"set card list")
        viewState.waitEnemyTimer(time)
        waitEnemyGameMode(MODE_PLAY_GAME).subscribe { e ->
            viewState.setCardsList(ArrayList(myCards))
            viewState.setCardChooseEnabled(true)

            lobby.gameData?.enemyId?.let {
                RepositoryProvider.userRepository.findById(it)
                        .subscribe { t: User ->
                            viewState.setEnemyUserData(t)
                        }
            }
            gamesRepository.startGame(lobby, this)
        }
    }

    fun chooseCard(card: Card) {
        gamesRepository.findById(lobby.id).subscribe { e ->
            viewState.setCardChooseEnabled(false)
            gamesRepository.chooseNextCard(card.id)
            viewState.showYouCardChoose(card)
            youCardChosed = true
        }
    }

    fun answer(correct: Boolean) {
        viewState.hideQuestionForYou()
        viewState.hideEnemyCardChoose()
        viewState.hideYouCardChoose()
        viewState.showYourAnswer(correct)

        gamesRepository.findById(lobby.id).subscribe { e ->
            gamesRepository.answerOnLastQuestion(lobby, correct)
            enemyCardChosed = false
            youCardChosed = false
        }
    }

    override fun onGameEnd(type: GamesRepository.GameEndType, card: Card) {
        Log.d("Alm", "Game End: " + type)
        viewState.showGameEnd(type,card)
    }

    override fun onEnemyCardChosen(choose: CardChoose) {
        Log.d("Alm", "enemy chosen card " + choose.cardId)
        Log.d("Alm", "enemy chosen question " + choose.questionId)
        enemyCardChosed = true
        lastEnemyChoose = choose
        RepositoryProvider.cardRepository.findFullCard(choose.cardId).subscribe { card ->
            viewState.showEnemyCardChoose(card)
        }
    }

    fun enemyDisconnected() {
        gamesRepository.onEnemyDisconnectAndYouWin(lobby)
    }

    fun showQuestion() {
        gamesRepository.findById(lobby.id).subscribe { e ->
            if (youCardChosed and enemyCardChosed) {
                lastEnemyChoose?.let {
                    RepositoryProvider.cardRepository.findFullCard(it.cardId).subscribe { card ->
                        viewState.showQuestionForYou(card.test.questions
                                .first { q -> q.id == it.questionId })
                    }
                }
            }
        }

    }

    override fun onEnemyAnswered(correct: Boolean) {
        viewState.showEnemyAnswer(correct)
        viewState.setCardChooseEnabled(true)
    }
}
