package com.summer.itis.cardsproject.repository.database.card

import android.util.Log
import com.google.firebase.database.*
import com.summer.itis.cardsproject.model.card.AbstractCard
import com.summer.itis.cardsproject.model.card.Card
import com.summer.itis.cardsproject.repository.RepositoryProvider
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.ABSTRACT_CARDS
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.USERS_ABSTRACT_CARDS
import com.summer.itis.cardsproject.repository.database.base.ChildRelativeRepository
import com.summer.itis.cardsproject.utils.AppHelper
import com.summer.itis.cardsproject.utils.Const
import com.summer.itis.cardsproject.utils.RxUtils
import io.reactivex.Single
import java.util.regex.Pattern

class AbstractCardRepository(): ChildRelativeRepository<AbstractCard>() {

    override val TABLE_NAME = ABSTRACT_CARDS

    override lateinit var createReference: DatabaseReference
    override val databaseReference: DatabaseReference = AppHelper.dataReference.child(TABLE_NAME)

    private val FIELD_ID = "id"
    private val FIELD_WIKI_URL = "wikiUrl"
    private val FIELD_NAME = "name"
    private val FIELD_LOWER_NAME = "lowerName"
    private val FIELD_PHOTO_URL = "photoUrl"
    private val FIELD_EXTRACT = "extract"
    private val FIELD_DESCRIPTION = "desc"

    override fun getMapValues(entity: AbstractCard): HashMap<String, Any?> {
        val result = HashMap<String, Any?>()
        result[FIELD_ID] = entity.id
        result[FIELD_NAME] = entity.name
        result[FIELD_LOWER_NAME] = entity.lowerName
        result[FIELD_PHOTO_URL] = entity.photoUrl
        result[FIELD_WIKI_URL] = entity.wikiUrl
        result[FIELD_EXTRACT] = entity.extract
        result[FIELD_DESCRIPTION] = entity.description

        return result
    }

    override fun getValueFromSnapshot(dataSnapshot: DataSnapshot): AbstractCard? {
        return dataSnapshot.getValue(AbstractCard::class.java)
    }

    fun findMyAbstractCards(userId: String): Single<List<AbstractCard>> {
        val single: Single<List<AbstractCard>> = Single.create { e ->
            findRelativeIds(userId, USERS_ABSTRACT_CARDS).subscribe { ids ->
                findEntitiesByIds(ids).subscribe { cards -> e.onSuccess(cards) }
            }
        }
        return single.compose(RxUtils.asyncSingle())
    }

}
