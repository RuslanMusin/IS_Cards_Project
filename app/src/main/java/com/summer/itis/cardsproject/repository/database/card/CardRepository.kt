package com.summer.itis.cardsproject.repository.database.card

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.summer.itis.cardsproject.repository.database.base.RelationalRepository
import com.summer.itis.cardsproject.model.card.Card
import com.summer.itis.cardsproject.model.common.Relation
import com.summer.itis.cardsproject.model.test.Test
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.CARDS
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.USERS_ABSTRACT_CARDS
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.USERS_CARDS
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.USERS_TESTS
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.abstractCardRepository
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.testRepository
import com.summer.itis.cardsproject.utils.AppHelper
import com.summer.itis.cardsproject.utils.Const
import com.summer.itis.cardsproject.utils.Const.AFTER_TEST
import com.summer.itis.cardsproject.utils.Const.BEFORE_TEST
import com.summer.itis.cardsproject.utils.Const.LOSE_GAME
import com.summer.itis.cardsproject.utils.Const.OFFICIAL_TYPE
import com.summer.itis.cardsproject.utils.Const.SEP
import com.summer.itis.cardsproject.utils.Const.WIN_GAME
import com.summer.itis.cardsproject.utils.RxUtils
import io.reactivex.Observable
import io.reactivex.Single

class CardRepository: RelationalRepository<Card>() {

    override val TABLE_NAME = CARDS

    override lateinit var createReference: DatabaseReference
    override val databaseReference: DatabaseReference = AppHelper.dataReference.child(TABLE_NAME)

    private val FIELD_ID = "id"
    private val FIELD_CARD_ID = "cardId"
    private val FIELD_TEST_ID = "testId"
    private val FIELD_INTELLIGENCE = "intelligence"
    private val FIELD_SUPPORT = "support"
    private val FIELD_PRESTIGE = "prestige"
    private val FIELD_HP = "hp"
    private val FIELD_STRENGTH = "strength"
    private val FIELD_TYPE = "type"

    override fun getMapValues(entity: Card): HashMap<String, Any?> {
        val result = HashMap<String, Any?>()
        result[FIELD_ID] = entity.id
        result[FIELD_TEST_ID] = entity.testId
        result[FIELD_CARD_ID] = entity.absCardId
        result[FIELD_INTELLIGENCE] = entity.intelligence
        result[FIELD_PRESTIGE] = entity.prestige
        result[FIELD_HP] = entity.hp
        result[FIELD_SUPPORT] = entity.support
        result[FIELD_STRENGTH] = entity.strength
        result[FIELD_TYPE] = entity.type

        return result
    }

    override fun getValueFromSnapshot(dataSnapshot: DataSnapshot): Card? {
        return dataSnapshot.getValue(Card::class.java)
    }

    fun findFullCard(id: String): Single<Card> {
        val single : Single<Card> = Single.create { e ->
            findById(id).subscribe { card ->
                abstractCardRepository
                        .findById(card.absCardId)
                        .subscribe { t ->
                            card?.abstractCard = t
                            testRepository
                                    .findById(card.testId)
                                    .subscribe { test ->
                                        card?.test = test
                                        e.onSuccess(card)
                                    }
                        }
            }
        }
        return single.compose(RxUtils.asyncSingle())
    }

    fun findCards(ids: List<String>, findRelativeData: Boolean): Single<List<Card>> {
        if(findRelativeData) {
            val single: Single<List<Card>> = Single.create { e ->
                Observable
                        .fromIterable(ids)
                        .flatMap {
                            findFullCard(it).toObservable()
                        }
                        .toList()
                        .subscribe { entities ->
                            e.onSuccess(entities)
                        }
            }
            return single.compose(RxUtils.asyncSingle())
        } else {
            return findEntitiesByIds(ids)
        }
    }

    fun findCardsByType(userId: String, type: String, findRelativeData: Boolean): Single<List<Card>> {
        if(type.equals(OFFICIAL_TYPE)) {
            return findOfficialMyCards(userId, findRelativeData)
        } else {
            return findMyCards(userId, findRelativeData)
        }
    }

    fun findOfficialMyCards(userId: String, findRelativeData: Boolean): Single<List<Card>> {
        val single:Single<List<Card>> =  Single.create { e ->
            findMyCards(userId, findRelativeData).subscribe { cards ->
                val officials: MutableList<Card> = ArrayList()
                for (card in cards) {
                    if (card.type.equals(OFFICIAL_TYPE)) {
                        officials.add(card)
                    }
                }
                e.onSuccess(officials)
            }
        }
        return single.compose(RxUtils.asyncSingle())
    }

    fun findMyCards(userId: String, findRelativeData: Boolean): Single<List<Card>> {
        val single: Single<List<Card>> = Single.create { e ->
            findRelativeIds(userId, USERS_CARDS).subscribe { ids ->
                findCards(ids, true).subscribe { cards ->
                    e.onSuccess(cards)
                }
            }
        }
        return single.compose(RxUtils.asyncSingle())
    }
}
