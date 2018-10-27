package com.summer.itis.cardsproject.repository.database.test

import android.util.Log
import com.google.firebase.database.*
import com.summer.itis.cardsproject.model.test.Test
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.TESTS
import com.summer.itis.cardsproject.repository.database.base.RelationalRepository
import com.summer.itis.cardsproject.utils.AppHelper

import kotlin.collections.HashMap

class TestRepository: RelationalRepository<Test>() {

    override val TABLE_NAME = TESTS

    override lateinit var createReference: DatabaseReference
    override val databaseReference: DatabaseReference = AppHelper.dataReference.child(TABLE_NAME)


    private val TEST_QUESTIONS = "test_questions"
    private val TEST_CARDS = "test_cards"
    private val ABSTRACT_CARDS = "abstract_cards"


    private val FIELD_ID = "id"
    private val FIELD_TITLE = "title"
    private val FIELD_LOWER_TITLE = "lowerTitle"
    private val FIELD_CARD_ID = "absCardId"
    private val FIELD_AUTHOR_ID = "authorId"
    private val FIELD_AUTHOR_NAME = "authorName"
    private val FIELD_QUESTIONS = "questions"
    private val FIELD_DESC = "desc"
    private val FIELD_TYPE = "type"
    private val FIELD_IMAGE_URL = "imageUrl"


    private val FIELD_RELATION = "relation"

    override fun getValueFromSnapshot(dataSnapshot: DataSnapshot): Test? {
        return dataSnapshot.getValue(Test::class.java)
    }

    override fun getMapValues(entity: Test): HashMap<String, Any?> {
        val result = HashMap<String, Any?>()
        result[FIELD_ID] = entity.id
        result[FIELD_DESC] = entity.desc
        result[FIELD_TITLE] = entity.title
        result[FIELD_LOWER_TITLE] = entity.lowerTitle
        result[FIELD_AUTHOR_ID] = entity.authorId
        result[FIELD_AUTHOR_NAME] = entity.authorName
        result[FIELD_CARD_ID] = entity.cardId
        result[FIELD_QUESTIONS] = entity.questions
        result[FIELD_TYPE] = entity.type
        result[FIELD_IMAGE_URL] = entity.imageUrl
        return result
    }
}
