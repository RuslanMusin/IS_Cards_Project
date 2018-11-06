package com.summer.itis.cardsproject.ui.game.play.user_play

import com.summer.itis.cardsproject.model.card.Card
import com.summer.itis.cardsproject.model.test.Question
import com.summer.itis.cardsproject.model.user.User
import com.summer.itis.cardsproject.repository.database.game.GamesRepository
import com.summer.itis.cardsproject.ui.game.play.PlayView


interface PlayGameView : PlayView {

    fun setEnemyUserData(user: User)

    fun setCardsList(cards: ArrayList<Card>)

    fun changeCards(cards: MutableList<Card>, mutCards: MutableList<Card>)

    fun setCardChooseEnabled(enabled: Boolean)

    fun showEnemyCardChoose(card: Card)
    fun hideEnemyCardChoose()

    fun showQuestionForYou(question: Question)
    fun hideQuestionForYou()

    fun showYouCardChoose(card: Card)
    fun hideYouCardChoose()

    fun showEnemyAnswer(correct: Boolean)
    fun showYourAnswer(correct: Boolean)

    fun showGameEnd(type: GamesRepository.GameEndType, card: Card)

    fun waitEnemyTimer(time: Long)
}
