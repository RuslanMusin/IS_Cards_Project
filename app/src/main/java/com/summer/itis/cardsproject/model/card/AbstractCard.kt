package com.summer.itis.cardsproject.model.card

import com.summer.itis.cardsproject.model.common.Identified

class AbstractCard() : Identified {

    override lateinit var id: String
    lateinit var name: String
    lateinit var lowerName: String
    lateinit var photoUrl: String
    lateinit var extract: String
    lateinit var description: String
    lateinit var wikiUrl: String

    var isOwner: Boolean = false

}
