package com.summer.itis.cardsproject.model.card

import com.google.firebase.database.Exclude
import com.google.firebase.database.PropertyName
import com.summer.itis.cardsproject.model.common.Identified
import com.summer.itis.cardsproject.model.test.Test

open class Card: Identified {

    override lateinit var id: String
    @PropertyName("cardId")
    lateinit var absCardId: String
    lateinit var testId: String
    lateinit var type: String
    var intelligence: Int = 10
    var support: Int = 10
    var prestige: Int = 10
    var hp: Int = 10
    var strength: Int = 10

    var abstractCard: AbstractCard = AbstractCard()

    var test: Test = Test()
}
