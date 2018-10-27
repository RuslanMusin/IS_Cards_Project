package com.summer.itis.cardsproject.model.game

import com.summer.itis.cardsproject.model.card.Card

class CardChoose {

    lateinit var cardId: String
    var questionId: String
    var card: Card? = null

    constructor(cardId: String, questionId: String) {
        this.cardId = cardId
        this.questionId = questionId
    }

    constructor(card: Card, questionId: String) {
        this.card = card
        this.questionId = questionId
    }

    companion object {

    }
}
