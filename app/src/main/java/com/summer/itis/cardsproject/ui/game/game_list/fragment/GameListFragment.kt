package com.summer.itis.cardsproject.ui.game.game_list.fragment

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.summer.itis.cardsproject.R
import com.summer.itis.cardsproject.model.game.Lobby
import com.summer.itis.cardsproject.ui.game.add_game.AddGameActivity
import com.summer.itis.cardsproject.ui.game.game_list.GameAdapter
import com.summer.itis.cardsproject.ui.game.game_list.game.GameListView
import com.summer.itis.cardsproject.utils.AppHelper
import com.summer.itis.cardsproject.utils.Const
import kotlinx.android.synthetic.main.fragment_recycler_list.*
import kotlinx.android.synthetic.main.fragment_test_list.*

import java.util.*

class GameListFragment : Fragment() {
    
    lateinit var adapter: GameAdapter
    private lateinit var type: String
    private lateinit var parentView: GameListView
    private var isLoaded = false

    companion object {

        fun newInstance(type: String, parentView: GameListView): GameListFragment {
            val fragment = GameListFragment()
            fragment.type = type
            fragment.parentView = parentView
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_test_list, container, false)
        Log.d(Const.TAG_LOG, "create view = " + this.type)
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(Const.TAG_LOG, "on view created = " + this.type)
        initRecycler()
        if (!isLoaded && type.equals(Const.OFFICIAL_LIST)) {
            parentView.changeAdapter(0)
        }
        super.onViewCreated(view, savedInstanceState)
    }
    
    private fun initRecycler() {
        adapter = GameAdapter(ArrayList<Lobby>())
        val manager = LinearLayoutManager(this.activity)
        rv_list.layoutManager = manager
        rv_list.setEmptyView(tv_empty)
        adapter.attachToRecyclerView(rv_list)
        adapter.setOnItemClickListener(parentView)

        setFloatingButton()
    }

    private fun setFloatingButton() {
        rv_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && floating_button.getVisibility() == View.VISIBLE) {
                    floating_button.hide();
                } else if (dy < 0 && floating_button.getVisibility() != View.VISIBLE) {
                    floating_button.show();
                }
            }
        })

        floating_button.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                Log.d(Const.TAG_LOG, "act float btn")
                MaterialDialog.Builder(activity as Activity)
                        .title(R.string.create_new_game)
                        .content(R.string.old_game_will_be_deleted)
                        .positiveText("Создать")
                        .onPositive(object :MaterialDialog.SingleButtonCallback {
                            override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                                AppHelper.currentUser.let { it.lobbyId?.let { it1 ->
                                    adapter.removeItemById(it1)
                                } }
                                AddGameActivity.start(activity as Activity)
                            }

                        })
                        .negativeText("Отмена")
                        .onNegative{ dialog, action -> dialog.cancel()}
                        .show()


            }
        })
    }

    fun loadGames() {
        when (type) {

            Const.USER_LIST -> parentView.loadUserTests()

            else -> parentView.loadOfficialTests()
        }
        isLoaded = true
    }

    fun changeDataInAdapter() {
        parentView.setCurrentType(type)
        parentView.setAdapter(adapter)
        parentView.setProgressBar(pb_list)
        loadGames()
    }
}