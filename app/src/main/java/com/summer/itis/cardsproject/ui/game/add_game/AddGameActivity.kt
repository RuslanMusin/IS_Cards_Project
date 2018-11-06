package com.summer.itis.cardsproject.ui.game.add_game

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.view.View
import android.widget.SeekBar
import com.arellomobile.mvp.presenter.InjectPresenter
import com.bumptech.glide.Glide
import com.summer.itis.cardsproject.R
import com.summer.itis.cardsproject.model.common.PhotoItem
import com.summer.itis.cardsproject.model.game.Lobby
import com.summer.itis.cardsproject.model.game.LobbyPlayerData
import com.summer.itis.cardsproject.repository.database.user.UserRepository
import com.summer.itis.cardsproject.ui.base.base_first.BaseActivity
import com.summer.itis.cardsproject.ui.game.add_photo.AddPhotoActivity
import com.summer.itis.cardsproject.ui.game.game_list.game.GameListActivity
import com.summer.itis.cardsproject.utils.Const.ADD_CARD
import com.summer.itis.cardsproject.utils.Const.EDIT_STATUS
import com.summer.itis.cardsproject.utils.Const.ITEM_JSON
import com.summer.itis.cardsproject.utils.Const.OFFICIAL_TYPE
import com.summer.itis.cardsproject.utils.Const.ONLINE_STATUS
import com.summer.itis.cardsproject.utils.Const.USER_ID
import com.summer.itis.cardsproject.utils.Const.USER_TYPE
import com.summer.itis.cardsproject.utils.Const.gsonConverter
import kotlinx.android.synthetic.main.activity_add_game.*

class AddGameActivity : BaseActivity<AddGamePresenter>(), AddGameView, View.OnClickListener {

    @InjectPresenter
    override lateinit var presenter: AddGamePresenter

    var lobby: Lobby = Lobby()
    lateinit var types: List<String>

    companion object {

        fun start(activity: Activity) {
            val intent = Intent(activity, AddGameActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun setStartStatus() {
        setStatus(EDIT_STATUS)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_game)
        initViews()
    }

    private fun initViews() {
        setToolbarData()
        setSpinner()
        setListeners()
    }

    private fun setToolbarData() {
        setBackArrow(toolbar)
        setToolbarTitle(getString(R.string.new_game))
    }

    private fun setSpinner() {
        types = listOf(getString(R.string.user_type), getString(R.string.official_type))
        spinner.setItems(types)
    }

    private fun setListeners() {
        btn_add_game_photo.setOnClickListener(this)
        btn_create_game.setOnClickListener(this)
        seekBarCards.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val strProgress: String = seekBar?.progress.toString()
                tvCards.text = strProgress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
    }

    override fun onClick(v: View) {
        when (v.id) {

            R.id.btn_create_game -> {
               prepareCreatingGame()
            }

            R.id.btn_add_game_photo -> {
                addPhoto()
            }
        }
    }

    private fun prepareCreatingGame() {
        lobby.cardNumber = seekBarCards.progress
        if(lobby.cardNumber >= 5) {
            if (types[spinner.selectedIndex].equals(getString(R.string.official_type))) {
                lobby.type = OFFICIAL_TYPE
            } else {
                lobby.type = USER_TYPE
            }
            presenter.checkCanCreateGame(lobby)

        } else {
            showSnackBar(R.string.set_card_min)
        }
    }

    override fun createGame(lobby: Lobby) {
        lobby.title = et_game_name.text.toString()
        lobby.lowerTitle = lobby.title?.toLowerCase()
        lobby.status = ONLINE_STATUS
        lobby.isFastGame = false
        val playerData = LobbyPlayerData()
        playerData.playerId = UserRepository.currentId
        playerData.online = true
        lobby.creator = playerData

        presenter.createGame(lobby)
    }

    private fun addPhoto() {
        val intent = Intent(this, AddPhotoActivity::class.java)
        intent.putExtra(USER_ID, UserRepository.currentId)
        startActivityForResult(intent, ADD_CARD)
    }

    override fun onActivityResult(reqCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(reqCode, resultCode, data)

        if (reqCode == ADD_CARD && resultCode == Activity.RESULT_OK) {
            val photoItem = gsonConverter.fromJson(data!!.getStringExtra(ITEM_JSON), PhotoItem::class.java)
            Glide.with(iv_cover.context)
                    .load(photoItem.photoUrl)
                    .into(iv_cover)
            lobby.photoUrl = photoItem.photoUrl
        }
    }

    override fun onGameCreated() {
        GameListActivity.start(this)
    }
}