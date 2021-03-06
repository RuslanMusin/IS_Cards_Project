package com.summer.itis.cardsproject.model.common

import com.google.firebase.database.IgnoreExtraProperties
import com.google.gson.annotations.Expose
import com.summer.itis.cardsproject.utils.Const.BEFORE_TEST
import java.util.*

//ОТНОШЕНИЕ МЕЖДУ ЮЗЕРАМИ(ДРУЗЬЯ И Т.Д)
@IgnoreExtraProperties
class Relation : ElementId() {

    var relation: String = BEFORE_TEST

    @Expose
    var relBefore: String? = null

    companion object {

        private val FIELD_RELATION = "relation"
        private val FIELD_ID = "id"

        fun toMap(id: String, relation: String): Map<String, Any> {
            val result = HashMap<String, Any>()
            result[FIELD_ID] = id
            result[FIELD_RELATION] = relation
            return result
        }
    }
}
