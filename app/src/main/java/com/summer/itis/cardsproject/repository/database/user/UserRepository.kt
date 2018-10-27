package com.summer.itis.cardsproject.repository.database.user

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.summer.itis.cardsproject.model.common.Relation
import com.summer.itis.cardsproject.model.user.User
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.LOBBIES
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.USERS
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.USER_FRIENDS
import com.summer.itis.cardsproject.repository.database.base.RelationalRepository
import com.summer.itis.cardsproject.utils.AppHelper
import com.summer.itis.cardsproject.utils.Const.SEP
import com.summer.itis.cardsproject.utils.Const.TAG_LOG
import com.summer.itis.cardsproject.utils.RxUtils
import io.reactivex.Single
import java.util.*

class UserRepository: RelationalRepository<User>() {

    override val TABLE_NAME: String = USERS
    override lateinit var createReference: DatabaseReference
    override val databaseReference: DatabaseReference = AppHelper.dataReference.child(TABLE_NAME)


    private val FIELD_RELATION = "relation"

    private val FIELD_ID = "id"
    private val FIELD_EMAIL = "email"
    private val FIELD_USERNAME = "username"
    private val FIELD_LOWER_NAME = "lowerUsername"
    private val FIELD_PHOTO_URL = "photoUrl"
    private val FIELD_DESC = "desc"
    private val FIELD_ROLE = "role"
    private val FIELD_IS_STAND_PHOTO = "isStandartPhoto"
    val FIELD_STATUS = "status"

    companion object {

        val FIELD_LOBBY_ID = "lobbyId"

        val currentId: String
            get() = AppHelper.currentUser.id
//            get() = Objects.requireNonNull<FirebaseUser>(FirebaseAuth.getInstance().currentUser).getUid()
    }

    override fun getMapValues(entity: User): HashMap<String, Any?> {
        val result = HashMap<String, Any?>()
        result[FIELD_ID] = entity.id
        result[FIELD_EMAIL] = entity.email
        result[FIELD_USERNAME] = entity.username
        result[FIELD_LOWER_NAME] = entity.lowerUsername
        result[FIELD_PHOTO_URL] = entity.photoUrl
        result[FIELD_DESC] = entity.description
        result[FIELD_ROLE] = entity.role
        result[FIELD_LOBBY_ID] = entity.lobbyId
        result[FIELD_IS_STAND_PHOTO] = entity.isStandartPhoto
        result[FIELD_STATUS] = entity.status

        return result
    }

    override fun getValueFromSnapshot(dataSnapshot: DataSnapshot): User? {
        return dataSnapshot.getValue(User::class.java)
    }

}
