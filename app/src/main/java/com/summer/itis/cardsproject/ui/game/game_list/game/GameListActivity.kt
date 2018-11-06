package com.summer.itis.cardsproject.ui.game.game_list.game

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.summer.itis.cardsproject.R
import com.summer.itis.cardsproject.model.game.Lobby
import com.summer.itis.cardsproject.repository.database.user.UserRepository
import com.summer.itis.cardsproject.ui.base.navigation_base.NavigationBaseActivity
import com.summer.itis.cardsproject.ui.game.game_list.GameAdapter
import com.summer.itis.cardsproject.ui.game.game_list.fragment.GameListFragment
import com.summer.itis.cardsproject.ui.game.play.bot_play.BotGameActivity
import com.summer.itis.cardsproject.ui.game.play.user_play.PlayGameActivity
import com.summer.itis.cardsproject.utils.Const
import com.summer.itis.cardsproject.utils.Const.ONLINE_STATUS
import com.summer.itis.cardsproject.utils.Const.TAG_LOG
import com.summer.itis.cardsproject.widget.FragViewPagerAdapter

import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_test_pager.*


class GameListActivity : NavigationBaseActivity<GameListPresenter>(), GameListView {

    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: GameAdapter

    @InjectPresenter
    override lateinit var presenter: GameListPresenter

    private var isLoading = false
    private var currentType: String? = null

    var isClickable: Boolean = true

    override fun setStartStatus() {
        setStartWaiting(ONLINE_STATUS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbarData()
        setViewPagerData()
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_test_pager
    }
    
    private fun setToolbarData() {
        supportActionBar(toolbar)
        setToolbarTitle(getString(R.string.games))
    }
    
    private fun setViewPagerData() {
        setupViewPager(viewpager)
        tab_layout.setupWithViewPager(viewpager)
        viewpager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab_layout))
        setTabListener()

    }

    private fun setTabListener() {
        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                Log.d(Const.TAG_LOG, "on tab selected")
                viewpager.currentItem = tab.position
                this@GameListActivity.changeAdapter(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
    }

    override fun changeAdapter(position: Int) {
        val fragment = (viewpager.adapter as FragViewPagerAdapter<*>).getFragmentForChange(position)
        (fragment as GameListFragment).changeDataInAdapter()
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = FragViewPagerAdapter<GameListFragment>(supportFragmentManager)
        adapter.addFragment(GameListFragment.newInstance(Const.OFFICIAL_LIST, this), Const.OFFICIAL_LIST)
        adapter.addFragment(GameListFragment.newInstance(Const.USER_LIST, this), Const.USER_LIST)
        this.currentType = Const.OFFICIAL_LIST
        viewPager.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.game_list_menu, menu)
        setSearchMenuItem(menu)
        setBotItem(menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setBotItem(menu: Menu) {
        val botItem = menu.findItem(R.id.action_find_bot)
        botItem.setOnMenuItemClickListener{
            Log.d(TAG_LOG,"find bot")
            presenter.findBotGame()
            true
        }
    }

    private fun setSearchMenuItem(menu: Menu) {
        val searchItem = menu.findItem(R.id.action_search)
        var searchView: SearchView? = null
        if (searchItem != null) {
            searchView = searchItem.actionView as SearchView
        }
        if (searchView != null) {
            val finalSearchView = searchView
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String): Boolean {
                    when (currentType) {
                        Const.OFFICIAL_LIST -> presenter.loadOfficialGamesByQuery(query)

                        Const.USER_LIST -> presenter.loadUserGamesByQuery(query, UserRepository.currentId)
                    }
                    if (!finalSearchView.isIconified) {
                        finalSearchView.isIconified = true
                    }
                    searchItem.collapseActionView()
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return false
                }
            })
        }
    }

    override fun onItemClick(item: Lobby) {
        if(isClickable) {
            presenter.onItemClick(item)
            isClickable = false
        }
    }

    override fun handleError(throwable: Throwable) {
        Log.d(Const.TAG_LOG, "throwable = " + throwable.message)
        throwable.printStackTrace()
        Toast.makeText(this, throwable.message, Toast.LENGTH_LONG).show()
    }

    override fun changeDataSet(games: List<Lobby>) {
        adapter.changeDataSet(games)
    }

    override fun setAdapter(adapter: GameAdapter) {
        Log.d(Const.TAG_LOG, "set adapter")
        Log.d(Const.TAG_LOG, "type adapter =  " + currentType)
        this.adapter = adapter
    }

    override fun loadOfficialTests() {
        Log.d(Const.TAG_LOG, "load requests")
        presenter.loadOfficialGames()
    }

    override fun loadUserTests() {
        Log.d(Const.TAG_LOG, "load friends")
        presenter.loadUserGames(UserRepository.currentId)
    }

    override fun setProgressBar(progressBar: ProgressBar) {
        this.progressBar = progressBar
        Log.d(Const.TAG_LOG,"set proggress  and type = $currentType}")
    }

    override fun setNotLoading() {
        isLoading = false
    }

    override fun showLoading(disposable: Disposable) {
        Log.d(Const.TAG_LOG,"show loading  and type = $currentType}")
        progressBar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        Log.d(Const.TAG_LOG,"hide loading and type = $currentType}")
        progressBar.visibility = View.GONE
    }

    override fun showDetails(lobby: Lobby) {
        Log.d(Const.TAG_LOG,"show test act")
        presenter.findGame(lobby)

    }

    override fun onGameFinded(){
            Log.d(TAG_LOG, "start usual game")
            PlayGameActivity.start(this)
    }

    override fun hideProgressDialog() {
        showSnackBar("Противник не принял приглашение")
        super.hideProgressDialog()
        isClickable = true
    }

    override fun onBotGameFinded() {
        Log.d(TAG_LOG,"start bot")
        BotGameActivity.start(this)
    }

    override fun loadNextElements(i: Int) {
//        presenter.loadNextElements(i)
    }

    override fun setCurrentType(type: String) {
        Log.d(Const.TAG_LOG, "current type = $type")
        this.currentType = type
    }

    companion object {

        fun start(context: Context) {
            val intent = Intent(context, GameListActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }
    }
}
