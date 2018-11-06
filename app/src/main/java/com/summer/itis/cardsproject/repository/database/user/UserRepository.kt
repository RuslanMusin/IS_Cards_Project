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
import com.summer.itis.cardsproject.utils.Const.OFFLINE_STATUS
import com.summer.itis.cardsproject.utils.Const.ONLINE_STATUS
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

    fun changeJustUserStatus(status: String): Single<Boolean> {
        Log.d(TAG_LOG,"chageJustUserStatus = $status")
        val single: Single<Boolean> = Single.create{e ->
            if(AppHelper.userInSession) {
                AppHelper.currentUser.let { user ->
                    user.status = status
                    databaseReference.child(user.id).child(FIELD_STATUS).setValue(user.status)
                    user.lobbyId?.let {
                        databaseReference.root.child(LOBBIES).child(it).child(FIELD_STATUS).setValue(user.status)
                    }
                    e.onSuccess(true)
                }
            }
        }
        return single.compose(RxUtils.asyncSingle())
    }

    fun checkUserStatus(userId: String): Single<Boolean> {
        val single: Single<Boolean> = Single.create{e ->
            findById(userId).subscribe{user ->
                if(user.status.equals(ONLINE_STATUS)) {
                    e.onSuccess(true)
                } else {
                    e.onSuccess(false)
                }
            }
        }
        return single.compose(RxUtils.asyncSingle())
    }

    fun checkUserConnection(checkIt: () -> (Unit)) {
        if(AppHelper.userInSession) {
            AppHelper.currentUser.let {
                if(it.status.equals(OFFLINE_STATUS)) {
                    checkIt()
                }
                val myConnect = databaseReference.root.child(TABLE_NAME).child(it.id).child(FIELD_STATUS)
                myConnect.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (OFFLINE_STATUS.equals(snapshot.value) || it.status.equals(OFFLINE_STATUS)) {
                            Log.d(TAG_LOG, "my disconnect")
                            checkIt()
                        }

                    }

                })
                myConnect.onDisconnect().setValue(OFFLINE_STATUS)
            }
        }
    }

    fun setOnOfflineStatus() {
        if(AppHelper.userInSession) {
            AppHelper.currentUser.let {
                val myConnect = databaseReference.root.child(TABLE_NAME).child(it.id).child(FIELD_STATUS)
                myConnect.onDisconnect().setValue(OFFLINE_STATUS)

            }
        }
    }

}
