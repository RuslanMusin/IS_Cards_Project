package com.summer.itis.cardsproject.ui.base.navigation_base

import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.FrameLayout
import com.summer.itis.cardsproject.R
import com.summer.itis.cardsproject.R.id.drawer_layout
import com.summer.itis.cardsproject.R.id.nav_view
import com.summer.itis.cardsproject.R.string.logout
import com.summer.itis.cardsproject.model.common.Relation
import com.summer.itis.cardsproject.model.game.GameData
import com.summer.itis.cardsproject.model.game.Lobby
import com.summer.itis.cardsproject.model.game.LobbyPlayerData
import com.summer.itis.cardsproject.model.test.Test
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.gamesRepository
import com.summer.itis.cardsproject.repository.RepositoryProvider.Companion.userRepository
import com.summer.itis.cardsproject.repository.database.game.GamesRepository.Companion.FIELD_CREATOR
import com.summer.itis.cardsproject.repository.database.user.UserRepository
import com.summer.itis.cardsproject.ui.base.base_first.BaseActivity
import com.summer.itis.cardsproject.ui.base.base_first.BasePresenter
import com.summer.itis.cardsproject.ui.game.game_list.game.GameListActivity
import com.summer.itis.cardsproject.ui.game.play.bot_play.BotGameActivity
import com.summer.itis.cardsproject.ui.start.login.LoginActivity
import com.summer.itis.cardsproject.ui.tests.test_item.TestActivity
import com.summer.itis.cardsproject.utils.AppHelper
import com.summer.itis.cardsproject.utils.Const.BOT_GAME
import com.summer.itis.cardsproject.utils.Const.BOT_ID
import com.summer.itis.cardsproject.utils.Const.IN_GAME_STATUS
import com.summer.itis.cardsproject.utils.Const.OFFLINE_STATUS
import com.summer.itis.cardsproject.utils.Const.TAG_LOG
import com.summer.itis.cardsproject.utils.Const.USER_TYPE
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.view_nav_header.view.*

//АКТИВИТИ РОДИТЕЛЬ ДЛЯ ОСНОВНОЙ НАВИГАЦИИ(БОКОВОЙ). ЮЗАТЬ МЕТОДЫ supportActionBar И setBackArrow(ЕСЛИ НУЖНА СТРЕЛКА НАЗАД)
abstract class NavigationBaseActivity<Presenter: BasePresenter<*>> : BaseActivity<Presenter>() {

    companion object {

        const val TAG_NAVIG_ACT = "TAG_NAVIG_ACTIVITY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        val contentFrameLayout = findViewById<FrameLayout>(R.id.container)
        layoutInflater.inflate(getContentLayout(), contentFrameLayout)
    }

    abstract fun getContentLayout(): Int

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    protected fun supportActionBar(toolbar: Toolbar) {
        if(supportActionBar == null) {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
            initNavigationDrawer(toolbar)
        }
    }



    private fun initNavigationDrawer(toolbar: Toolbar) {
        setNavigListener()
        setHeaderData()
        setActionBar(toolbar)
    }

    private fun setNavigListener() {
        nav_view.setNavigationItemSelectedListener { menuItem ->
            val id = menuItem.itemId
            when (id) {
                R.id.menu_tests -> showTests()

                R.id.menu_game -> showGame()

                R.id.menu_cards -> showCards()

                R.id.menu_friends -> showFriends()

                R.id.menu_logout -> logout()
            }
            true
        }
    }

    private fun setHeaderData() {
        val header = nav_view.getHeaderView(0)
        header.tv_menu.text = AppHelper.currentUser.username
        AppHelper.setPhotoAndListener(header.iv_crossing, AppHelper.currentUser, this)
    }

    private fun setActionBar(toolbar: Toolbar) {
        Log.d(TAG_NAVIG_ACT, "set action bar")

        val actionBarDrawerToggle = ActionBarDrawerToggle(this, drawer_layout, toolbar,
                R.string.drawer_open, R.string.drawer_close)
        drawer_layout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
    }


    private fun logout() {
        LoginActivity.start(this)
    }

    private fun showCards() {}

    private fun showFriends() {
    }

    private fun showGame() {
        GameListActivity.start(this)
    }

    private fun showTests() {
        TestActivity.start(this, Test())
    }

   /* private fun prepareBotData(): Lobby {
        val lobby: Lobby = Lobby()

        val playerData = LobbyPlayerData()
        playerData.playerId = UserRepository.currentId
        playerData.online = true

        lobby.creator = playerData
        lobby.status = IN_GAME_STATUS
        lobby.isFastGame = true
        lobby.type = USER_TYPE

        val enemyData = LobbyPlayerData()
        enemyData.playerId = BOT_ID
        enemyData.online = true

        val gameData: GameData = GameData()
        gameData.enemyId = BOT_ID
        gameData.gameMode = BOT_GAME
        gameData.role = FIELD_CREATOR
        lobby.gameData = gameData

        return lobby
    }

    private fun playWithBot(lobby: Lobby) {
        AppHelper.currentUser.let {
            it.gameLobby = lobby
            Log.d(TAG_LOG,"enemyId = ${lobby.gameData?.enemyId}")
            Log.d(TAG_LOG,"enemyId 2= ${it.gameLobby?.gameData?.enemyId}")
            gamesRepository.createBotLobby(lobby).subscribe { created ->
                val relation: Relation = Relation()
                relation.relation = IN_GAME_STATUS
                relation.id = lobby.id
                BotGameActivity.start(this)
            }
        }
    }*/
}
