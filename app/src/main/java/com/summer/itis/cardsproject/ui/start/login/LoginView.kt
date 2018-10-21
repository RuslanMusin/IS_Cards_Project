package com.summer.itis.cardsproject.ui.start.login

import com.summer.itis.cardsproject.model.user.User
import com.summer.itis.cardsproject.ui.base.base_first.BaseView


interface LoginView: BaseView {

    fun showEmailError(hasError: Boolean)

    fun showPasswordError(hasError: Boolean)

    fun showError()

    fun createCookie(email: String, password: String)

    fun goToProfile(user: User)
}