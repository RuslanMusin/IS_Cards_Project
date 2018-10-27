package com.summer.itis.cardsproject.repository.database.game

import android.util.Log
import com.google.firebase.database.*
import com.summer.itis.cardsproject.model.card.Card
import com.summer.itis.cardsproject.model.common.Relation
import com.summer.itis.cardsproject.model.game.CardChoose
import com.summer.itis.cardsproject.model.game.GameData
import com.summer.itis.cardsproject.model.game.Lobby
import com.summer.itis.cardsproject.model.game.LobbyPlayerData
import com.summer.itis.cardsproject.model.user.User
import com.summer.itis.cardsproject.repository.RepositoryProvider
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.LOBBIES
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.USERS
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.USERS_LOBBIES
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.cardRepository
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.userRepository
import com.summer.itis.cardsproject.repository.database.base.RelationalRepository
import com.summer.itis.cardsproject.repository.database.user.UserRepository
import com.summer.itis.cardsproject.repository.database.user.UserRepository.Companion.FIELD_LOBBY_ID
import com.summer.itis.cardsproject.utils.AppHelper
import com.summer.itis.cardsproject.utils.Const.IN_GAME_STATUS
import com.summer.itis.cardsproject.utils.Const.MODE_END_GAME
import com.summer.itis.cardsproject.utils.Const.NOT_ACCEPTED
import com.summer.itis.cardsproject.utils.Const.OFFICIAL_TYPE
import com.summer.itis.cardsproject.utils.Const.OFFLINE_STATUS
import com.summer.itis.cardsproject.utils.Const.ONLINE_GAME
import com.summer.itis.cardsproject.utils.Const.ONLINE_STATUS
import com.summer.itis.cardsproject.utils.Const.TAG_LOG
import com.summer.itis.cardsproject.utils.Const.USER_TYPE
import com.summer.itis.cardsproject.utils.RxUtils
import com.summer.itis.cardsproject.utils.getRandom
import io.reactivex.Single


class GamesRepository: RelationalRepository<Lobby>() {
    

    lateinit var currentLobbyRef: DatabaseReference
    lateinit var myLobbyRef: DatabaseReference
    lateinit var enemyLobbyRef: DatabaseReference

    var listeners = HashMap<DatabaseReference, ValueEventListener>()
    
    override val TABLE_NAME = LOBBIES

    override lateinit var createReference: DatabaseReference
    override val databaseReference: DatabaseReference = AppHelper.dataReference.child(TABLE_NAME)

    private val FIELD_ID = "id"
    private val FIELD_TITLE = "title"
    private val FIELD_LOWER_TITLE = "lowerTitle"
    private val FIELD_PHOTO_URL = "photoUrl"
    private val FIELD_CARD_NUMBER = "cardNumber"
    private val FIELD_STATUS = "status"
    private val FIELD_TYPE = "type"
    private val FIELD_IS_FAST_GAME = "isFastGame"
    private val FIELD_CREATOR = "creator"

    private val FIELD_INVITED = "invited"


    companion object {
        const val FIELD_RELATION = "relation"

        const val FIELD_INVITED = "invited"
        const val FIELD_CREATOR = "creator"

        const val FIELD_MODE = "mode"
        const val FIELD_ONLINE = "online"

    }

    override fun getValueFromSnapshot(dataSnapshot: DataSnapshot): Lobby? {
        return dataSnapshot.getValue(Lobby::class.java)
    }

    override fun getMapValues(entity: Lobby): HashMap<String, Any?> {
        val result = HashMap<String, Any?>()
        result[FIELD_ID] = entity.id
        result[FIELD_TITLE] = entity.title
        result[FIELD_LOWER_TITLE] = entity.lowerTitle
        result[FIELD_PHOTO_URL] = entity.photoUrl
        result[FIELD_CARD_NUMBER] = entity.cardNumber
        result[FIELD_STATUS] = entity.status
        result[FIELD_TYPE] = entity.type
        result[FIELD_IS_FAST_GAME] = entity.isFastGame
        result[FIELD_CREATOR] = entity.creator
        return result
    }


    fun setLobbyRefs(lobbyId: String) {
        currentLobbyRef = databaseReference.child(lobbyId)

        AppHelper.currentUser.let {
            if(it.gameLobby?.gameData?.role.equals(FIELD_INVITED)) {
                myLobbyRef = currentLobbyRef.child(FIELD_INVITED)
                enemyLobbyRef = currentLobbyRef.child(FIELD_CREATOR)
            } else {
                myLobbyRef = currentLobbyRef.child(FIELD_CREATOR)
                enemyLobbyRef = currentLobbyRef.child(FIELD_INVITED)
            }
        }
      
    }

    fun removeListeners() {
        for (l in listeners) {
            l.key.removeEventListener(l.value)
        }
        listeners.clear()
    }

    private fun setGameRelations(lobby: Lobby, myId: String, enemyId: String, myStatus: String, enemyStatus: String) {
        val relation:Relation = Relation()
        relation.id = lobby.id
        relation.relation = myStatus
        databaseReference.root.child(USERS_LOBBIES).child(myId).child(lobby.id).setValue(relation)
        relation.relation = enemyStatus
        databaseReference.root.child(USERS_LOBBIES).child(enemyId).child(lobby.id).setValue(relation)
    }

    private fun prepareGameData(lobby: Lobby, user: User, enemyId: String) {
        val gameData: GameData = GameData()
        gameData.enemyId = enemyId
        gameData.gameMode = ONLINE_GAME
        gameData.role = FIELD_CREATOR
        user.gameLobby = lobby
        user.gameLobby?.gameData = gameData
    }

    fun createBotLobby(lobby: Lobby): Single<Boolean> {
        val single: Single<Boolean> = Single.create { e ->
            setKey(lobby)
            setLobbyRefs(lobby.id)
            e.onSuccess(true)
        }
        return single.compose(RxUtils.asyncSingle())
    }

    fun removeRedundantLobbies(shouldFind: Boolean) {
        Log.d(TAG_LOG,"removeRedundantLobbies")
        if(AppHelper.userInSession) {
            AppHelper.currentUser.let { user ->
                databaseReference.root.child(USERS_LOBBIES).child(user.id).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (snap in snapshot.children) {
                            val relation: Relation? = snap.getValue(Relation::class.java)
                            relation?.let {
                                if (user.lobbyId.equals(it.id)) {
                                    val playerData: LobbyPlayerData = LobbyPlayerData()
                                    playerData.online = true
                                    playerData.playerId = UserRepository.currentId
                                    if (user.gameLobby == null) {
                                        user.gameLobby = Lobby()
                                        user.gameLobby?.gameData?.role = FIELD_CREATOR

                                    }
                                    setLobbyRefs(it.id)
                                    myLobbyRef.setValue(playerData)
                                    enemyLobbyRef.setValue(null)
                                }
                                databaseReference.root.child(USERS_LOBBIES).child(user.id).child(it.id).setValue(null)
                                if (shouldFind) {
                                    findById(it.id).subscribe { lobby ->
                                        if (lobby.isFastGame) {
                                            databaseReference.child(lobby.id).setValue(null)
                                        }
                                    }
                                }
                            }
                        }
                    }


                })
            }
        }
    }

    interface InGameCallbacks {
        fun onGameEnd(type: GameEndType, card: Card)
        fun onEnemyCardChosen(choose: CardChoose)
        fun onEnemyAnswered(correct: Boolean)
    }

    enum class GameEndType {
        YOU_WIN, YOU_LOSE, YOU_DISCONNECTED_AND_LOSE, ENEMY_DISCONNECTED_AND_YOU_WIN,
        DRAW
    }
}
