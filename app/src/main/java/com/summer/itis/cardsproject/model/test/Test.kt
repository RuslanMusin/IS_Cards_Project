package com.summer.itis.cardsproject.model.test

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import com.summer.itis.cardsproject.model.card.Card
import com.summer.itis.cardsproject.model.common.Identified
import java.util.*

@IgnoreExtraProperties
class Test: Identified {

    override lateinit var id: String
    lateinit var title: String
    lateinit var lowerTitle: String
    lateinit var desc: String
    lateinit var authorId: String
    lateinit var authorName: String
    lateinit var cardId: String
    lateinit var type: String
    lateinit var imageUrl: String
    var questions: MutableList<Question> = ArrayList()

    lateinit var card: Card

    var testDone: Boolean = false

    lateinit var rightQuestions: List<Question>

    lateinit var wrongQuestions: List<Question>
}
