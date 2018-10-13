package com.summer.itis.cardsproject.model.test

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import com.summer.itis.cardsproject.model.common.Identified
import com.summer.itis.cardsproject.model.test.Answer

class Question: Identified {

    override lateinit var id: String

    lateinit var question: String

    var answers: MutableList<Answer> = ArrayList()

    var userRight: Boolean = false
}
