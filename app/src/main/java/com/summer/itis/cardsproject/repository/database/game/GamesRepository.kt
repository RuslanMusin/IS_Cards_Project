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

    lateinit var callbacks: InGameCallbacks

    var lastEnemyChoose: CardChoose? = null
    var lastMyChosenCardId: String? = null

    var enemyId: String? = null

    var enemy_answers = 0;
    var enemy_score = 0;

    var my_answers = 0;
    var my_score = 0;

    var onYouLoseCard: Card? = null
    var onEnemyLoseCard: Card? = null

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

    fun removeLobby(id: String) {
        Log.d(TAG_LOG,"remove lobby $id")
        databaseReference.child(id).removeValue()
        AppHelper.currentUser.let {
            it.lobbyId = null
            databaseReference.root.child(USERS).child(it.id).child(FIELD_LOBBY_ID).setValue(null)
            databaseReference.root.child(USERS_LOBBIES).child(it.id).child(id).setValue(null)
        }
    }


    fun resetData() {
        lastEnemyChoose = null
        lastMyChosenCardId = null
        enemyId = null
        enemy_answers = 0;
        enemy_score = 0;
        my_answers = 0;
        my_score = 0;

        onYouLoseCard = null
        onEnemyLoseCard = null

        removeListeners()
    }

    fun removeListeners() {
        for (l in listeners) {
            l.key.removeEventListener(l.value)
        }
        listeners.clear()
    }

    fun createLobby(lobby: Lobby): Single<Boolean> {
        val single: Single<Boolean> = Single.create { e ->
            setKey(lobby)
            createReference = databaseReference.child(lobby.id)
            createEntity(lobby).subscribe()
            AppHelper.currentUser.let {
                it.id.let { it1 ->
                    databaseReference.root.child(USERS).child(it1).child(FIELD_LOBBY_ID).setValue(lobby.id)
                    val reference = databaseReference.root.child(USERS_LOBBIES).child(it1).child(lobby.id)
                    createRelation(reference, lobby.id, ONLINE_STATUS)
                }
                it.lobbyId = lobby.id
                e.onSuccess(true)
            }

        }
        return single.compose(RxUtils.asyncSingle())
    }

    fun removeFastLobby(userId: String, lobby: Lobby): Single<Boolean> {
        Log.d(TAG_LOG,"remove fast lobby")
        val single: Single<Boolean> = Single.create { e ->
            val lobbyId: String? = lobby.id
            lobbyId?.let {
                databaseReference.child(it).removeValue()
                AppHelper.currentUser.let {
                    it.id.let { it1 ->
                        databaseReference.root.child(USERS_LOBBIES).child(it1).child(lobbyId).removeValue()
                        databaseReference.root.child(USERS_LOBBIES).child(userId).child(lobbyId).removeValue()
                    }
                    e.onSuccess(true)
                }
            }
        }
        return single.compose(RxUtils.asyncSingle())
    }

    fun createFastLobby(userId: String, lobby: Lobby): Single<Lobby> {
        Log.d(TAG_LOG, "create fast lobby")
        val single: Single<Lobby> = Single.create { e ->
            setKey(lobby)
            createReference = databaseReference.child(lobby.id)
            createEntity(lobby).subscribe()
            AppHelper.currentUser.let {
                setGameRelations(lobby, it.id, userId, ONLINE_STATUS, IN_GAME_STATUS)
                prepareGameData(lobby, it, userId)
                setLobbyRefs(lobby.id)
                e.onSuccess(lobby)
            }
        }
        return single.compose(RxUtils.asyncSingle())
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

    fun refuseGame(lobby: Lobby): Single<Boolean> {
        val single: Single<Boolean> = Single.create { e ->
            Log.d(TAG_LOG, "refuse game")
            val relation: Relation = Relation()
            relation.id = lobby.id
            relation.relation = NOT_ACCEPTED
            lobby.gameData?.enemyId?.let {
                Log.d(TAG_LOG,"refuse in db")
                databaseReference.root.child(USERS_LOBBIES).child(it).child(lobby.id).setValue(relation)
            }
            AppHelper.currentUser.let {
                databaseReference.root.child(USERS_LOBBIES).child(it.id).child(lobby.id).setValue(null)
                        .addOnCompleteListener{ e.onSuccess(true)}

            }
        }
        return single.compose(RxUtils.asyncSingle())
    }

    fun waitEnemy(): Single<Relation>{
        val single: Single<Relation> = Single.create{ e ->
            AppHelper.currentUser.let { user ->
                val query: Query = databaseReference.root.child(USERS_LOBBIES).child(user.id)
                query.addValueEventListener(object: ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }
                    override fun onDataChange(p0: DataSnapshot) {
                        for(snap in p0.children) {
                            val relation: Relation? = snap.getValue(Relation::class.java)
                            relation?.let {
                                if(it.relation.equals(IN_GAME_STATUS)) {
                                    Log.d(TAG_LOG,"wait enemy in game")
                                    user.status = IN_GAME_STATUS
                                    query.removeEventListener(this)
                                    e.onSuccess(relation)
                                }
                                if(it.relation.equals(NOT_ACCEPTED)) {
                                    Log.d(TAG_LOG,"enemy refused")
                                    query.removeEventListener(this)
                                    e.onSuccess(relation)
                                }
                            }
                        }
                    }

                })
            }

        }
        return single.compose(RxUtils.asyncSingle())
    }

    fun disconnectMe(): Single<Boolean> {
        val single: Single<Boolean> = Single.create { e ->
            Log.d(TAG_LOG, "disconnect me")
            val myConnect = databaseReference.root.child(USERS).child(AppHelper.currentUser.id).child(FIELD_STATUS)
            myConnect.setValue(OFFLINE_STATUS)
            e.onSuccess(true)
        }
        return single.compose(RxUtils.asyncSingle())
    }

    fun goToLobby(lobby: Lobby, onAccepted: () -> (Unit), onNotAccepted: () -> (Unit)) {
        setLobbyRefs(lobby.id)

        AppHelper.currentUser.let { user ->
            val playerData = LobbyPlayerData()
            playerData.playerId = AppHelper.currentUser.id
            playerData.online = true
            myLobbyRef.setValue(playerData)

            var reference = databaseReference.root.child(USERS_LOBBIES).child(user.id).child(lobby.id)
            createRelation(reference, user.id, ONLINE_STATUS)
            lobby.creator?.playerId?.let {
                reference = databaseReference.root.child(USERS_LOBBIES).child(it).child(lobby.id)
                createRelation(reference, it, IN_GAME_STATUS)
            }
            waitResponse(lobby, onAccepted, onNotAccepted)
        }
    }

    private fun waitResponse(lobby: Lobby, accepted: () -> (Unit), onNotAccepted: () -> (Unit)) {
        val query = databaseReference.root.child(USERS_LOBBIES).child(UserRepository.currentId).child(lobby.id)
        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(snap: DataSnapshot) {
                val relation: Relation? = snap.getValue(Relation::class.java)
                relation?.let {
                    if (it.relation.equals(IN_GAME_STATUS)) {
                        userRepository.changeJustUserStatus(IN_GAME_STATUS).subscribe()
                        accepted()
                    } else if (it.relation.equals(NOT_ACCEPTED)) {
                        onNotAccepted()
                    }
                }
                query.removeEventListener(this)
            }

        })
    }

    fun notAccepted(lobby: Lobby) {
        AppHelper.currentUser.let {
            Log.d(TAG_LOG,"not accept")
            databaseReference.root.child(USERS_LOBBIES).child(it.id).child(lobby.id).setValue(null)
            myLobbyRef.setValue(null)

        }
    }

    fun acceptMyGame(lobby: Lobby): Single<Boolean> {
        return Single.create { e ->
            val relation: Relation = Relation()
            relation.id = lobby.id
            relation.relation = IN_GAME_STATUS
            lobby.gameData?.enemyId?.let {
                databaseReference.root.child(USERS_LOBBIES).child(it).child(lobby.id).setValue(relation)
                e.onSuccess(true)
            }
        }
    }

    fun startGame(lobby: Lobby, callbacks: InGameCallbacks) {
        this.callbacks = callbacks
        selectOnLoseCard(lobby)
        watchEnemyCards()
        watchEnemyAnswers(lobby)
        watchMyConnection()
        watchEnemyConnection(lobby)
    }

    private fun watchEnemyAnswers(lobby: Lobby) {
        enemyLobbyRef.child(LobbyPlayerData.answers)
                .addChildEventListener(object : ChildEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

                    override fun onChildChanged(p0: DataSnapshot, p1: String?) {}

                    override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                        Log.d("Alm", "onChildAdded to enemy answers")
                        val correct = dataSnapshot.value as Boolean
                        callbacks.onEnemyAnswered(correct)

                        enemy_answers++
                        if (correct) {
                            enemy_score++
                        }
                        Log.d(TAG_LOG,"enemyAnswers = $enemy_answers")
                        Log.d(TAG_LOG,"enemyScroe = $enemy_score")
                        checkGameEnd(lobby)
                    }

                    override fun onChildRemoved(p0: DataSnapshot) {}
                })
    }

    private fun watchEnemyCards() {
        enemyLobbyRef.child(LobbyPlayerData.choosedCards)
                .addChildEventListener(object : ChildEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

                    override fun onChildChanged(p0: DataSnapshot, p1: String?) {}

                    override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                        lastEnemyChoose = dataSnapshot.getValue(CardChoose::class.java)
                        lastEnemyChoose?.let { callbacks.onEnemyCardChosen(it) }
                    }

                    override fun onChildRemoved(p0: DataSnapshot) {}
                })
    }

    private fun watchEnemyConnection(lobby: Lobby) {
        lobby.gameData?.enemyId?.let {
            val enemyConnect = databaseReference.root.child(USERS).child(it).child(FIELD_STATUS)
            enemyConnect.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && OFFLINE_STATUS.equals(snapshot.value)) {
                        Log.d(TAG_LOG, "enemy disconnect")
                        onEnemyDisconnectAndYouWin(lobby)
                    }
                }
            })
        }
    }

    private fun watchMyConnection() {
        val myConnect = databaseReference.root.child(USERS).child(UserRepository.currentId).child(FIELD_STATUS)
        val connectedRef = myLobbyRef.child(FIELD_ONLINE)
        connectedRef.setValue(true)
        myConnect.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(OFFLINE_STATUS.equals(snapshot.value)) {
                    Log.d(TAG_LOG,"my disconnect")
                    onDisconnectAndLose(true)
                }
            }

        })
        myConnect.onDisconnect().setValue(OFFLINE_STATUS)
    }

    private fun selectOnLoseCard(lobby: Lobby) {
        lobby.gameData?.enemyId?.let {
            cardRepository.findCardsByType(it,lobby.type, false).subscribe { enemyCards: List<Card>? ->
                cardRepository.findCardsByType(AppHelper.currentUser.id,lobby.type, false).subscribe { myCards: List<Card>? ->
                    onYouLoseCard = enemyCards?.let { it1 -> ArrayList(myCards).minus(it1).getRandom() }
                    if(onYouLoseCard == null) {
                        onYouLoseCard = myCards?.getRandom()
                    }

                    Log.d(TAG_LOG,"onYouLoseCard = ${onYouLoseCard?.id}")

                    myLobbyRef
                            .child(LobbyPlayerData.randomSendOnLoseCard)
                            .setValue(onYouLoseCard?.id)
                }
            }
        }

        enemyLobbyRef
                .child(LobbyPlayerData.randomSendOnLoseCard)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            RepositoryProvider.cardRepository
                                    .findFullCard(dataSnapshot.value as String)
                                    .subscribe { t: Card? ->
                                        onEnemyLoseCard = t
                                    }
                        }
                    }
                })
    }

    fun chooseNextCard(cardId: String) {
        lastMyChosenCardId = cardId;
        RepositoryProvider.cardRepository.findFullCard(cardId).subscribe { card: Card ->
            val questionId = card.test.questions.getRandom()?.id
            questionId?.let {
                val choose = CardChoose(cardId, questionId)
                myLobbyRef.child(LobbyPlayerData.choosedCards).push().setValue(choose)
            }

        }
    }

    fun answerOnLastQuestion(lobby: Lobby, correct: Boolean) {
        val query: Query =  myLobbyRef
                .child(LobbyPlayerData.choosedCards)
                .orderByKey()
                .limitToLast(1)

        my_answers++
        if (correct) {
            my_score++
        }
        Log.d(TAG_LOG,"myAnswers = $my_answers")
        Log.d(TAG_LOG,"myScore = $my_score")

        checkGameEnd(lobby)

        query.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}

            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                val key = dataSnapshot.key

                key?.let {
                    myLobbyRef.child(LobbyPlayerData.answers)
                            .child(it)
                            .setValue(correct)
                }

                query.removeEventListener(this)
            }

            override fun onChildRemoved(p0: DataSnapshot) {}
        })

    }

    private fun checkGameEnd(lobby: Lobby) {
        if (enemy_answers == lobby.cardNumber && my_answers == lobby.cardNumber) {
            Log.d("Alm", "repo: GAME END!")

            Log.d("Alm", "repo: GAME END onEnemyLoseCard: " + onEnemyLoseCard?.id)
            Log.d("Alm", "repo: GAME END onYouLoseCard: " + onYouLoseCard?.id)
            changeGameMode(MODE_END_GAME).subscribe{ changed ->
                waitGameMode(MODE_END_GAME).subscribe{ waited ->
                    //TODO
                    Log.d(TAG_LOG,"gameEnd and removeLobby")
                    removeLobbyAndRelations(lobby)

                    if (my_score > enemy_score) {
                        onWin(lobby)

                    } else if (enemy_score > my_score) {
                        onLose()

                    } else {
                        compareLastCards(lobby)
                    }
                }
            }

        }
    }

    private fun removeLobbyAndRelations(lobby: Lobby) {
        if(lobby.isFastGame) {
            currentLobbyRef.setValue(null)
            val reference: DatabaseReference = databaseReference.root.child(USERS_LOBBIES).child(UserRepository.currentId)
            reference.child(lobby.id).setValue(null)
        }
        if(!lobby.creator?.playerId.equals(UserRepository.currentId)) {
            val reference: DatabaseReference = databaseReference.root.child(USERS_LOBBIES).child(UserRepository.currentId)
            reference.child(lobby.id).setValue(null)
        } else {
            val playerData: LobbyPlayerData = LobbyPlayerData()
            playerData.online = true
            playerData.playerId = UserRepository.currentId
            myLobbyRef.setValue(playerData)
            enemyLobbyRef.setValue(null)
            databaseReference.root.child(USERS_LOBBIES).child(UserRepository.currentId).child(lobby.id).setValue(null)
        }
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

    fun waitGameMode(mode: String): Single<Boolean> {
        val single: Single<Boolean> = Single.create{ e ->
            val query: Query = enemyLobbyRef.child(FIELD_MODE)
            query.addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        val modeStr: String = snapshot.value as String
                        if (mode.equals(modeStr)) {
                            query.removeEventListener(this)
                            Log.d(TAG_LOG, "modeStr = $modeStr")
                            e.onSuccess(true)
                        }
                    }
                }

            })
        }
        return single.compose(RxUtils.asyncSingle())
    }

    fun changeGameMode(mode: String): Single<Boolean> {
        val single: Single<Boolean> = Single.create{e ->
            myLobbyRef.child(FIELD_MODE).setValue(mode)
            e.onSuccess(true)
        }
        return single.compose(RxUtils.asyncSingle())
    }

    private fun compareLastCards(lobby: Lobby) {
        lastMyChosenCardId?.let {
            cardRepository.findFullCard(it).subscribe { myLastCard ->
                lastEnemyChoose?.cardId?.let { it1 ->
                    cardRepository
                            .findFullCard(it1).subscribe { enemyLastCard ->
                                var c = 0

                                c += compareCardsParameter({ card -> card.intelligence }, myLastCard, enemyLastCard)
                                c += compareCardsParameter({ card -> card.support }, myLastCard, enemyLastCard)
                                c += compareCardsParameter({ card -> card.prestige }, myLastCard, enemyLastCard)
                                c += compareCardsParameter({ card -> card.hp }, myLastCard, enemyLastCard)
                                c += compareCardsParameter({ card -> card.strength }, myLastCard, enemyLastCard)

                                when {
                                    c > 0 -> onWin(lobby)
                                    c < 0 -> onLose()
                                    else -> onDraw()
                                }

                            }
                }
            }
        }
    }

    private fun onDraw() {
        onYouLoseCard?.let { callbacks.onGameEnd(GameEndType.DRAW, it) }
    }

    fun compareCardsParameter(f: ((card: Card) -> Int), card1: Card, card2: Card): Int {
        return f(card1).compareTo(f(card2))
    }

    private fun onWin(lobby: Lobby) {
        moveCardAfterWin(lobby)
        onEnemyLoseCard?.let { callbacks.onGameEnd(GameEndType.YOU_WIN, it) }
        removeListeners()

    }

    private fun onLose() {
        onYouLoseCard?.let { callbacks.onGameEnd(GameEndType.YOU_LOSE, it) }
        removeListeners()
    }

    fun watchMyStatus() {
        val myConnect = databaseReference.root.child(USERS).child(UserRepository.currentId).child(FIELD_STATUS)
        myConnect.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                if(OFFLINE_STATUS.equals(snapshot.value)) {
                    Log.d(TAG_LOG,"my disconnect")
                    removeRedundantLobbies(true)
                }
            }

        })
        myConnect.onDisconnect().setValue(OFFLINE_STATUS)
    }

    fun onDisconnectAndLose(shouldFind: Boolean) {
        onYouLoseCard?.let { callbacks.onGameEnd(GameEndType.YOU_DISCONNECTED_AND_LOSE, it) }
        removeRedundantLobbies(shouldFind)
        removeListeners()
    }

    fun onEnemyDisconnectAndYouWin(lobby: Lobby) {
        moveCardAfterWin(lobby)
        onEnemyLoseCard?.let { callbacks.onGameEnd(GameEndType.ENEMY_DISCONNECTED_AND_YOU_WIN, it) }
        removeRedundantLobbies(true)
        removeListeners()

    }

    private fun moveCardAfterWin(lobby: Lobby) {
        lobby.gameData?.enemyId?.let {
            onEnemyLoseCard?.id?.let { it1 ->
                RepositoryProvider.cardRepository.addCardAfterGame(it1, AppHelper.currentUser.id, it)
                        .subscribe()
            }
        }
    }

    fun findOfficialTests(userId: String): Single<List<Lobby>> {
        return findTestsByType(userId, OFFICIAL_TYPE)
    }

    fun findUserTests(userId: String): Single<List<Lobby>> {
        return findTestsByType(userId, USER_TYPE)
    }

    fun findOfficialTestsByQuery(query: String, userId: String): Single<List<Lobby>> {
        return findTestsByTypeByQuery(query, userId, OFFICIAL_TYPE)
    }

    fun findUserTestsByQuery(query: String, userId: String): Single<List<Lobby>> {
        return findTestsByTypeByQuery(query, userId, USER_TYPE)
    }

    fun findTestsByType(userId: String, type: String): Single<List<Lobby>> {
        val single: Single<List<Lobby>> = Single.create { e ->
            cardRepository.findCardsByType(userId, type, false).subscribe { myCards ->
                val myNumber = myCards.size
                findEntityByFieldValue(FIELD_TYPE, type).subscribe { lobbies ->
                    val list: MutableList<Lobby> = ArrayList()
                    for (lobby in lobbies) {
                        if ((ONLINE_STATUS.equals(lobby.status) && lobby.type.equals(type)) && !lobby.isFastGame
                                && (myNumber >= lobby.cardNumber || lobby.id.equals(AppHelper.currentUser.lobbyId))) {
                            if (lobby.id.equals(AppHelper.currentUser.lobbyId)) {
                                lobby.isMyCreation = true
                            }
                            list.add(lobby)
                        }
                    }
                    e.onSuccess(list)
                }
            }
        }
        return single.compose(RxUtils.asyncSingle())
    }

    fun findTestsByTypeByQuery(userQuery: String, userId: String, type: String): Single<List<Lobby>> {
        val single: Single<List<Lobby>> = Single.create { e ->
            cardRepository.findCardsByType(userId, type, false).subscribe { myCards ->
                val myNumber = myCards.size
                findByQueryField(FIELD_LOWER_TITLE, userQuery).subscribe { lobbies ->
                    val list: MutableList<Lobby> = ArrayList()
                    for (item in lobbies) {
                        if ((ONLINE_STATUS.equals(item.status) && item.type.equals(type)) && !item.isFastGame
                                && (myNumber >= item.cardNumber || item.id.equals(AppHelper.currentUser.lobbyId))) {
                            if (item.id.equals(AppHelper.currentUser.lobbyId)) {
                                item.isMyCreation = true
                            }
                            list.add(item)
                        }
                    }
                    e.onSuccess(list)
                }
            }
        }
        return single.compose(RxUtils.asyncSingle())
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
