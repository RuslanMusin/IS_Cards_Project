package com.summer.itis.cardsproject.ui.game.game_list.game

import android.widget.ProgressBar
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.summer.itis.cardsproject.model.game.Lobby
import com.summer.itis.cardsproject.ui.base.base_first.BaseAdapter
import com.summer.itis.cardsproject.ui.base.base_first.BaseView
import com.summer.itis.cardsproject.ui.game.game_list.GameAdapter
import io.reactivex.disposables.Disposable

interface GameListView : BaseView, BaseAdapter.OnItemClickListener<Lobby> {

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun handleError(throwable: Throwable)

    fun setNotLoading()

    fun showLoading(disposable: Disposable)

    fun hideLoading()

    fun showDetails(lobby: Lobby)

    fun loadNextElements(i: Int)

    fun setCurrentType(type: String)

    fun setAdapter(adapter: GameAdapter)

    fun loadOfficialTests()

    fun loadUserTests()

    fun setProgressBar(progressBar: ProgressBar)

    fun changeAdapter(position: Int)

    fun changeDataSet(games: List<Lobby>)

    fun onGameFinded()

    fun onBotGameFinded()

}
