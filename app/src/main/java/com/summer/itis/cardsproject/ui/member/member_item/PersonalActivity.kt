package com.summer.itis.cardsproject.ui.member.member_item

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import com.afollestad.materialdialogs.MaterialDialog
import com.arellomobile.mvp.presenter.InjectPresenter
import com.google.gson.Gson
import com.summer.itis.cardsproject.ui.base.navigation_base.NavigationBaseActivity
import com.summer.itis.cardsproject.ui.member.member_item.PersonalPresenter
import com.summer.itis.cardsproject.R
import com.summer.itis.cardsproject.model.user.User
import com.summer.itis.cardsproject.utils.AppHelper
import com.summer.itis.cardsproject.utils.Const.ONLINE_STATUS
import com.summer.itis.cardsproject.utils.Const.OWNER_TYPE
import com.summer.itis.cardsproject.utils.Const.USER_KEY
import com.summer.itis.cardsproject.utils.Const.gsonConverter
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.layout_expandable_text_view.*
import kotlinx.android.synthetic.main.layout_personal.*


class PersonalActivity : NavigationBaseActivity<PersonalPresenter>(), PersonalView, View.OnClickListener {
    
    lateinit var user: User
    var type: String = OWNER_TYPE
    lateinit var gameDialog: MaterialDialog
    lateinit var types: List<String>

    @InjectPresenter
    override lateinit var presenter: PersonalPresenter

    companion object {
        
        const val TAG_PERS_ACT = "TAG_PERS_ACT"

        fun start(context: Context, user: User) {
            val intent = Intent(context, PersonalActivity::class.java)
            val gson = Gson()
            val userJson = gson.toJson(user)
            intent.putExtra(USER_KEY, userJson)
            context.startActivity(intent)
        }

        fun start(context: Context) {
            val intent = Intent(context, PersonalActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }
    }

    override fun setStartStatus() {
        setStartWaiting(ONLINE_STATUS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = gsonConverter.fromJson(intent.getStringExtra(USER_KEY), User::class.java)
        initViews(OWNER_TYPE)
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_profile
    }

    override fun initViews(type: String) {
        Log.d(TAG_PERS_ACT, "type = " + type)
        this.type = type
        findViews()
        setToolbarData()
        setListeners()
    }
    
    private fun setToolbarData() {
        if(type.equals(OWNER_TYPE)) {
            supportActionBar(tb_profile)
        } else {
            setSupportActionBar(tb_profile)
            setBackArrow(tb_profile)
        }
        tb_profile.title = user.username

    }
    
    private fun setListeners() {
        btn_add_friend.setOnClickListener(this)
        li_tests.setOnClickListener(this)
        li_cards.setOnClickListener(this)
        btn_play_game.setOnClickListener(this)
    }

    private fun findViews() {
        if (type == OWNER_TYPE) {
            user = AppHelper.currentUser
            setUserData()
        } else {
            setUserData()
        }
    }

    private fun setUserData() {
        tv_username.text = user.username
        expand_text_view.text = user.description
        AppHelper.setUserPhoto(iv_user_photo, user, this)
        setUserBtnText()
    }
    
    private fun setUserBtnText() {
        when (type) {
            OWNER_TYPE -> {
                btn_add_friend.visibility = View.GONE
                btn_play_game.visibility = View.GONE
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
        }
    }
}
