package com.summer.itis.cardsproject.ui.game.play.bot_play

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.summer.itis.cardsproject.model.card.Card
import com.summer.itis.cardsproject.model.game.CardChoose
import com.summer.itis.cardsproject.model.game.Lobby
import com.summer.itis.cardsproject.model.user.User
import com.summer.itis.cardsproject.repository.RepositoryProvider
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.cardRepository
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.gamesRepository
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.userRepository
import com.summer.itis.cardsproject.repository.database.game.GamesRepository
import com.summer.itis.cardsproject.ui.base.base_first.BasePresenter
import com.summer.itis.cardsproject.ui.game.play.bot_play.BotGameView
import com.summer.itis.cardsproject.utils.AppHelper
import com.summer.itis.cardsproject.utils.Const
import com.summer.itis.cardsproject.utils.Const.TAG_LOG
import com.summer.itis.cardsproject.utils.getRandom

import io.reactivex.Single
import java.util.*

@InjectViewState
class BotGamePresenter() : BasePresenter<BotGameView>(), GamesRepository.InGameCallbacks {
    
    lateinit var botCards: MutableList<Card>
    lateinit var myCards: MutableList<Card>
    lateinit var lobby: Lobby

    var youCardChosed = false
    var enemyCardChosed = false
    var lastEnemyChoose: CardChoose? = null

    fun setInitState(initlobby: Lobby) {
        lobby = initlobby
        gamesRepository.setLobbyRefs(lobby.id)
        setStartCards()
    }

    private fun setStartCards() {
        val single: Single<List<Card>> = cardRepository.findCardsByType(AppHelper.currentUser.id,lobby.type, true)
        single.subscribe { cards: List<Card>? ->
            Log.d(TAG_LOG, "cards finded")
            cards?.let {
                val mutCards = cards.toMutableList()
                val myCards: MutableList<Card> = ArrayList()

                for (i in 1..lobby.cardNumber) {
                    mutCards.getRandom()?.let {
                        Log.d(Const.TAG_LOG,"random card num = $i and name = ${it.abstractCard.name}")
                        myCards.add(it)
                        mutCards.remove(it)
                    }
                }
                setCardList(myCards)
            }
        }
    }

    fun setCardList(myCards: List<Card>) {
        Log.d(Const.TAG_LOG, "set card list")
        this.myCards = myCards.toMutableList()
        viewState.setCardsList(ArrayList(myCards))
        viewState.setCardChooseEnabled(true)
        setEnemyData()
        setEnemyCards().subscribe { e ->
            startGame()
        }
    }

    private fun setEnemyData() {
        lobby.gameData?.enemyId?.let {
            RepositoryProvider.userRepository.findById(it)
                    .subscribe { t: User ->
                        viewState.setEnemyUserData(t)
                    }
        }
    }

    private fun setEnemyCards(): Single<Boolean> {
        return Single.create { e ->
            Log.d(Const.TAG_LOG, "find bot cards")
            val single: Single<List<Card>>
            if (lobby.type.equals(Const.OFFICIAL_TYPE)) {
                single = cardRepository.findOfficialMyCards(Const.BOT_ID, true)
            } else {
                single = cardRepository.findMyCards(Const.BOT_ID, true)
            }
            single.subscribe { cards ->
                botCards = cards.toMutableList()
                e.onSuccess(true)
            }
        }
    }

    fun chooseCard(card: Card) {
        viewState.setCardChooseEnabled(false)
        viewState.showYouCardChoose(card)
        youCardChosed = true
        card.test.questions.getRandom()?.id?.let { questionId ->
            val choose = CardChoose(card, questionId)
            lobby.gameData?.lastMyChosenCard = choose

            Log.d(Const.TAG_LOG, "bot choose card")
            botChooseCard()
        }
    }

    fun botChooseCard() {
        val card: Card? = botCards.getRandom()
        botCards.remove(card)
        card?.let {
            card.test.questions.getRandom()?.id?.let { questionId ->
                val choose = CardChoose(card, questionId)
                enemyCardChosed = true
                lastEnemyChoose = choose
                lobby.gameData?.lastEnemyChoose = lastEnemyChoose
                viewState.showEnemyCardChoose(card)
            }
        }
    }

    fun answer(correct: Boolean) {
        viewState.hideQuestionForYou()
        viewState.hideEnemyCardChoose()
        viewState.hideYouCardChoose()
        viewState.showYourAnswer(correct)
        updateLobbyAfterAnswer(correct)
        Log.d(Const.TAG_LOG, "bot answer")
        answerBot()
        checkGameEnd(lobby)
    }

    private fun updateLobbyAfterAnswer(correct: Boolean) {
        lobby.gameData?.let{
            it.my_answers++
            if(correct) {
                it.my_score++
            }
        }
        enemyCardChosed = false
        youCardChosed = false
    }

    fun answerBot() {
        val correct: Boolean = Random().nextBoolean()
        lobby.gameData?.let{
            it.enemy_answers++
            if(correct) {
                it.enemy_score++
            }
        }
        viewState.showEnemyAnswer(correct)
        viewState.setCardChooseEnabled(true)
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

    fun showQuestion() {
        if (youCardChosed and enemyCardChosed) {
            lastEnemyChoose?.card?.let {
                viewState.showQuestionForYou(it.test.questions
                        .first { q -> q.id == lastEnemyChoose?.questionId })
            }

        }
    }

    override fun onEnemyAnswered(correct: Boolean) {
        viewState.showEnemyAnswer(correct)
        viewState.setCardChooseEnabled(true)
    }

    private fun checkGameEnd(lobby: Lobby) {
        if (lobby.gameData?.enemy_answers == lobby.cardNumber && lobby.gameData?.my_answers == lobby.cardNumber) {
            Log.d("Alm", "repo: GAME END!")
            val myScore = lobby.gameData?.my_score
            val enemyScore = lobby.gameData?.enemy_score
            if(myScore != null && enemyScore != null) {
                if (myScore > enemyScore) {
                    onWin(lobby)
                } else if (enemyScore > myScore) {
                    onLose()
                } else {
                    compareLastCards(lobby)
                }
            }
        }
    }

    private fun compareLastCards(lobby: Lobby) {
        val myLastCard: Card? = lobby.gameData?.lastMyChosenCard?.card
        val enemyLastCard: Card? = lobby.gameData?.lastEnemyChoose?.card

        myLastCard?.let { myLastCard ->
            enemyLastCard?.let { enemyLastCard ->
                var c = 0

                c += compareCardsParameter({ card -> card.intelligence }, myLastCard, enemyLastCard)
                c += compareCardsParameter({ card -> card.support }, myLastCard, enemyLastCard)
                c += compareCardsParameter({ card -> card.prestige }, myLastCard, enemyLastCard)
                c += compareCardsParameter({ card -> card.hp }, myLastCard, enemyLastCard)
                c += compareCardsParameter({ card -> card.strength }, myLastCard, enemyLastCard)

                if (c > 0) {
                    onWin(lobby)
                } else if (c < 0) {
                    onLose()
                } else {
                    onDraw()
                }
            }
        }

    }

    private fun onDraw() {
        lobby.gameData?.onYouLoseCard?.let { onGameEnd(GamesRepository.GameEndType.DRAW, it) }
    }

    fun compareCardsParameter(f: ((card: Card) -> Int), card1: Card, card2: Card): Int {
        return f(card1).compareTo(f(card2))
    }

    private fun onWin(lobby: Lobby) {
        lobby.gameData?.onEnemyLoseCard?.let { onGameEnd(GamesRepository.GameEndType.YOU_WIN, it) }
    }

    private fun onLose() {
        lobby.gameData?.onYouLoseCard?.let { onGameEnd(GamesRepository.GameEndType.YOU_LOSE, it)}
    }

    fun startGame() {
        setLoseCards()
    }

    private fun setLoseCards() {
        var onYouLoseCard = ArrayList(myCards).minus(botCards).getRandom()
        if(onYouLoseCard == null) {
            onYouLoseCard = myCards.getRandom()
        }
        lobby.gameData?.onYouLoseCard = onYouLoseCard
        lobby.gameData?.onEnemyLoseCard = botCards.getRandom()
    }
}
