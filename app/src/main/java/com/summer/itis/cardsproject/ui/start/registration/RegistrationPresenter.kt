package com.summer.itis.cardsproject.ui.start.registration

import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.summer.itis.cardsproject.R
import com.summer.itis.cardsproject.model.user.User
import com.summer.itis.cardsproject.ui.base.base_first.BasePresenter
import com.summer.itis.cardsproject.ui.start.registration.RegistrationActivity.Companion.TAG_REGISTRATION
import com.summer.itis.cardsproject.utils.AppHelper
import com.summer.itis.cardsproject.utils.Const
import com.summer.itis.cardsproject.utils.Const.AVATAR
import com.summer.itis.cardsproject.utils.Const.TAG_LOG


@InjectViewState
class RegistrationPresenter: BasePresenter<RegistrationView>() {

    fun createAccount(email: String, password: String) {
        Log.d(TAG_LOG, "createAccount:$email")
        if (!validateForm(email, password)) {
            return
        }

        viewState.showProgressDialog(R.string.progress_message)
        viewState.prepareUserBeforeCreate()

    }

    fun createUserInDatabase(user: User, imageUri: Uri?) {

        if (!user.isStandartPhoto) {
            user.photoUrl = Const.IMAGE_START_PATH + user.id +
                             Const.SEP + AVATAR


        }

    }

    private fun validateForm(email: String, password: String): Boolean {
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

    private fun updateUI(user: User) {
        AppHelper.currentUser = user
        AppHelper.userInSession = true
        viewState.goToProfile(user)

    }
}
