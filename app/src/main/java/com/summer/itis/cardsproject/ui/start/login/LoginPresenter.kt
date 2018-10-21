package com.summer.itis.cardsproject.ui.start.login

import android.text.TextUtils
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.summer.itis.cardsproject.R
import com.summer.itis.cardsproject.model.user.User
import com.summer.itis.cardsproject.ui.base.base_first.BasePresenter
import com.summer.itis.cardsproject.ui.start.login.LoginActivity.Companion.TAG_LOGIN
import com.summer.itis.cardsproject.utils.AppHelper
import com.summer.itis.cardsproject.utils.Const.ONLINE_STATUS
import com.summer.itis.cardsproject.utils.Const.TAG_LOG


@InjectViewState
class LoginPresenter: BasePresenter<LoginView>() {

    fun signIn(email: String, password: String) {
        Log.d(TAG_LOG, "signIn:$email")
        if (!validateForm(email, password)) {
            return
        }

        viewState.showProgressDialog(R.string.progress_message)
        updateUI()

    }

    fun validateForm(email: String, password: String): Boolean {
        var valid = true

        if (TextUtils.isEmpty(email)) {
            viewState.showEmailError(true)
            valid = false
        } else {
            viewState.showEmailError(false)
        }

        if (TextUtils.isEmpty(password)) {
            viewState.showPasswordError(true)
            valid = false
        } else {
            viewState.showPasswordError(false)
        }

        return valid
    }

    fun updateUI() {
        val user: User = User()
        setUserSession(user)
        viewState.goToProfile(user)
    }

    fun setUserSession(user: User) {
        AppHelper.currentUser = user
        AppHelper.userInSession = true
    }
}
