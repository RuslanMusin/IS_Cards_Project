package com.summer.itis.cardsproject.ui.start.login


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.google.android.gms.flags.impl.SharedPreferencesFactory.getSharedPreferences
import com.summer.itis.cardsproject.R
import com.summer.itis.cardsproject.model.test.Test
import com.summer.itis.cardsproject.model.user.User
import com.summer.itis.cardsproject.ui.base.base_first.BaseActivity
import com.summer.itis.cardsproject.ui.member.member_item.PersonalActivity
import com.summer.itis.cardsproject.ui.start.registration.RegistrationActivity
import com.summer.itis.cardsproject.ui.tests.test_item.TestActivity
import com.summer.itis.cardsproject.utils.Const.USER_DATA_PREFERENCES
import com.summer.itis.cardsproject.utils.Const.USER_PASSWORD
import com.summer.itis.cardsproject.utils.Const.USER_USERNAME
import kotlinx.android.synthetic.main.activity_login.*


/**
 * Created by Ruslan on 18.02.2018.
 */

class LoginActivity : BaseActivity<LoginPresenter>(), LoginView, View.OnClickListener {

    @InjectPresenter
    override lateinit var presenter: LoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setListeners()
        checkUserSession()
    }

    private fun checkUserSession() {
        val sharedPreferences: SharedPreferences = getSharedPreferences(USER_DATA_PREFERENCES,Context.MODE_PRIVATE)
        if(sharedPreferences.contains(USER_USERNAME)) {
            val email: String = sharedPreferences.getString(USER_USERNAME,"")
            val passwored: String = sharedPreferences.getString(USER_PASSWORD,"")
            presenter.signIn(email,passwored)
        }
    }

    override fun showEmailError(hasError: Boolean) {
        if(hasError) {
            ti_email.error = getString(R.string.enter_correct_name)
        } else {
            ti_email.error = null
        }

    }

    override fun showPasswordError(hasError: Boolean) {
        if(hasError) {
            ti_password.error = getString(R.string.enter_correct_password)
        } else {
            ti_password.error = null
        }

    }

    private fun setListeners() {
        btn_enter.setOnClickListener(this)
        link_signup.setOnClickListener(this)
        iv_cover.setOnClickListener(this)
        tv_name.setOnClickListener(this)
    }

    override fun onClick(view: View) {

        when (view.id) {

            R.id.btn_enter -> {
                val username = et_email.getText().toString();
                val password = et_password.getText().toString();
                presenter.signIn(username, password)
            }

            R.id.link_signup -> goToRegistration()

            R.id.tv_name -> {
                et_email.setText("rast@ma.ru")
                et_password.setText("rastamka")

            }

            R.id.iv_cover -> {
                et_email.setText("rust@ma.ru")
                et_password.setText("rustamka")
            }
        }
    }

    override fun goToProfile(user: User) {
//        TestActivity.start(this, Test())
        PersonalActivity.start(this, user)
    }

    private fun goToRegistration() {
        RegistrationActivity.start(this)
    }

    override fun showError() {
        ti_email.error = getString(R.string.enter_correct_name)
        ti_password.error = getString(R.string.enter_correct_password)
    }

    override fun createCookie(email: String, password: String) {
        val mySharedPreferences = getSharedPreferences(USER_DATA_PREFERENCES, Context.MODE_PRIVATE)
        if(!mySharedPreferences.contains(USER_USERNAME)) {
            val editor = mySharedPreferences.edit()
            editor.putString(USER_USERNAME, email)
            editor.putString(USER_PASSWORD, password)
            editor.apply()
        }
    }

    companion object {

        const val  TAG_LOGIN = "TAG_LOGIN"

        fun start(activity: Context) {
            val intent = Intent(activity, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            activity.startActivity(intent)
        }
    }
}
