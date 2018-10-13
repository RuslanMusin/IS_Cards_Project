package com.summer.itis.cardsproject.model.test

import com.summer.itis.cardsproject.model.common.Identified

class Answer: Identified {

    override lateinit var id: String

    lateinit var text: String
    var isRight: Boolean = false
    var userClicked: Boolean = false
}
